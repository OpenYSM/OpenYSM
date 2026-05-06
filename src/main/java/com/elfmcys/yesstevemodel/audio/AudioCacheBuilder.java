package com.elfmcys.yesstevemodel.audio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class AudioCacheBuilder {

    private final AudioStreamCache.CachedAudioStreamProvider cacheProvider;

    private final AudioTrackData trackData;

    private final ByteBuf audioBuffer;

    private final IntArrayList chunkSizes = new IntArrayList(5);

    private boolean isClosed = false;

    public AudioCacheBuilder(AudioStreamCache.CachedAudioStreamProvider cacheProvider, AudioTrackData trackData) {
        this.cacheProvider = cacheProvider;
        this.trackData = trackData;
        this.audioBuffer = PooledByteBufAllocator.DEFAULT.directBuffer(((int) trackData.getDuration()) * 2);
    }

    public void appendAudio(ByteBuffer byteBuffer) {
        if (!this.isClosed && this.audioBuffer.writableBytes() > 0) {
            int iMin = Math.min(this.audioBuffer.writableBytes(), byteBuffer.remaining());
            this.audioBuffer.writeBytes(byteBuffer.limit(iMin));
            this.chunkSizes.add(iMin);
        }
    }

    public void flushToCache() {
        if (!this.isClosed) {
            this.isClosed = true;
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(this.audioBuffer.readableBytes());
            this.audioBuffer.readBytes(byteBuffer.duplicate());
            this.audioBuffer.release();
            this.cacheProvider.cacheAudioData(this.trackData, byteBuffer, this.chunkSizes);
        }
    }
}