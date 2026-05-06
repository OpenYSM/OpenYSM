package com.elfmcys.yesstevemodel.audio;

import com.elfmcys.yesstevemodel.ResourceCleanupHelper;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class AudioStreamCache {

    private static final IdentityHashMap<ModelAssembly, WeakReference<CachedAudioStreamProvider>> providerCache = new IdentityHashMap<>();

    private static final Object LOCK = new Object();

    public static IAudioStreamProvider getOrCreateProvider(ModelAssembly renderContext) {
        CachedAudioStreamProvider existingProvider;
        RenderSystem.assertOnRenderThread();
        WeakReference<CachedAudioStreamProvider> weakReference = providerCache.get(renderContext);
        if (weakReference != null && (existingProvider = weakReference.get()) != null) {
            return existingProvider;
        }
        CachedAudioStreamProvider newProvider = new CachedAudioStreamProvider();
        ResourceCleanupHelper.registerCleanup(newProvider, renderContext, it -> {
            Minecraft.getInstance().execute(() -> {
                providerCache.remove(it);
            });
        });
        providerCache.put(renderContext, new WeakReference<>(newProvider));
        return newProvider;
    }

    public static class CachedAudioStreamProvider implements IAudioStreamProvider {

        private final ConcurrentHashMap<AudioTrackData, CachedAudioEntry> cachedEntries = new ConcurrentHashMap<>();

        private final ConcurrentHashMap<AudioTrackData, Object> pendingTracks = new ConcurrentHashMap<>();

        CachedAudioStreamProvider() {
        }

        public void cacheAudioData(AudioTrackData trackData, ByteBuffer byteBuffer, IntArrayList intArrayList) {
            this.cachedEntries.put(trackData, new CachedAudioEntry(byteBuffer, new AudioFormat(trackData.getSampleRate(), 16, 1, true, false), intArrayList));
            this.pendingTracks.remove(trackData);
        }

        @Override
        public IAudioStreamSupport createAudioStream(AudioTrackData trackData) throws UnsupportedAudioFileException, IOException {
            AudioCacheBuilder cacheBuilder;
            CachedAudioEntry audioEntry = this.cachedEntries.get(trackData);
            if (audioEntry != null) {
                return new SeekableAudioStream(audioEntry.audioData.duplicate(), audioEntry.seekPositions, audioEntry.audioFormat);
            }
            if (trackData.getDuration() / trackData.getSampleRate() <= 4 && !this.pendingTracks.contains(trackData)) {
                cacheBuilder = new AudioCacheBuilder(this, trackData);
                this.pendingTracks.put(trackData, AudioStreamCache.LOCK);
            } else {
                cacheBuilder = null;
            }
            return switch (trackData.getCodec()) {
                case VORBIS -> new OggVorbisAudioStream(trackData.getData(), cacheBuilder);
                case OPUS -> new OggOpusAudioStream(trackData.getData(), cacheBuilder);
                default -> throw new UnsupportedAudioFileException();
            };
        }

        private record CachedAudioEntry(ByteBuffer audioData, AudioFormat audioFormat, IntArrayList seekPositions) {
        }
    }
}