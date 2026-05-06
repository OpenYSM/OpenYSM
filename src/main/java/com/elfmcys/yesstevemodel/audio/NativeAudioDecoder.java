package com.elfmcys.yesstevemodel.audio;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.concentus.OpusDecoder;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class NativeAudioDecoder {
    private boolean inputSet = false;
    private int channels;
    private OggFile oggFile;
    private OpusFile opusFile;
    private OpusDecoder opusDecoder;
    private ByteBuffer opusPcmBuffer;
    private ShortBuffer opusPcmShortBuffer;
    private short[] decodePcmArray;

    public boolean openStream(ByteBuffer input) {
        if (!input.isDirect()) {
            throw new IllegalArgumentException("input is not direct buffer");
        }

        try {
            this.oggFile = new OggFile(new ByteBufInputStream(Unpooled.wrappedBuffer(input.slice())));

            try {
                this.opusFile = new OpusFile(this.oggFile);
            } catch (IllegalArgumentException e) {
                throw new UnsupportedAudioFileException("File is not a valid Opus audio stream.");
            }

            this.channels = opusFile.getInfo().getNumChannels();
            if (this.channels != 1 && this.channels != 2) {
                throw new UnsupportedAudioFileException("Unsupported Opus channels: " + this.channels);
            }

            this.opusDecoder = new OpusDecoder(48000, this.channels);
            this.decodePcmArray = new short[5760 * this.channels];

            if (this.opusPcmBuffer == null || this.opusPcmBuffer.capacity() < this.decodePcmArray.length * 2) {
                this.opusPcmBuffer = ByteBuffer.allocateDirect(this.decodePcmArray.length * 2).order(ByteOrder.nativeOrder());
                this.opusPcmShortBuffer = this.opusPcmBuffer.asShortBuffer();
            }
            this.opusPcmBuffer.position(0);
            this.opusPcmBuffer.limit(0);

            this.inputSet = true;
            return true;

        } catch (Exception e) {
            destroyEngines();
            e.printStackTrace();
            return false;
        }
    }

    public int decodeFrame(ByteBuffer output) {
        if (!output.isDirect()) throw new IllegalArgumentException("output is not direct buffer");
        if (!inputSet || opusDecoder == null) return -1;

        try {
            output.order(ByteOrder.LITTLE_ENDIAN);
            int requestedMonoSamples = output.remaining() / 2;
            if (requestedMonoSamples <= 0) return 0;

            int monoSamplesGenerated = 0;

            while (output.remaining() >= 2) {
                if (opusPcmBuffer.hasRemaining()) {
                    if (this.channels == 2) {
                        while (opusPcmBuffer.remaining() >= 4 && output.remaining() >= 2) {
                            short left = opusPcmBuffer.getShort();
                            short right = opusPcmBuffer.getShort();
                            short mixed = (short) ((left + right) / 2);
                            output.putShort(mixed);
                            monoSamplesGenerated++;
                        }
                    } else {
                        while (opusPcmBuffer.hasRemaining() && output.remaining() >= 2) {
                            output.putShort(opusPcmBuffer.getShort());
                            monoSamplesGenerated++;
                        }
                    }
                    if (output.remaining() < 2) break;
                }

                OpusAudioData audioData = opusFile.getNextAudioPacket();
                if (audioData == null) {
                    break; // EOF
                }

                byte[] data = audioData.getData();
                if (data == null || data.length == 0) continue;

                try {
                    int decodedSamplesPerChannel = opusDecoder.decode(data, 0, data.length, decodePcmArray, 0, 5760, false);
                    if (decodedSamplesPerChannel > 0) {
                        int totalShorts = decodedSamplesPerChannel * this.channels;
                        opusPcmBuffer.clear();
                        opusPcmShortBuffer.clear();
                        opusPcmShortBuffer.put(decodePcmArray, 0, totalShorts);
                        opusPcmBuffer.position(0);
                        opusPcmBuffer.limit(totalShorts * 2);
                    }
                } catch (Exception ex) {
                    System.err.println("Opus decode error on valid payload: " + ex.getMessage());
                }
            }

            return monoSamplesGenerated * 2;

        } catch (Exception e) {
            e.printStackTrace();
            return -100;
        }
    }

    public void reset() {
        this.inputSet = false;
        destroyEngines();
    }

    public void destroy() {
        this.inputSet = false;
        destroyEngines();
    }

    private void destroyEngines() {
        try {
            if (this.opusFile != null) this.opusFile.close();
            if (this.oggFile != null) this.oggFile.close();
        } catch (Throwable ex) {ex.printStackTrace();}
        this.opusDecoder = null;
        this.opusFile = null;
        this.oggFile = null;

        if (this.opusPcmBuffer != null) {
            this.opusPcmBuffer.clear();
        }
    }
}