/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gagravarr.flac;

import java.io.IOException;
import java.io.OutputStream;

import org.gagravarr.ogg.IOUtils;

/**
 * The Stream Info metadata block holds useful
 *  information on the audio data of the file
 */
public class FlacInfo extends FlacMetadataBlock {
    /**
     * <16> The minimum block size (in samples) used in the stream. 
     */
    private int minimumBlockSize;
    /**
     * <16> The maximum block size (in samples) used in the stream. (Minimum blocksize == maximum blocksize) implies a fixed-blocksize stream.
     */
    private int maximumBlockSize;
    /**
     * <24> The minimum frame size (in bytes) used in the stream. May be 0 to imply the value is not known.
     */
    private int minimumFrameSize;
    /**
     * <24> The maximum frame size (in bytes) used in the stream. May be 0 to imply the value is not known.
     */
    private int maximumFrameSize;
    /**
     * <20> Sample rate in Hz. Though 20 bits are available, the maximum sample
     *  rate is limited by the structure of frame headers to 655350Hz. 
     *  Also, a value of 0 is invalid.
     */
    private int sampleRate;
    /**
     * <3> (number of channels)-1. FLAC supports from 1 to 8 channels
     */
    private int numChannels;
    /**
     * <5> (bits per sample)-1. FLAC supports from 4 to 32 bits per sample. 
     * Currently the reference encoder and decoders only support up to 
     *  24 bits per sample.
     */
    private int bitsPerSample;
    /**
     * <36> Total samples in stream. 'Samples' means inter-channel sample, 
     *  i.e. one second of 44.1Khz audio will have 44100 samples regardless 
     *  of the number of channels. 
     * A value of zero here means the number of total samples is unknown.
     */
    private long numberOfSamples;

    /** 
     * <128> MD5 signature of the unencoded audio data. 
     */
    private byte[] signature;

    /**
     * Creates a new, empty info
     */
    public FlacInfo() {
        super(STREAMINFO);
        signature = new byte[16];
    }

    /**
     * Reads the Info from the specified data
     */
    public FlacInfo(byte[] data, int offset) {
        super(STREAMINFO);

        // Grab the range numbers
        minimumBlockSize = IOUtils.getIntBE(
                IOUtils.toInt(data[offset++]),
                IOUtils.toInt(data[offset++])
        );
        maximumBlockSize = IOUtils.getIntBE(
                IOUtils.toInt(data[offset++]),
                IOUtils.toInt(data[offset++])
        );
        minimumFrameSize = (int)IOUtils.getIntBE(
                IOUtils.toInt(data[offset++]),
                IOUtils.toInt(data[offset++]),
                IOUtils.toInt(data[offset++])
        );
        maximumFrameSize = (int)IOUtils.getIntBE(
                IOUtils.toInt(data[offset++]),
                IOUtils.toInt(data[offset++]),
                IOUtils.toInt(data[offset++])
        );

        // The next bit is stored LE, bit packed
        // TODO Switch to using BitsReader to read this nicer
        int[] next = new int[8];
        for(int i=0; i<8; i++) {
            next[i] = IOUtils.toInt(data[i+offset]);
        }
        offset += 8;
        sampleRate = (next[0]<<12) + (next[1]<<4) + ((next[2]&0xf0)>>4);
        numChannels = ((next[2] & 0x0e) >> 1) + 1;
        bitsPerSample = ((next[2]&0x01)<<4) + ((next[3]&0xf0)>>4) + 1;
        numberOfSamples = ((next[3]&0x0f)<<30) + (next[4]<<24) + 
                (next[5]<<16) + (next[6]<<8) + next[7];

        // Get the signature
        signature = new byte[16];
        System.arraycopy(data, offset, signature, 0, 16);
    }

    @Override
    protected void write(OutputStream out) throws IOException {
        // Write the frame numbers
        IOUtils.writeInt2BE(out, minimumBlockSize);
        IOUtils.writeInt2BE(out, maximumBlockSize);
        IOUtils.writeInt3BE(out, minimumFrameSize);
        IOUtils.writeInt3BE(out, maximumFrameSize);

        // Write the rates/channels/samples
        // TODO
        out.write(new byte[8]);

        // Write the signature
        out.write(signature);
    }

    /**
     * The duration of the audio, in seconds.
     * Will return 0 if the encoder didn't include the
     *  number of samples in the Header, in that case use
     *  {@link FlacAudioStatistics} to re-calculate.
     */
    public double getDurationSeconds() {
       return (double)numberOfSamples / sampleRate;
    }

    /**
     * The minimum block size (in samples) used in the stream. 
     */
    public int getMinimumBlockSize() {
        return minimumBlockSize;
    }
    public void setMinimumBlockSize(int minimumBlockSize) {
        this.minimumBlockSize = minimumBlockSize;
    }

    /**
     * The maximum block size (in samples) used in the stream. 
     * (Minimum blocksize == maximum blocksize) implies a fixed-blocksize stream.
     */
    public int getMaximumBlockSize() {
        return maximumBlockSize;
    }
    public void setMaximumBlockSize(int maximumBlockSize) {
        this.maximumBlockSize = maximumBlockSize;
    }

   public int getMinimumFrameSize() {
      return minimumFrameSize;
   }
   public void setMinimumFrameSize(int minimumFrameSize) {
      this.minimumFrameSize = minimumFrameSize;
   }

   public int getMaximumFrameSize() {
      return maximumFrameSize;
   }
   public void setMaximumFrameSize(int maximumFrameSize) {
      this.maximumFrameSize = maximumFrameSize;
   }

   public int getSampleRate() {
      return sampleRate;
   }
   public void setSampleRate(int sampleRate) {
      this.sampleRate = sampleRate;
   }

   public int getNumChannels() {
      return numChannels;
   }
   public void setNumChannels(int numChannels) {
      this.numChannels = numChannels;
   }

   public int getBitsPerSample() {
      return bitsPerSample;
   }
   public void setBitsPerSample(int bitsPerSample) {
      this.bitsPerSample = bitsPerSample;
   }

   public long getNumberOfSamples() {
      return numberOfSamples;
   }
   public void setNumberOfSamples(long numberOfSamples) {
      this.numberOfSamples = numberOfSamples;
   }

   public int getPreSkip() {
       return 0;
   }

   public byte[] getSignature() {
      return signature;
   }
   public void setSignature(byte[] signature) {
      this.signature = signature;
   }
}
