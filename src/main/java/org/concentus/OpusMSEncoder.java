/* Copyright (c) 2007-2008 CSIRO
   Copyright (c) 2007-2011 Xiph.Org Foundation
   Originally written by Jean-Marc Valin, Gregory Maxwell, Koen Vos,
   Timothy B. Terriberry, and the Opus open-source contributors
   Ported to Java by Logan Stromberg

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:

   - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   - Neither the name of Internet Society, IETF or IETF Trust, nor the
   names of specific contributors, may be used to endorse or promote
   products derived from this software without specific prior written
   permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
   OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.concentus;

public class OpusMSEncoder {

    final ChannelLayout layout = new ChannelLayout();
    int lfe_stream = 0;
    OpusApplication application = OpusApplication.OPUS_APPLICATION_AUDIO;
    OpusFramesize variable_duration = OpusFramesize.OPUS_FRAMESIZE_UNKNOWN;
    int surround = 0;
    int bitrate_bps = 0;
    final float[] subframe_mem = new float[3];
    OpusEncoder[] encoders = null;
    int[] window_mem = null;
    int[] preemph_mem = null;

    private OpusMSEncoder(int nb_streams, int nb_coupled_streams) {
        if (nb_streams < 1 || nb_coupled_streams > nb_streams || nb_coupled_streams < 0) {
            throw new IllegalArgumentException("Invalid channel count in MS encoder");
        }

        encoders = new OpusEncoder[nb_streams];
        for (int c = 0; c < nb_streams; c++) {
            encoders[c] = new OpusEncoder();
        }
        // fixme is this nb_streams or nb_channels?
        window_mem = new int[nb_streams * 120];
        preemph_mem = new int[nb_streams];
    }

    public void resetState() {
        int s;
        subframe_mem[0] = subframe_mem[1] = subframe_mem[2] = 0;
        if (surround != 0) {
            Arrays.MemSet(preemph_mem, 0, layout.nb_channels);
            Arrays.MemSet(window_mem, 0, layout.nb_channels * 120);
        }
        int encoder_ptr = 0;
        for (s = 0; s < layout.nb_streams; s++) {
            OpusEncoder enc = encoders[encoder_ptr++];
            enc.resetState();
        }
    }

    static int validate_encoder_layout(ChannelLayout layout) {
        int s;
        for (s = 0; s < layout.nb_streams; s++) {
            if (s < layout.nb_coupled_streams) {
                if (OpusMultistream.get_left_channel(layout, s, -1) == -1) {
                    return 0;
                }
                if (OpusMultistream.get_right_channel(layout, s, -1) == -1) {
                    return 0;
                }
            } else if (OpusMultistream.get_mono_channel(layout, s, -1) == -1) {
                return 0;
            }
        }
        return 1;
    }

    static void channel_pos(int channels, int[] pos/*[8]*/) {
        /* Position in the mix: 0 don't mix, 1: left, 2: center, 3:right */
        if (channels == 4) {
            pos[0] = 1;
            pos[1] = 3;
            pos[2] = 1;
            pos[3] = 3;
        } else if (channels == 3 || channels == 5 || channels == 6) {
            pos[0] = 1;
            pos[1] = 2;
            pos[2] = 3;
            pos[3] = 1;
            pos[4] = 3;
            pos[5] = 0;
        } else if (channels == 7) {
            pos[0] = 1;
            pos[1] = 2;
            pos[2] = 3;
            pos[3] = 1;
            pos[4] = 3;
            pos[5] = 2;
            pos[6] = 0;
        } else if (channels == 8) {
            pos[0] = 1;
            pos[1] = 2;
            pos[2] = 3;
            pos[3] = 1;
            pos[4] = 3;
            pos[5] = 1;
            pos[6] = 3;
            pos[7] = 0;
        }
    }

    private static final int[] diff_table/*[17]*/ = {
                ((short) (0.5 + (0.5000000f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.5000000f, CeltConstants.DB_SHIFT)*/, ((short) (0.5 + (0.2924813f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.2924813f, CeltConstants.DB_SHIFT)*/, ((short) (0.5 + (0.1609640f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.1609640f, CeltConstants.DB_SHIFT)*/, ((short) (0.5 + (0.0849625f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.0849625f, CeltConstants.DB_SHIFT)*/,
                ((short) (0.5 + (0.0437314f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.0437314f, CeltConstants.DB_SHIFT)*/, ((short) (0.5 + (0.0221971f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.0221971f, CeltConstants.DB_SHIFT)*/, ((short) (0.5 + (0.0111839f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.0111839f, CeltConstants.DB_SHIFT)*/, ((short) (0.5 + (0.0056136f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.0056136f, CeltConstants.DB_SHIFT)*/,
                ((short) (0.5 + (0.0028123f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(0.0028123f, CeltConstants.DB_SHIFT)*/};

    /* Computes a rough approximation of log2(2^a + 2^b) */
    static int logSum(int a, int b) {
        int max;
        int diff;
        int frac;

        int low;
        if (a > b) {
            max = a;
            diff = Inlines.SUB32(Inlines.EXTEND32(a), Inlines.EXTEND32(b));
        } else {
            max = b;
            diff = Inlines.SUB32(Inlines.EXTEND32(b), Inlines.EXTEND32(a));
        }
        if (!(diff < ((short) (0.5 + (8.0f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(8.0f, CeltConstants.DB_SHIFT)*/)) /* inverted to catch NaNs */ {
            return max;
        }
        low = Inlines.SHR32(diff, CeltConstants.DB_SHIFT - 1);
        frac = Inlines.SHL16(diff - Inlines.SHL16(low, CeltConstants.DB_SHIFT - 1), 16 - CeltConstants.DB_SHIFT);
        return max + diff_table[low] + Inlines.MULT16_16_Q15(frac, Inlines.SUB16(diff_table[low + 1], diff_table[low]));
    }

    // fixme: test the perf of this alternate implementation
    //int logSum(int a, int b)
    //{
    //    return log2(pow(4, a) + pow(4, b)) / 2;
    //}
    static void surround_analysis(CeltMode celt_mode, short[] pcm, int pcm_ptr,
            int[] bandLogE, int[] mem, int[] preemph_mem,
            int len, int overlap, int channels, int rate
    ) {
        int c;
        int i;
        int LM;
        int[] pos = {0, 0, 0, 0, 0, 0, 0, 0};
        int upsample;
        int frame_size;
        int channel_offset;
        int[][] bandE = Arrays.InitTwoDimensionalArrayInt(1, 21);
        int[][] maskLogE = Arrays.InitTwoDimensionalArrayInt(3, 21);
        int[] input;
        short[] x;
        int[][] freq;

        upsample = CeltCommon.resampling_factor(rate);
        frame_size = len * upsample;

        for (LM = 0; LM < celt_mode.maxLM; LM++) {
            if (celt_mode.shortMdctSize << LM == frame_size) {
                break;
            }
        }

        input = new int[frame_size + overlap];
        x = new short[len];
        freq = Arrays.InitTwoDimensionalArrayInt(1, frame_size);

        channel_pos(channels, pos);

        for (c = 0; c < 3; c++) {
            for (i = 0; i < 21; i++) {
                maskLogE[c][i] = -((short) (0.5 + (28.0f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(28.0f, CeltConstants.DB_SHIFT)*/;
            }
        }

        for (c = 0; c < channels; c++) {
            System.arraycopy(mem, c * overlap, input, 0, overlap);
            opus_copy_channel_in_short(x, 0, 1, pcm, pcm_ptr, channels, c, len);
            BoxedValueInt boxed_preemph = new BoxedValueInt(preemph_mem[c]);
            CeltCommon.celt_preemphasis(x, input, overlap, frame_size, 1, upsample, celt_mode.preemph, boxed_preemph, 0);
            preemph_mem[c] = boxed_preemph.Val;

            MDCT.clt_mdct_forward(
                    celt_mode.mdct,
                    input,
                    0,
                    freq[0],
                    0,
                    celt_mode.window,
                    overlap,
                    celt_mode.maxLM - LM,
                    1);
            if (upsample != 1) {
                int bound = len;
                for (i = 0; i < bound; i++) {
                    freq[0][i] *= upsample;
                }
                for (; i < frame_size; i++) {
                    freq[0][i] = 0;
                }
            }

            Bands.compute_band_energies(celt_mode, freq, bandE, 21, 1, LM);
            QuantizeBands.amp2Log2(celt_mode, 21, 21, bandE[0], bandLogE, 21 * c, 1);
            /* Apply spreading function with -6 dB/band going up and -12 dB/band going down. */
            for (i = 1; i < 21; i++) {
                bandLogE[21 * c + i] = Inlines.MAX16(bandLogE[21 * c + i], bandLogE[21 * c + i - 1] - ((short) (0.5 + (1.0f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(1.0f, CeltConstants.DB_SHIFT)*/);
            }
            for (i = 19; i >= 0; i--) {
                bandLogE[21 * c + i] = Inlines.MAX16(bandLogE[21 * c + i], bandLogE[21 * c + i + 1] - ((short) (0.5 + (2.0f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(2.0f, CeltConstants.DB_SHIFT)*/);
            }
            if (pos[c] == 1) {
                for (i = 0; i < 21; i++) {
                    maskLogE[0][i] = logSum(maskLogE[0][i], bandLogE[21 * c + i]);
                }
            } else if (pos[c] == 3) {
                for (i = 0; i < 21; i++) {
                    maskLogE[2][i] = logSum(maskLogE[2][i], bandLogE[21 * c + i]);
                }
            } else if (pos[c] == 2) {
                for (i = 0; i < 21; i++) {
                    maskLogE[0][i] = logSum(maskLogE[0][i], bandLogE[21 * c + i] - ((short) (0.5 + (.5f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(.5f, CeltConstants.DB_SHIFT)*/);
                    maskLogE[2][i] = logSum(maskLogE[2][i], bandLogE[21 * c + i] - ((short) (0.5 + (.5f) * (((int) 1) << (CeltConstants.DB_SHIFT))))/*Inlines.QCONST16(.5f, CeltConstants.DB_SHIFT)*/);
                }
            }
            System.arraycopy(input, frame_size, mem, c * overlap, overlap);
        }
        for (i = 0; i < 21; i++) {
            maskLogE[1][i] = Inlines.MIN32(maskLogE[0][i], maskLogE[2][i]);
        }
        channel_offset = Inlines.HALF16(Inlines.celt_log2(((int) (0.5 + (2.0f) * (((int) 1) << (14))))/*Inlines.QCONST32(2.0f, 14)*/ / (channels - 1)));
        for (c = 0; c < 3; c++) {
            for (i = 0; i < 21; i++) {
                maskLogE[c][i] += channel_offset;
            }
        }

        for (c = 0; c < channels; c++) {
            int[] mask;
            if (pos[c] != 0) {
                mask = maskLogE[pos[c] - 1];
                for (i = 0; i < 21; i++) {
                    bandLogE[21 * c + i] = bandLogE[21 * c + i] - mask[i];
                }
            } else {
                for (i = 0; i < 21; i++) {
                    bandLogE[21 * c + i] = 0;
                }
            }
        }
    }

    int opus_multistream_encoder_init(
            int Fs,
            int channels,
            int streams,
            int coupled_streams,
            short[] mapping,
            OpusApplication application,
            int surround
    ) {
        int i, ret;
        int encoder_ptr;

        if ((channels > 255) || (channels < 1) || (coupled_streams > streams)
                || (streams < 1) || (coupled_streams < 0) || (streams > 255 - coupled_streams)) {
            return OpusError.OPUS_BAD_ARG;
        }

        this.layout.nb_channels = channels;
        this.layout.nb_streams = streams;
        this.layout.nb_coupled_streams = coupled_streams;
        this.subframe_mem[0] = this.subframe_mem[1] = this.subframe_mem[2] = 0;
        if (surround == 0) {
            this.lfe_stream = -1;
        }
        this.bitrate_bps = OpusConstants.OPUS_AUTO;
        this.application = application;
        this.variable_duration = OpusFramesize.OPUS_FRAMESIZE_ARG;
        for (i = 0; i < this.layout.nb_channels; i++) {
            this.layout.mapping[i] = mapping[i];
        }
        if (OpusMultistream.validate_layout(this.layout) == 0 || validate_encoder_layout(this.layout) == 0) {
            return OpusError.OPUS_BAD_ARG;
        }

        encoder_ptr = 0;

        for (i = 0; i < this.layout.nb_coupled_streams; i++) {
            ret = this.encoders[encoder_ptr].opus_init_encoder(Fs, 2, application);
            if (ret != OpusError.OPUS_OK) {
                return ret;
            }
            if (i == this.lfe_stream) {
                this.encoders[encoder_ptr].setIsLFE(true);
            }
            encoder_ptr += 1;
        }
        for (; i < this.layout.nb_streams; i++) {
            ret = this.encoders[encoder_ptr].opus_init_encoder(Fs, 1, application);
            if (i == this.lfe_stream) {
                this.encoders[encoder_ptr].setIsLFE(true);
            }
            if (ret != OpusError.OPUS_OK) {
                return ret;
            }
            encoder_ptr += 1;
        }
        if (surround != 0) {
            Arrays.MemSet(this.preemph_mem, 0, channels);
            Arrays.MemSet(this.window_mem, 0, channels * 120);
        }
        this.surround = surround;
        return OpusError.OPUS_OK;
    }

    int opus_multistream_surround_encoder_init(
            int Fs,
            int channels,
            int mapping_family,
            BoxedValueInt streams,
            BoxedValueInt coupled_streams,
            short[] mapping,
            OpusApplication application
    ) {
        streams.Val = 0;
        coupled_streams.Val = 0;
        if ((channels > 255) || (channels < 1)) {
            return OpusError.OPUS_BAD_ARG;
        }
        this.lfe_stream = -1;
        if (mapping_family == 0) {
            if (channels == 1) {
                streams.Val = 1;
                coupled_streams.Val = 0;
                mapping[0] = 0;
            } else if (channels == 2) {
                streams.Val = 1;
                coupled_streams.Val = 1;
                mapping[0] = 0;
                mapping[1] = 1;
            } else {
                return OpusError.OPUS_UNIMPLEMENTED;
            }
        } else if (mapping_family == 1 && channels <= 8 && channels >= 1) {
            int i;
            streams.Val = VorbisLayout.vorbis_mappings[channels - 1].nb_streams;
            coupled_streams.Val = VorbisLayout.vorbis_mappings[channels - 1].nb_coupled_streams;
            for (i = 0; i < channels; i++) {
                mapping[i] = VorbisLayout.vorbis_mappings[channels - 1].mapping[i];
            }
            if (channels >= 6) {
                this.lfe_stream = streams.Val - 1;
            }
        } else if (mapping_family == 255) {
            byte i;
            streams.Val = channels;
            coupled_streams.Val = 0;
            for (i = 0; i < channels; i++) {
                mapping[i] = i;
            }
        } else {
            return OpusError.OPUS_UNIMPLEMENTED;
        }
        return opus_multistream_encoder_init(Fs, channels, streams.Val, coupled_streams.Val,
                mapping, application, (channels > 2 && mapping_family == 1) ? 1 : 0);
    }

    public static OpusMSEncoder Create(
            int Fs,
            int channels,
            int streams,
            int coupled_streams,
            short[] mapping,
            OpusApplication application
    ) throws OpusException {
        int ret;
        OpusMSEncoder st;
        if ((channels > 255) || (channels < 1) || (coupled_streams > streams)
                || (streams < 1) || (coupled_streams < 0) || (streams > 255 - coupled_streams)) {
            throw new IllegalArgumentException("Invalid channel / stream configuration");
        }
        st = new OpusMSEncoder(streams, coupled_streams);
        ret = st.opus_multistream_encoder_init(Fs, channels, streams, coupled_streams, mapping, application, 0);
        if (ret != OpusError.OPUS_OK) {
            if (ret == OpusError.OPUS_BAD_ARG) {
                throw new IllegalArgumentException("OPUS_BAD_ARG when creating MS encoder");
            }
            throw new OpusException("Could not create MS encoder", ret);
        }
        return st;
    }

    static void GetStreamCount(int channels, int mapping_family, BoxedValueInt nb_streams, BoxedValueInt nb_coupled_streams) {
        if (mapping_family == 0) {
            if (channels == 1) {
                nb_streams.Val = 1;
                nb_coupled_streams.Val = 0;
            } else if (channels == 2) {
                nb_streams.Val = 1;
                nb_coupled_streams.Val = 1;
            } else {
                throw new IllegalArgumentException("More than 2 channels requires custom mappings");
            }
        } else if (mapping_family == 1 && channels <= 8 && channels >= 1) {
            nb_streams.Val = VorbisLayout.vorbis_mappings[channels - 1].nb_streams;
            nb_coupled_streams.Val = VorbisLayout.vorbis_mappings[channels - 1].nb_coupled_streams;
        } else if (mapping_family == 255) {
            nb_streams.Val = channels;
            nb_coupled_streams.Val = 0;
        } else {
            throw new IllegalArgumentException("Invalid mapping family");
        }
    }

    public static OpusMSEncoder CreateSurround(
            int Fs,
            int channels,
            int mapping_family,
            BoxedValueInt streams,
            BoxedValueInt coupled_streams,
            short[] mapping,
            OpusApplication application
    ) throws OpusException {
        int ret;
        OpusMSEncoder st;
        if ((channels > 255) || (channels < 1) || application == OpusApplication.OPUS_APPLICATION_UNIMPLEMENTED) {
            throw new IllegalArgumentException("Invalid channel count or application");
        }
        BoxedValueInt nb_streams = new BoxedValueInt(0);
        BoxedValueInt nb_coupled_streams = new BoxedValueInt(0);
        GetStreamCount(channels, mapping_family, nb_streams, nb_coupled_streams);

        st = new OpusMSEncoder(nb_streams.Val, nb_coupled_streams.Val);
        ret = st.opus_multistream_surround_encoder_init(Fs, channels, mapping_family, streams, coupled_streams, mapping, application);
        if (ret != OpusError.OPUS_OK) {
            if (ret == OpusError.OPUS_BAD_ARG) {
                throw new IllegalArgumentException("Bad argument passed to CreateSurround");
            }
            throw new OpusException("Could not create multistream encoder", ret);
        }
        return st;
    }

    int surround_rate_allocation(
            int[] out_rates,
            int frame_size
    ) {
        int i;
        int channel_rate;
        int Fs;
        OpusEncoder ptr;
        int stream_offset;
        int lfe_offset;
        int coupled_ratio;
        /* Q8 */
        int lfe_ratio;
        /* Q8 */
        int rate_sum = 0;

        ptr = this.encoders[0];
        Fs = ptr.getSampleRate();

        if (this.bitrate_bps > this.layout.nb_channels * 40000) {
            stream_offset = 20000;
        } else {
            stream_offset = this.bitrate_bps / this.layout.nb_channels / 2;
        }
        stream_offset += 60 * (Fs / frame_size - 50);
        /* We start by giving each stream (coupled or uncoupled) the same bitrate.
           This models the main saving of coupled channels over uncoupled. */
 /* The LFE stream is an exception to the above and gets fewer bits. */
        lfe_offset = 3500 + 60 * (Fs / frame_size - 50);
        /* Coupled streams get twice the mono rate after the first 20 kb/s. */
        coupled_ratio = 512;
        /* Should depend on the bitrate, for now we assume LFE gets 1/8 the bits of mono */
        lfe_ratio = 32;

        /* Compute bitrate allocation between streams */
        if (this.bitrate_bps == OpusConstants.OPUS_AUTO) {
            channel_rate = Fs + 60 * Fs / frame_size;
        } else if (this.bitrate_bps == OpusConstants.OPUS_BITRATE_MAX) {
            channel_rate = 300000;
        } else {
            int nb_lfe;
            int nb_uncoupled;
            int nb_coupled;
            int total;
            nb_lfe = (this.lfe_stream != -1) ? 1 : 0;
            nb_coupled = this.layout.nb_coupled_streams;
            nb_uncoupled = this.layout.nb_streams - nb_coupled - nb_lfe;
            total = (nb_uncoupled << 8) /* mono */
                    + coupled_ratio * nb_coupled /* stereo */
                    + nb_lfe * lfe_ratio;
            channel_rate = 256 * (this.bitrate_bps - lfe_offset * nb_lfe - stream_offset * (nb_coupled + nb_uncoupled)) / total;
        }

        for (i = 0; i < this.layout.nb_streams; i++) {
            if (i < this.layout.nb_coupled_streams) {
                out_rates[i] = stream_offset + (channel_rate * coupled_ratio >> 8);
            } else if (i != this.lfe_stream) {
                out_rates[i] = stream_offset + channel_rate;
            } else {
                out_rates[i] = lfe_offset + (channel_rate * lfe_ratio >> 8);
            }
            out_rates[i] = Inlines.IMAX(out_rates[i], 500);
            rate_sum += out_rates[i];
        }
        return rate_sum;
    }

    /* Max size in case the encoder decides to return three frames */
    private final int MS_FRAME_TMP = (3 * 1275 + 7);

    int opus_multistream_encode_native(
            short[] pcm,
            int pcm_ptr,
            int analysis_frame_size,
            byte[] data,
            int data_ptr,
            int max_data_bytes,
            int lsb_depth,
            int float_api
    ) {
        int Fs;
        int s;
        int encoder_ptr;
        int tot_size;
        short[] buf;
        int[] bandSMR;
        byte[] tmp_data = new byte[MS_FRAME_TMP];
        OpusRepacketizer rp = new OpusRepacketizer();
        int vbr;
        CeltMode celt_mode;
        int[] bitrates = new int[256];
        int[] bandLogE = new int[42];
        int[] mem = null;
        int[] preemph_mem = null;
        int frame_size;
        int rate_sum;
        int smallest_packet;

        if (this.surround != 0) {
            preemph_mem = this.preemph_mem;
            mem = this.window_mem;
        }

        encoder_ptr = 0;
        Fs = this.encoders[encoder_ptr].getSampleRate();
        vbr = this.encoders[encoder_ptr].getUseVBR() ? 1 : 0;
        celt_mode = this.encoders[encoder_ptr].GetCeltMode();

        {
            int delay_compensation;
            int channels;

            channels = this.layout.nb_streams + this.layout.nb_coupled_streams;
            delay_compensation = this.encoders[encoder_ptr].getLookahead();
            delay_compensation -= Fs / 400;
            frame_size = CodecHelpers.compute_frame_size(pcm, pcm_ptr, analysis_frame_size,
                    this.variable_duration, channels, Fs, this.bitrate_bps,
                    delay_compensation, this.subframe_mem, this.encoders[encoder_ptr].analysis.enabled);
        }

        if (400 * frame_size < Fs) {
            return OpusError.OPUS_BAD_ARG;
        }
        /* Validate frame_size before using it to allocate stack space.
           This mirrors the checks in opus_encode[_float](). */
        if (400 * frame_size != Fs && 200 * frame_size != Fs
                && 100 * frame_size != Fs && 50 * frame_size != Fs
                && 25 * frame_size != Fs && 50 * frame_size != 3 * Fs) {
            return OpusError.OPUS_BAD_ARG;
        }

        /* Smallest packet the encoder can produce. */
        smallest_packet = this.layout.nb_streams * 2 - 1;
        if (max_data_bytes < smallest_packet) {
            return OpusError.OPUS_BUFFER_TOO_SMALL;
        }
        buf = new short[2 * frame_size];

        bandSMR = new int[21 * this.layout.nb_channels];
        if (this.surround != 0) {
            surround_analysis(celt_mode, pcm, pcm_ptr, bandSMR, mem, preemph_mem, frame_size, 120, this.layout.nb_channels, Fs);
        }

        /* Compute bitrate allocation between streams (this could be a lot better) */
        rate_sum = surround_rate_allocation(bitrates, frame_size);

        if (vbr == 0) {
            if (this.bitrate_bps == OpusConstants.OPUS_AUTO) {
                max_data_bytes = Inlines.IMIN(max_data_bytes, 3 * rate_sum / (3 * 8 * Fs / frame_size));
            } else if (this.bitrate_bps != OpusConstants.OPUS_BITRATE_MAX) {
                max_data_bytes = Inlines.IMIN(max_data_bytes, Inlines.IMAX(smallest_packet,
                        3 * this.bitrate_bps / (3 * 8 * Fs / frame_size)));
            }
        }

        for (s = 0; s < this.layout.nb_streams; s++) {
            OpusEncoder enc = this.encoders[encoder_ptr];
            encoder_ptr += 1;
            enc.setBitrate(bitrates[s]);
            if (this.surround != 0) {
                int equiv_rate;
                equiv_rate = this.bitrate_bps;
                if (frame_size * 50 < Fs) {
                    equiv_rate -= 60 * (Fs / frame_size - 50) * this.layout.nb_channels;
                }
                if (equiv_rate > 10000 * this.layout.nb_channels) {
                    enc.setBandwidth(OpusBandwidth.OPUS_BANDWIDTH_FULLBAND);
                } else if (equiv_rate > 7000 * this.layout.nb_channels) {
                    enc.setBandwidth(OpusBandwidth.OPUS_BANDWIDTH_SUPERWIDEBAND);
                } else if (equiv_rate > 5000 * this.layout.nb_channels) {
                    enc.setBandwidth(OpusBandwidth.OPUS_BANDWIDTH_WIDEBAND);
                } else {
                    enc.setBandwidth(OpusBandwidth.OPUS_BANDWIDTH_NARROWBAND);
                }
                if (s < this.layout.nb_coupled_streams) {
                    /* To preserve the spatial image, force stereo CELT on coupled streams */
                    enc.setForceMode(OpusMode.MODE_CELT_ONLY);
                    enc.setForceChannels(2);
                }
            }
        }

        encoder_ptr = 0;
        /* Counting ToC */
        tot_size = 0;
        for (s = 0; s < this.layout.nb_streams; s++) {
            OpusEncoder enc;
            int len;
            int curr_max;
            int c1, c2;

            rp.Reset();
            enc = this.encoders[encoder_ptr];
            if (s < this.layout.nb_coupled_streams) {
                int i;
                int left, right;
                left = OpusMultistream.get_left_channel(this.layout, s, -1);
                right = OpusMultistream.get_right_channel(this.layout, s, -1);
                opus_copy_channel_in_short(buf, 0, 2,
                        pcm, pcm_ptr, this.layout.nb_channels, left, frame_size);
                opus_copy_channel_in_short(buf, 1, 2,
                        pcm, pcm_ptr, this.layout.nb_channels, right, frame_size);
                encoder_ptr += 1;
                if (this.surround != 0) {
                    for (i = 0; i < 21; i++) {
                        bandLogE[i] = bandSMR[21 * left + i];
                        bandLogE[21 + i] = bandSMR[21 * right + i];
                    }
                }
                c1 = left;
                c2 = right;
            } else {
                int i;
                int chan = OpusMultistream.get_mono_channel(this.layout, s, -1);
                opus_copy_channel_in_short(buf, 0, 1,
                        pcm, pcm_ptr, this.layout.nb_channels, chan, frame_size);
                encoder_ptr += 1;
                if (this.surround != 0) {
                    for (i = 0; i < 21; i++) {
                        bandLogE[i] = bandSMR[21 * chan + i];
                    }
                }
                c1 = chan;
                c2 = -1;
            }
            if (this.surround != 0) {
                enc.SetEnergyMask(bandLogE);
            }

            /* number of bytes left (+Toc) */
            curr_max = max_data_bytes - tot_size;
            /* Reserve one byte for the last stream and two for the others */
            curr_max -= Inlines.IMAX(0, 2 * (this.layout.nb_streams - s - 1) - 1);
            curr_max = Inlines.IMIN(curr_max, MS_FRAME_TMP);
            /* Repacketizer will add one or two bytes for self-delimited frames */
            if (s != this.layout.nb_streams - 1) {
                curr_max -= curr_max > 253 ? 2 : 1;
            }
            if (vbr == 0 && s == this.layout.nb_streams - 1) {
                enc.setBitrate(curr_max * (8 * Fs / frame_size));
            }
            len = enc.opus_encode_native(buf, 0, frame_size, tmp_data, 0, curr_max, lsb_depth,
                    pcm, pcm_ptr, analysis_frame_size, c1, c2, this.layout.nb_channels, float_api);
            if (len < 0) {
                return len;
            }
            /* We need to use the repacketizer to add the self-delimiting lengths
               while taking into account the fact that the encoder can now return
               more than one frame at a time (e.g. 60 ms CELT-only) */
            rp.addPacket(tmp_data, 0, len);
            len = rp.opus_repacketizer_out_range_impl(0, rp.getNumFrames(),
                    data, data_ptr, max_data_bytes - tot_size, (s != this.layout.nb_streams - 1) ? 1 : 0, (vbr == 0 && s == this.layout.nb_streams - 1) ? 1 : 0);
            data_ptr += len;
            tot_size += len;
        }

        return tot_size;
    }

    static void opus_copy_channel_in_short(
            short[] dst,
            int dst_ptr,
            int dst_stride,
            short[] src,
            int src_ptr,
            int src_stride,
            int src_channel,
            int frame_size
    ) {
        int i;
        for (i = 0; i < frame_size; i++) {
            dst[dst_ptr + i * dst_stride] = src[i * src_stride + src_channel + src_ptr];
        }
    }

    public int encodeMultistream(
            short[] pcm,
            int pcm_offset,
            int frame_size,
            byte[] outputBuffer,
            int outputBuffer_offset,
            int max_data_bytes
    ) {
        // todo: catch error codes here
        return opus_multistream_encode_native(pcm, pcm_offset, frame_size, outputBuffer, outputBuffer_offset, max_data_bytes, 16, 0);
    }

    public int getBitrate() {
        int s;
        int value = 0;
        int encoder_ptr = 0;
        for (s = 0; s < layout.nb_streams; s++) {
            OpusEncoder enc = encoders[encoder_ptr++];
            value += enc.getBitrate();
        }
        return value;
    }

    public void setBitrate(int value) {
        if (value < 0 && value != OpusConstants.OPUS_AUTO && value != OpusConstants.OPUS_BITRATE_MAX) {
            throw new IllegalArgumentException("Invalid bitrate");
        }
        bitrate_bps = value;
    }

    public OpusApplication getApplication() {
        return encoders[0].getApplication();
    }

    public void setApplication(OpusApplication value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setApplication(value);
        }
    }

    public int getForceChannels() {
        return encoders[0].getForceChannels();
    }

    public void setForceChannels(int value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setForceChannels(value);
        }
    }

    public OpusBandwidth getMaxBandwidth() {
        return encoders[0].getMaxBandwidth();
    }

    public void setMaxBandwidth(OpusBandwidth value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setMaxBandwidth(value);
        }
    }

    public OpusBandwidth getBandwidth() {
        return encoders[0].getBandwidth();
    }

    public void setBandwidth(OpusBandwidth value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setBandwidth(value);
        }
    }

    public boolean getUseDTX() {
        return encoders[0].getUseDTX();
    }

    public void setUseDTX(boolean value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setUseDTX(value);
        }
    }

    public int getComplexity() {
        return encoders[0].getComplexity();
    }

    public void setComplexity(int value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setComplexity(value);
        }
    }

    public OpusMode getForceMode() {
        return encoders[0].getForceMode();
    }

    public void setForceMode(OpusMode value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setForceMode(value);
        }
    }

    public boolean getUseInbandFEC() {
        return encoders[0].getUseInbandFEC();
    }

    public void setUseInbandFEC(boolean value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setUseInbandFEC(value);
        }
    }

    public int getPacketLossPercent() {
        return encoders[0].getPacketLossPercent();
    }

    public void setPacketLossPercent(int value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setPacketLossPercent(value);
        }
    }

    public boolean getUseVBR() {
        return encoders[0].getUseVBR();
    }

    public void setUseVBR(boolean value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setUseVBR(value);
        }
    }

    public boolean getUseConstrainedVBR() {
        return encoders[0].getUseConstrainedVBR();
    }

    public void setUseConstrainedVBR(boolean value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setUseConstrainedVBR(value);
        }
    }

    public OpusSignal getSignalType() {
        return encoders[0].getSignalType();
    }

    public void setSignalType(OpusSignal value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setSignalType(value);
        }
    }

    public int getLookahead() {
        return encoders[0].getLookahead();
    }

    public int getSampleRate() {
        return encoders[0].getSampleRate();
    }

    public int getFinalRange() {
        int s;
        int value = 0;
        int encoder_ptr = 0;
        for (s = 0; s < layout.nb_streams; s++) {
            value ^= encoders[encoder_ptr++].getFinalRange();
        }
        return value;
    }

    public int getLSBDepth() {
        return encoders[0].getLSBDepth();
    }

    public void setLSBDepth(int value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setLSBDepth(value);
        }
    }

    public boolean getPredictionDisabled() {
        return encoders[0].getPredictionDisabled();
    }

    public void setPredictionDisabled(boolean value) {
        for (int encoder_ptr = 0; encoder_ptr < layout.nb_streams; encoder_ptr++) {
            encoders[encoder_ptr].setPredictionDisabled(value);
        }
    }

    public OpusFramesize getExpertFrameDuration() {
        return variable_duration;
    }

    public void setExpertFrameDuration(OpusFramesize value) {
        variable_duration = value;
    }

    public OpusEncoder getMultistreamEncoderState(int streamId) {
        if (streamId >= layout.nb_streams) {
            throw new IllegalArgumentException("Requested stream doesn't exist");
        }
        return encoders[streamId];
    }
}
