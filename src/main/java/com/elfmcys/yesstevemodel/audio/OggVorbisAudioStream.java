package com.elfmcys.yesstevemodel.audio;

import com.mojang.blaze3d.audio.OggAudioStream;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OggVorbisAudioStream implements IAudioStreamSupport {

    private static final ByteBuffer EMPTY_BUFFER = BufferUtils.createByteBuffer(0);

    private final OggAudioStream oggStream;

    private final AudioFormat audioFormat;

    @Nullable
    private final AudioCacheBuilder cacheBuilder;

    private volatile boolean isClosed;

    private boolean isEndOfStream;

    public OggVorbisAudioStream(ByteBuffer byteBuffer, @Nullable AudioCacheBuilder cacheBuilder) throws UnsupportedAudioFileException, IOException {
        this.oggStream = new OggAudioStream(new ByteBufInputStream(Unpooled.wrappedBuffer(byteBuffer)));
        if (this.oggStream.getFormat().getChannels() != 1 && this.oggStream.getFormat().getChannels() != 2) {
            throw new UnsupportedAudioFileException();
        }
        this.audioFormat = new AudioFormat(this.oggStream.getFormat().getSampleRate(), 16, 1, true, false);
        this.cacheBuilder = cacheBuilder;
    }

    @NotNull
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    @NotNull
    public ByteBuffer read(int i) throws IOException {
        ByteBuffer byteBufferCreateByteBuffer;
        if (this.isEndOfStream || this.isClosed) {
            return EMPTY_BUFFER;
        }
        ByteBuffer byteBufferSlice = this.oggStream.read(this.oggStream.getFormat().getChannels() * i);
        if (!byteBufferSlice.hasRemaining()) {
            if (this.cacheBuilder != null) {
                this.cacheBuilder.flushToCache();
            }
            this.isEndOfStream = true;
            return byteBufferSlice;
        }
        if (this.oggStream.getFormat().getChannels() == 2) {
            ByteBuffer byteBufferOrder = byteBufferSlice.duplicate().order(ByteOrder.nativeOrder());
            if (!byteBufferSlice.isReadOnly()) {
                byteBufferCreateByteBuffer = byteBufferSlice.duplicate().order(ByteOrder.nativeOrder()).limit(byteBufferOrder.remaining() / 2);
            } else {
                byteBufferCreateByteBuffer = BufferUtils.createByteBuffer(byteBufferOrder.remaining() / 2);
            }
            byteBufferSlice = byteBufferCreateByteBuffer.slice();
            do {
                byteBufferCreateByteBuffer.putShort((short) Math.round((byteBufferOrder.getShort() + byteBufferOrder.getShort()) / 2.0f));
            } while (byteBufferOrder.hasRemaining());
        }
        if (this.cacheBuilder != null) {
            this.cacheBuilder.appendAudio(byteBufferSlice.duplicate());
        }
        return byteBufferSlice;
    }

    public void close() throws IOException {
        if (!this.isClosed) {
            this.oggStream.close();
            this.isClosed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
}