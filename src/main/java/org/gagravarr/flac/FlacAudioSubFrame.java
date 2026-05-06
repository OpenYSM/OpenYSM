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

import org.gagravarr.ogg.BitsReader;
import org.gagravarr.flac.FlacAudioFrame.ChannelType;

/**
 * Per-channel, compressed audio
 */
public abstract class FlacAudioSubFrame {
    public static FlacAudioSubFrame create(int type, int channelNumber, int wastedBits,
                                           FlacAudioFrame audioFrame, BitsReader data) throws IOException {
        // Sanity check
        if (type < 0 || type >= 64) {
            throw new IllegalArgumentException("Type must be a un-signed 6 bit number, found " + type);
        }

        // Create the right type
        if (SubFrameConstant.matchesType(type))
            return new SubFrameConstant(channelNumber, wastedBits, audioFrame, data);
        else if (SubFrameVerbatim.matchesType(type))
            return new SubFrameVerbatim(channelNumber, wastedBits, audioFrame, data);
        else if (SubFrameFixed.matchesType(type))
            return new SubFrameFixed(type, channelNumber, wastedBits, audioFrame, data);
        else if (SubFrameLPC.matchesType(type))
            return new SubFrameLPC(type, channelNumber, wastedBits, audioFrame, data);
        else 
           return new SubFrameReserved(wastedBits, audioFrame);
    }

    protected final FlacAudioFrame audioFrame;
    protected final int predictorOrder;
    protected final int sampleSizeBits;
    protected final int blockSize;
    private int wastedBits;

    protected FlacAudioSubFrame(int predictorOrder, int channelNumber, int wastedBits, FlacAudioFrame audioFrame) {
        this.predictorOrder = predictorOrder;
        this.wastedBits = wastedBits;
        this.audioFrame = audioFrame;
        this.blockSize = audioFrame.getBlockSize();

        // Adjust the sample size for any "wasted bits", which were
        //  excluded before the samples were written
        int sampleSizeBits = audioFrame.getBitsPerSample();
        sampleSizeBits -= wastedBits;

        // Adjust sample size for channel number, if needed
        // TODO Is this the right adjustment amount?
        if (audioFrame.getChannelType() == ChannelType.LEFT && channelNumber == 1) {
            sampleSizeBits++;
        }
        if (audioFrame.getChannelType() == ChannelType.RIGHT && channelNumber == 0) {
            sampleSizeBits++;
        }
        if (audioFrame.getChannelType() == ChannelType.MID && channelNumber == 1) {
            sampleSizeBits++;
        }
        this.sampleSizeBits = sampleSizeBits;
    }

    /**
     * The number of "wasted bits" per sample. 
     * This is where at least that many least-significant bits of all samples
     *  in the subframe are zero, and so samples are stored without them.
     * Audio decoding requires left-shifting by this number of bits to add
     *  back the padding!
     */
    public int getWastedBits() {
        return wastedBits;
    }
    public int getPredictorOrder() {
        return predictorOrder;
    }
    public int getSampleSizeBits() {
        return sampleSizeBits;
    }
    public int getBlockSize() {
        return blockSize;
    }
    public abstract String getType();

    public static class SubFrameConstant extends FlacAudioSubFrame {
        protected SubFrameConstant(int channelNumber, int wastedBits, FlacAudioFrame audioFrame,
                                   BitsReader data) throws IOException {
            super(-1, channelNumber, wastedBits, audioFrame);
            // TODO Capture the real value, adjust for wasted bits,
            //       and make available as the subframe samples
            data.read(sampleSizeBits);
        }
        public static boolean matchesType(final int type) {
            if (type == 0) return true;
            return false;
        }
        public String getType() { return "CONSTANT"; }
    }
    public static class SubFrameVerbatim extends FlacAudioSubFrame {
        protected SubFrameVerbatim(int channelNumber, int wastedBits, FlacAudioFrame audioFrame,
                                   BitsReader data) throws IOException {
            super(-1, channelNumber, wastedBits, audioFrame);
            for (int i=0; i<blockSize; i++) {
               // TODO Capture the values, adjust for wasted bits,
               //       and make the samples available
                data.read(sampleSizeBits);
            }
        }
        public static boolean matchesType(final int type) {
            if (type == 1) return true;
            return false;
        }
        public String getType() { return "VERBATIM"; }
    }

    public static class SubFrameWithResidual extends FlacAudioSubFrame {
        protected int[] warmUpSamples; // TODO Adjust for wasted bits
        protected SubFrameResidual residual;
        protected SubFrameWithResidual(int predictorOrder, int channelNumber, 
                                       int wastedBits, FlacAudioFrame audioFrame) {
            super(predictorOrder, channelNumber, wastedBits, audioFrame);
        }

        public SubFrameResidual getResidual() {
            return residual;
        }
        public int[] getWarmUpSamples() {
            return warmUpSamples;
        }
        public String getType() { return "UNKNOWN"; }
    }
    public static class SubFrameFixed extends SubFrameWithResidual {
        protected SubFrameFixed(int type, int channelNumber, int wastedBits, FlacAudioFrame audioFrame,
                                BitsReader data) throws IOException {
            super((type & 7), channelNumber, wastedBits, audioFrame);

            warmUpSamples = new int[predictorOrder];
            for (int i=0; i<predictorOrder; i++) {
                warmUpSamples[i] = data.read(sampleSizeBits); // TODO Adjust for wasted bits
            }

            residual = createResidual(data);
        }
        public static boolean matchesType(final int type) {
            if (type >= 8  && type <= 15) return true;
            return false;
        }
        public String getType() { return "FIXED"; }
    }
    public static class SubFrameLPC extends SubFrameWithResidual {
        protected final int linearPredictorCoefficientPrecision;
        protected final int linearPredictorCoefficientShift;

        protected final int[] coefficients;

        protected SubFrameLPC(int type, int channelNumber, int wastedBits, FlacAudioFrame audioFrame,
                              BitsReader data) throws IOException {
            super((type & 31) + 1, channelNumber, wastedBits, audioFrame);

            warmUpSamples = new int[predictorOrder];
            for (int i=0; i<predictorOrder; i++) {
                warmUpSamples[i] = data.read(sampleSizeBits); // TODO Adjust for wasted bits
            }

            this.linearPredictorCoefficientPrecision = data.read(4)+1;
            this.linearPredictorCoefficientShift = data.read(5);

            coefficients = new int[predictorOrder];
            for (int i=0; i<predictorOrder; i++) {
                coefficients[i] = data.read(linearPredictorCoefficientPrecision);
            }

            residual = createResidual(data);
        }
        public static boolean matchesType(final int type) {
            if (type >= 32) return true;
            return false;
        }

        public int getLinearPredictorCoefficientPrecision() {
            return linearPredictorCoefficientPrecision;
        }
        public int getLinearPredictorCoefficientShift() {
            return linearPredictorCoefficientShift;
        }
        public int[] getCoefficients() {
            return coefficients;
        }
        public String getType() { return "LPC"; }
    }
    public static class SubFrameReserved extends FlacAudioSubFrame {
        public static boolean matchesType(final int type) {
            if (type >= 2  && type <= 7) return true;
            if (type >= 16 && type <= 31) return true;
            return false;
        }
        private SubFrameReserved(int wastedBits, FlacAudioFrame audioFrame) {
            super(-1, -1, wastedBits, audioFrame);
        }
        public String getType() { return "RESERVED"; }
    }

    protected SubFrameResidual createResidual(BitsReader data) throws IOException {
        int type = data.read(2);
        if (type > 1) {
            // Un-supported / reserved type
            return null;
        }

        int partitionOrder = data.read(4);
        if (type == 0) {
            return new SubFrameResidualRice(partitionOrder, data);
        } else {
            return new SubFrameResidualRice2(partitionOrder, data);
        }
    }

    public class SubFrameResidual {
        protected final int partitionOrder;
        protected final int numPartitions;
        protected final int[] riceParams;

        private SubFrameResidual(int partitionOrder, int bits, int escapeCode, BitsReader data) throws IOException {
            this.partitionOrder = partitionOrder;
            numPartitions = 1<<partitionOrder;
            riceParams = new int[numPartitions];

            int numSamples = 0;
            if (partitionOrder > 0) {
                numSamples = blockSize >> partitionOrder;
            } else {
                numSamples = blockSize - predictorOrder;
            }

            for (int pn=0; pn<numPartitions; pn++) {
                int riceParam = data.read(bits);

                int partitionSamples = 0;
                if (partitionOrder == 0 || pn > 0) {
                    partitionSamples = numSamples;
                } else {
                    partitionSamples = numSamples - predictorOrder;
                }

                if (riceParam == escapeCode) {
                    // Partition holds un-encoded binary form
                    riceParam = data.read(5);
                    for (int i=0; i<partitionSamples; i++) {
                        data.read(riceParam);
                    }
                } else {
                    // Partition holds Rice encoded data
                    for (int sn=0; sn<partitionSamples; sn++) {
                        // Q value stored as zero-based unary
                        data.bitsToNextOne();
                        // R value stored as truncated binary
                        data.read(riceParam);
                    }
                }

                // Record the Rice Parameter for use in unit tests etc
                riceParams[pn] = riceParam;
            }
        }

        public int getPartitionOrder() {
            return partitionOrder;
        }
        public int getNumPartitions() {
            return numPartitions;
        }
        public int[] getRiceParams() {
            return riceParams;
        }
        public String getType() { return "UNKNOWN"; }
    }
    public class SubFrameResidualRice extends SubFrameResidual {
        private static final int PARAM_BITS = 4;
        private static final int ESCAPE_CODE = 15;
        public SubFrameResidualRice(int partitionOrder, BitsReader data) throws IOException {
            super(partitionOrder, PARAM_BITS, ESCAPE_CODE, data);
        }
        public String getType() { return "RICE"; }
    }
    public class SubFrameResidualRice2 extends SubFrameResidual {
        private static final int PARAM_BITS = 5;
        private static final int ESCAPE_CODE = 31;
        public SubFrameResidualRice2(int partitionOrder, BitsReader data) throws IOException {
            super(partitionOrder, PARAM_BITS, ESCAPE_CODE, data);
        }
        public String getType() { return "RICE2"; }
    }
}
