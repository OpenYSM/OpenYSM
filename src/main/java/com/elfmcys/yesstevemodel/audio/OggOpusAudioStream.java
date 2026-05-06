package com.elfmcys.yesstevemodel.audio;

import com.elfmcys.yesstevemodel.YesSteveModel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class OggOpusAudioStream implements IAudioStreamSupport {

    private static final ByteBuffer EMPTY_BUFFER = BufferUtils.createByteBuffer(0);

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(48000.0f, 16, 1, true, false);

    @Nullable
    private final AudioCacheBuilder cacheBuilder;

    private volatile boolean closed;

    private boolean endOfStream;

    private final NativeAudioDecoder decoder = ObjectPool.acquire();

    private final ByteBuf outputBuffer = PooledByteBufAllocator.DEFAULT.buffer(((int) AUDIO_FORMAT.getSampleRate()) * 2);

    public OggOpusAudioStream(ByteBuffer byteBuffer, @Nullable AudioCacheBuilder cacheBuilder2) throws UnsupportedAudioFileException {
        this.cacheBuilder = cacheBuilder2;
        this.outputBuffer.retain();
        this.decoder.openStream(byteBuffer);
    }

    @NotNull
    public ByteBuffer read(int i) throws IOException {
        if (i == 0 || this.endOfStream || this.closed) {
            return EMPTY_BUFFER;
        }
        if (this.outputBuffer.capacity() < i) {
            this.outputBuffer.capacity(i);
        }
        ByteBuffer byteBufferNioBuffer = this.outputBuffer.nioBuffer(0, i);
        int i2 = this.decoder.decodeFrame(byteBufferNioBuffer.duplicate());
        if (i2 <= 0) {
            if (i2 == 0 && this.cacheBuilder != null) {
                this.cacheBuilder.flushToCache();
            }
            if (i2 < 0) {
                YesSteveModel.LOGGER.error("Decoder error: {}", Integer.valueOf(i2));
            }
            this.endOfStream = true;
            return EMPTY_BUFFER;
        }
        ByteBuffer byteBufferSlice = byteBufferNioBuffer.slice(0, i2);
        if (this.cacheBuilder != null) {
            this.cacheBuilder.appendAudio(byteBufferSlice.slice());
        }
        return byteBufferSlice;
    }

    @NotNull
    public AudioFormat getFormat() {
        return AUDIO_FORMAT;
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.outputBuffer.release();
            ObjectPool.release(this.decoder);
            this.closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }
}