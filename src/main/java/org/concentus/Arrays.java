/* Copyright (c) 2016 Logan Stromberg

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

class Arrays {

    static int[][] InitTwoDimensionalArrayInt(int x, int y) {
        return new int[x][y];
    }

    static float[][] InitTwoDimensionalArrayFloat(int x, int y) {
        return new float[x][y];
    }

    static short[][] InitTwoDimensionalArrayShort(int x, int y) {
        return new short[x][y];
    }

    static byte[][] InitTwoDimensionalArrayByte(int x, int y) {
        return new byte[x][y];
    }

    static byte[][][] InitThreeDimensionalArrayByte(int x, int y, int z) {
        return new byte[x][y][z];
    }

    static void MemSet(byte[] array, byte value) {
        java.util.Arrays.fill(array, value);
    }

    static void MemSet(short[] array, short value) {
        java.util.Arrays.fill(array, value);
    }

    static void MemSet(int[] array, int value) {
        java.util.Arrays.fill(array, value);
    }

    static void MemSet(float[] array, float value) {
        java.util.Arrays.fill(array, value);
    }

    static void MemSet(byte[] array, byte value, int length) {
        java.util.Arrays.fill(array, 0, length, value);
    }

    static void MemSet(short[] array, short value, int length) {
        java.util.Arrays.fill(array, 0, length, value);
    }

    static void MemSet(int[] array, int value, int length) {
        java.util.Arrays.fill(array, 0, length, value);
    }

    static void MemSet(float[] array, float value, int length) {
        java.util.Arrays.fill(array, 0, length, value);
    }

    static void MemSetWithOffset(byte[] array, byte value, int offset, int length) {
        java.util.Arrays.fill(array, offset, offset + length, value);
    }

    static void MemSetWithOffset(short[] array, short value, int offset, int length) {
        java.util.Arrays.fill(array, offset, offset + length, value);
    }

    static void MemSetWithOffset(int[] array, int value, int offset, int length) {
        java.util.Arrays.fill(array, offset, offset + length, value);
    }

    static void MemMove(byte[] array, int src_idx, int dst_idx, int length) {
        System.arraycopy(array, src_idx, array, dst_idx, length);
    }

    static void MemMove(short[] array, int src_idx, int dst_idx, int length) {
        System.arraycopy(array, src_idx, array, dst_idx, length);
    }

    static void MemMove(int[] array, int src_idx, int dst_idx, int length) {
        System.arraycopy(array, src_idx, array, dst_idx, length);
    }

}
