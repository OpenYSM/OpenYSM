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

class CeltPitchXCorr {

    static int pitch_xcorr(
            int[] _x,
            int[] _y,
            int[] xcorr,
            int len,
            int max_pitch) {
        int i;
        int maxcorr = 1;
        Inlines.OpusAssert(max_pitch > 0);
        BoxedValueInt sum0 = new BoxedValueInt(0);
        BoxedValueInt sum1 = new BoxedValueInt(0);
        BoxedValueInt sum2 = new BoxedValueInt(0);
        BoxedValueInt sum3 = new BoxedValueInt(0);
        for (i = 0; i < max_pitch - 3; i += 4) {
            sum0.Val = 0;
            sum1.Val = 0;
            sum2.Val = 0;
            sum3.Val = 0;
            Kernels.xcorr_kernel(_x, _y, i, sum0, sum1, sum2, sum3, len);
            xcorr[i] = sum0.Val;
            xcorr[i + 1] = sum1.Val;
            xcorr[i + 2] = sum2.Val;
            xcorr[i + 3] = sum3.Val;
            sum0.Val = Inlines.MAX32(sum0.Val, sum1.Val);
            sum2.Val = Inlines.MAX32(sum2.Val, sum3.Val);
            sum0.Val = Inlines.MAX32(sum0.Val, sum2.Val);
            maxcorr = Inlines.MAX32(maxcorr, sum0.Val);
        }
        /* In case max_pitch isn't a multiple of 4, do non-unrolled version. */
        for (; i < max_pitch; i++) {
            int inner_sum = Kernels.celt_inner_prod(_x, 0, _y, i, len);
            xcorr[i] = inner_sum;
            maxcorr = Inlines.MAX32(maxcorr, inner_sum);
        }
        return maxcorr;
    }

    static int pitch_xcorr(
            short[] _x,
            int _x_ptr,
            short[] _y,
            int _y_ptr,
            int[] xcorr,
            int len,
            int max_pitch) {
        int i;
        int maxcorr = 1;
        Inlines.OpusAssert(max_pitch > 0);
        BoxedValueInt sum0 = new BoxedValueInt(0);
        BoxedValueInt sum1 = new BoxedValueInt(0);
        BoxedValueInt sum2 = new BoxedValueInt(0);
        BoxedValueInt sum3 = new BoxedValueInt(0);
        for (i = 0; i < max_pitch - 3; i += 4) {
            sum0.Val = 0;
            sum1.Val = 0;
            sum2.Val = 0;
            sum3.Val = 0;
            Kernels.xcorr_kernel(_x, _x_ptr, _y, _y_ptr + i, sum0, sum1, sum2, sum3, len);

            xcorr[i] = sum0.Val;
            xcorr[i + 1] = sum1.Val;
            xcorr[i + 2] = sum2.Val;
            xcorr[i + 3] = sum3.Val;
            sum0.Val = Inlines.MAX32(sum0.Val, sum1.Val);
            sum2.Val = Inlines.MAX32(sum2.Val, sum3.Val);
            sum0.Val = Inlines.MAX32(sum0.Val, sum2.Val);
            maxcorr = Inlines.MAX32(maxcorr, sum0.Val);
        }
        /* In case max_pitch isn't a multiple of 4, do non-unrolled version. */
        for (; i < max_pitch; i++) {
            int inner_sum = Kernels.celt_inner_prod(_x, _x_ptr, _y, _y_ptr + i, len);
            xcorr[i] = inner_sum;
            maxcorr = Inlines.MAX32(maxcorr, inner_sum);
        }
        return maxcorr;
    }

    static int pitch_xcorr(
            short[] _x,
            short[] _y,
            int[] xcorr,
            int len,
            int max_pitch) {
        int i;
        int maxcorr = 1;
        Inlines.OpusAssert(max_pitch > 0);
        BoxedValueInt sum0 = new BoxedValueInt(0);
        BoxedValueInt sum1 = new BoxedValueInt(0);
        BoxedValueInt sum2 = new BoxedValueInt(0);
        BoxedValueInt sum3 = new BoxedValueInt(0);
        for (i = 0; i < max_pitch - 3; i += 4) {
            sum0.Val = 0;
            sum1.Val = 0;
            sum2.Val = 0;
            sum3.Val = 0;
            Kernels.xcorr_kernel(_x, 0, _y, i, sum0, sum1, sum2, sum3, len);

            xcorr[i] = sum0.Val;
            xcorr[i + 1] = sum1.Val;
            xcorr[i + 2] = sum2.Val;
            xcorr[i + 3] = sum3.Val;
            sum0.Val = Inlines.MAX32(sum0.Val, sum1.Val);
            sum2.Val = Inlines.MAX32(sum2.Val, sum3.Val);
            sum0.Val = Inlines.MAX32(sum0.Val, sum2.Val);
            maxcorr = Inlines.MAX32(maxcorr, sum0.Val);
        }
        /* In case max_pitch isn't a multiple of 4, do non-unrolled version. */
        for (; i < max_pitch; i++) {
            int inner_sum = Kernels.celt_inner_prod(_x, _y, i, len);
            xcorr[i] = inner_sum;
            maxcorr = Inlines.MAX32(maxcorr, inner_sum);
        }
        return maxcorr;
    }
}
