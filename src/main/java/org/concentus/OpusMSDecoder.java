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

public class OpusMSDecoder {

    ChannelLayout layout = new ChannelLayout();
    OpusDecoder[] decoders = null;

    private OpusMSDecoder(int nb_streams, int nb_coupled_streams) {
        decoders = new OpusDecoder[nb_streams];
        for (int c = 0; c < nb_streams; c++) {
            decoders[c] = new OpusDecoder();
        }
    }

    int opus_multistream_decoder_init(
            int Fs,
            int channels,
            int streams,
            int coupled_streams,
            short[] mapping
    ) {
        int i, ret;
        int decoder_ptr = 0;

        if ((channels > 255) || (channels < 1) || (coupled_streams > streams)
                || (streams < 1) || (coupled_streams < 0) || (streams > 255 - coupled_streams)) {
            throw new IllegalArgumentException("Invalid channel or coupled stream count");
        }

        this.layout.nb_channels = channels;
        this.layout.nb_streams = streams;
        this.layout.nb_coupled_streams = coupled_streams;

        for (i = 0; i < this.layout.nb_channels; i++) {
            this.layout.mapping[i] = mapping[i];
        }
        if (OpusMultistream.validate_layout(this.layout) == 0) {
            throw new IllegalArgumentException("Invalid surround channel layout");
        }

        for (i = 0; i < this.layout.nb_coupled_streams; i++) {
            ret = this.decoders[decoder_ptr].opus_decoder_init(Fs, 2);
            if (ret != OpusError.OPUS_OK) {
                return ret;
            }
            decoder_ptr++;
        }
        for (; i < this.layout.nb_streams; i++) {
            ret = this.decoders[decoder_ptr].opus_decoder_init(Fs, 1);
            if (ret != OpusError.OPUS_OK) {
                return ret;
            }
            decoder_ptr++;
        }
        return OpusError.OPUS_OK;
    }

    /// <summary>
    /// Creates a new MS decoder
    /// </summary>
    /// <param name="Fs"></param>
    /// <param name="channels"></param>
    /// <param name="streams"></param>
    /// <param name="coupled_streams"></param>
    /// <param name="mapping">A mapping family (just use { 0, 1, 255 })</param>
    /// <returns></returns>
    public static OpusMSDecoder create(
            int Fs,
            int channels,
            int streams,
            int coupled_streams,
            short[] mapping) throws OpusException {
        int ret;
        OpusMSDecoder st;
        if ((channels > 255) || (channels < 1) || (coupled_streams > streams)
                || (streams < 1) || (coupled_streams < 0) || (streams > 255 - coupled_streams)) {
            throw new IllegalArgumentException("Invalid channel / stream configuration");
        }
        st = new OpusMSDecoder(streams, coupled_streams);
        ret = st.opus_multistream_decoder_init(Fs, channels, streams, coupled_streams, mapping);
        if (ret != OpusError.OPUS_OK) {
            if (ret == OpusError.OPUS_BAD_ARG) {
                throw new IllegalArgumentException("Bad argument while creating MS decoder");
            }
            throw new OpusException("Could not create MS decoder", ret);
        }
        return st;
    }

    static int opus_multistream_packet_validate(byte[] data, int data_ptr,
            int len, int nb_streams, int Fs) {
        int s;
        int count;
        BoxedValueByte toc = new BoxedValueByte((byte) 0);
        short[] size = new short[48];
        int samples = 0;
        BoxedValueInt packet_offset = new BoxedValueInt(0);
        BoxedValueInt dummy = new BoxedValueInt(0);

        for (s = 0; s < nb_streams; s++) {
            int tmp_samples;
            if (len <= 0) {
                return OpusError.OPUS_INVALID_PACKET;
            }

            count = OpusPacketInfo.opus_packet_parse_impl(data, data_ptr, len, (s != nb_streams - 1) ? 1 : 0, toc, null, 0,
                    size, 0, dummy, packet_offset);
            if (count < 0) {
                return count;
            }

            tmp_samples = OpusPacketInfo.getNumSamples(data, data_ptr, packet_offset.Val, Fs);
            if (s != 0 && samples != tmp_samples) {
                return OpusError.OPUS_INVALID_PACKET;
            }
            samples = tmp_samples;
            data_ptr += packet_offset.Val;
            len -= packet_offset.Val;
        }

        return samples;
    }

    int opus_multistream_decode_native(
            byte[] data,
            int data_ptr,
            int len,
            short[] pcm,
            int pcm_ptr,
            int frame_size,
            int decode_fec,
            int soft_clip
    ) {
        int Fs;
        int s, c;
        int decoder_ptr;
        int do_plc = 0;
        short[] buf;

        /* Limit frame_size to avoid excessive stack allocations. */
        Fs = this.getSampleRate();
        frame_size = Inlines.IMIN(frame_size, Fs / 25 * 3);
        buf = new short[2 * frame_size];
        decoder_ptr = 0;

        if (len == 0) {
            do_plc = 1;
        }
        if (len < 0) {
            return OpusError.OPUS_BAD_ARG;
        }
        if (do_plc == 0 && len < 2 * this.layout.nb_streams - 1) {
            return OpusError.OPUS_INVALID_PACKET;
        }
        if (do_plc == 0) {
            int ret = opus_multistream_packet_validate(data, data_ptr, len, this.layout.nb_streams, Fs);
            if (ret < 0) {
                return ret;
            } else if (ret > frame_size) {
                return OpusError.OPUS_BUFFER_TOO_SMALL;
            }
        }
        for (s = 0; s < this.layout.nb_streams; s++) {
            OpusDecoder dec;
            int ret;

            dec = this.decoders[decoder_ptr++];

            if (do_plc == 0 && len <= 0) {
                return OpusError.OPUS_INTERNAL_ERROR;
            }
            BoxedValueInt packet_offset = new BoxedValueInt(0);
            ret = dec.opus_decode_native(
                    data, data_ptr, len, buf, 0, frame_size, decode_fec,
                    (s != this.layout.nb_streams - 1) ? 1 : 0, packet_offset, soft_clip);
            data_ptr += packet_offset.Val;
            len -= packet_offset.Val;
            if (ret <= 0) {
                return ret;
            }
            frame_size = ret;
            if (s < this.layout.nb_coupled_streams) {
                int chan, prev;
                prev = -1;
                /* Copy "left" audio to the channel(s) where it belongs */
                while ((chan = OpusMultistream.get_left_channel(this.layout, s, prev)) != -1) {
                    opus_copy_channel_out_short(pcm, pcm_ptr, this.layout.nb_channels, chan,
                            buf, 0, 2, frame_size);
                    prev = chan;
                }
                prev = -1;
                /* Copy "right" audio to the channel(s) where it belongs */
                while ((chan = OpusMultistream.get_right_channel(this.layout, s, prev)) != -1) {
                    opus_copy_channel_out_short(pcm, pcm_ptr, this.layout.nb_channels, chan,
                            buf, 1, 2, frame_size);
                    prev = chan;
                }
            } else {
                int chan, prev;
                prev = -1;
                /* Copy audio to the channel(s) where it belongs */
                while ((chan = OpusMultistream.get_mono_channel(this.layout, s, prev)) != -1) {
                    opus_copy_channel_out_short(pcm, pcm_ptr, this.layout.nb_channels, chan,
                            buf, 0, 1, frame_size);
                    prev = chan;
                }
            }
        }
        /* Handle muted channels */
        for (c = 0; c < this.layout.nb_channels; c++) {
            if (this.layout.mapping[c] == 255) {
                opus_copy_channel_out_short(pcm, pcm_ptr, this.layout.nb_channels, c,
                        null, 0, 0, frame_size);
            }
        }

        return frame_size;
    }

    static void opus_copy_channel_out_short(
            short[] dst,
            int dst_ptr,
            int dst_stride,
            int dst_channel,
            short[] src,
            int src_ptr,
            int src_stride,
            int frame_size
    ) {
        int i;
        if (src != null) {
            for (i = 0; i < frame_size; i++) {
                dst[i * dst_stride + dst_channel + dst_ptr] = src[i * src_stride + src_ptr];
            }
        } else {
            for (i = 0; i < frame_size; i++) {
                dst[i * dst_stride + dst_channel + dst_ptr] = 0;
            }
        }
    }

    public int decodeMultistream(
            byte[] data,
            int data_offset,
            int len,
            short[] out_pcm,
            int out_pcm_offset,
            int frame_size,
            int decode_fec
    ) {
        return opus_multistream_decode_native(data, data_offset, len,
                out_pcm, out_pcm_offset, frame_size, decode_fec, 0);
    }

    public OpusBandwidth getBandwidth() {
        if (decoders == null || decoders.length == 0) {
            throw new IllegalStateException("Decoder not initialized");
        }
        return decoders[0].getBandwidth();
    }

    public int getSampleRate() {
        if (decoders == null || decoders.length == 0) {
            throw new IllegalStateException("Decoder not initialized");
        }
        return decoders[0].getSampleRate();
    }

    public int getGain() {
        if (decoders == null || decoders.length == 0) {
            throw new IllegalStateException("Decoder not initialized");
        }
        return decoders[0].getGain();
    }

    public void setGain(int value) {
        for (int s = 0; s < layout.nb_streams; s++) {
            decoders[s].setGain(value);
        }
    }

    public int getLastPacketDuration() {
        if (decoders == null || decoders.length == 0) {
            return OpusError.OPUS_INVALID_STATE;
        }
        return decoders[0].getLastPacketDuration();
    }

    public int getFinalRange() {
        int value = 0;
        for (int s = 0; s < layout.nb_streams; s++) {
            value ^= decoders[s].getFinalRange();
        }
        return value;
    }

    public void ResetState() {
        for (int s = 0; s < layout.nb_streams; s++) {
            decoders[s].resetState();
        }
    }

    public OpusDecoder GetMultistreamDecoderState(int streamId) {
        return decoders[streamId];
    }
}
