package com.elfmcys.yesstevemodel.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class YSMSoundInstance extends YSMTickableSoundInstance {

    private final IAudioStreamFactory streamFactory;

    private volatile IAudioStreamSupport audioStream;

    public YSMSoundInstance(SoundEvent soundEvent, IAudioStreamFactory streamFactory2, Entity entity) {
        super(soundEvent, entity);
        this.streamFactory = streamFactory2;
    }

    @NotNull
    public CompletableFuture<AudioStream> getStream(@NotNull SoundBufferLibrary soundBufferLibrary, @NotNull Sound sound, boolean z) {
        CompletableFuture<AudioStream> completableFuture = new CompletableFuture<>();
        Minecraft.getInstance().execute(() -> {
            try {
                IAudioStreamSupport audioStreamSupport = z ? new AudioStreamWrapper(this.streamFactory) : this.streamFactory.openStream();
                this.audioStream = audioStreamSupport;
                completableFuture.complete(audioStreamSupport);
            } catch (Throwable th) {
                completableFuture.completeExceptionally(th);
            }
        });
        return completableFuture;
    }

    @Override
    public boolean isStopped() {
        if (this.audioStream == null) {
            return super.isStopped();
        }
        if (this.audioStream.isClosed()) {
            if (!super.isStopped()) {
                super.release();
                return true;
            }
            return true;
        }
        return super.isStopped();
    }
}