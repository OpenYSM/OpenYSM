package com.elfmcys.yesstevemodel.audio;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public class AudioTrackData {

    @Nullable
    private final ByteBuffer data;

    private final AudioCodec codec;

    private final int sampleRate;

    private final long duration;

    public AudioTrackData(@Nullable ByteBuffer byteBuffer, int i, int i2, long j) {
        AudioCodec codec;
        if (byteBuffer != null) {
            if (i == 2) {
                this.data = ByteBuffer.allocateDirect(byteBuffer.remaining());
            } else {
                this.data = ByteBuffer.allocate(byteBuffer.remaining());
            }
            this.data.duplicate().put(byteBuffer.duplicate());
        } else {
            this.data = null;
        }
        switch (i) {
            case 1:
                codec = AudioCodec.VORBIS;
                break;
            case 2:
                codec = AudioCodec.OPUS;
                break;
            default:
                codec = AudioCodec.UNDEFINED;
                break;
        }
        this.codec = codec;
        this.sampleRate = i2;
        this.duration = j;
    }

    public long getDuration() {
        return this.duration;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public AudioCodec getCodec() {
        return this.codec;
    }

    @Nullable
    public ByteBuffer getData() {
        return this.data;
    }
}