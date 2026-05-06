package com.elfmcys.yesstevemodel.audio;

import net.minecraft.client.sounds.AudioStream;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioStreamWrapper implements IAudioStreamSupport {

    private static final ByteBuffer EMPTY_BUFFER = BufferUtils.createByteBuffer(0);

    private final IAudioStreamFactory streamFactory;

    private final AudioFormat audioFormat;

    private AudioStream currentStream;

    private volatile boolean closed;

    public AudioStreamWrapper(IAudioStreamFactory streamFactory2) throws UnsupportedAudioFileException, IOException {
        this.streamFactory = streamFactory2;
        try {
            this.currentStream = streamFactory2.openStream();
            this.audioFormat = this.currentStream.getFormat();
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @NotNull
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    @NotNull
    public ByteBuffer read(int i) throws IOException {
        if (this.currentStream != null) {
            ByteBuffer byteBuffer = this.currentStream.read(i);
            if (byteBuffer.remaining() == 0) {
                this.currentStream = null;
                try {
                    reset();
                    this.currentStream = this.streamFactory.openStream();
                    byteBuffer = this.currentStream.read(i);
                    if (byteBuffer.remaining() == 0) {
                        reset();
                        return EMPTY_BUFFER;
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                    return EMPTY_BUFFER;
                }
            }
            return byteBuffer;
        }
        return EMPTY_BUFFER;
    }

    public void close() throws IOException {
        this.closed = true;
        reset();
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    public void reset() throws IOException {
        if (this.currentStream != null) {
            this.currentStream.close();
            this.currentStream = null;
        }
    }
}