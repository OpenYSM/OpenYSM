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
package org.gagravarr.ogg;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Utilities for working with IO streams, such
 *  as reading and writing.
 *  
 * Endian Note - Ogg and Vorbis tend to work in
 *  Little Endian format, while FLAC tends to 
 *  work in Big Endian format.
 */
public class IOUtils {
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    public static void readFully(InputStream inp, byte[] destination) throws IOException {
        readFully(inp, destination, 0, destination.length);
    }
    public static void readFully(InputStream inp, byte[] destination, int offset, int length) throws IOException {
        int read = 0;
        int r;
        while(read < length) {
            r = inp.read(destination, offset+read, length-read);
            if(r == -1) {
                throw new EOFException("Asked to read " + length + " bytes from " + offset + " but hit EoF at " + read);
            }
            read += r;
        }
    }


    public static int toInt(byte b) {
        if(b < 0)
            return b & 0xff;
        return b;
    }
    public static byte fromInt(int i) {
        if(i > 256) {
            throw new IllegalArgumentException("Number " + i + " too big");
        }
        if(i > 127) {
            return (byte)(i-256);
        }
        return (byte)i;
    }


    public static int readOrEOF(InputStream stream) throws IOException {
        int data = stream.read();
        if (data == -1) throw new EOFException("No data remains");
        return data;
    }


    public static int getInt2(byte[] data) {
        return getInt2(data, 0);
    }
    public static int getInt2(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        return getInt(b0, b1);
    }

    public static long getInt3(byte[] data) {
        return getInt3(data, 0);
    }
    public static long getInt3(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        return getInt(b0, b1, b2);
    }

    public static long getInt4(byte[] data) {
        return getInt4(data, 0);
    }
    public static long getInt4(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        return getInt(b0, b1, b2, b3);
    }

    public static long getInt5(byte[] data) {
        return getInt5(data, 0);
    }
    public static long getInt5(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        int b4 = data[i++] & 0xFF;
        return getInt(b0, b1, b2, b3, b4);
    }

    public static long getInt8(byte[] data) {
        return getInt8(data, 0);
    }
    public static long getInt8(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        int b4 = data[i++] & 0xFF;
        int b5 = data[i++] & 0xFF;
        int b6 = data[i++] & 0xFF;
        int b7 = data[i++] & 0xFF;
        return getInt(b0, b1, b2, b3, b4, b5, b6, b7);
    }

    public static int getInt(int i0, int i1) {
        return (i1 << 8) + (i0 << 0);
    }
    public static long getInt(int i0, int i1, int i2) {
        return (i2 << 16) + (i1 << 8) + (i0 << 0);
    }
    public static long getInt(int i0, int i1, int i2, int i3) {
        return (i3 << 24) + (i2 << 16) + (i1 << 8) + (i0 << 0);
    }
    public static long getInt(int i0, int i1, int i2, int i3, int i4) {
        return (i4 << 32) + (i3 << 24) + (i2 << 16) + (i1 << 8) + (i0 << 0);
    }
    public static long getInt(int i0, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        // Special check for all 0xff, to avoid overflowing long
        if (i0 == 255 && i1 == 255 && i3 == 255 && i4 == 255 && i5 == 255 && i6 == 255 && i7 == 255) return -1l;
        // Otherwise normal convert
        return (long)(i7 << 56) + (long)(i6 << 48) +
                (long)(i5 << 40) + (long)(i4 << 32) +
                (i3 << 24) + (i2 << 16) + (i1 << 8) + (i0 << 0);
    }


    public static int getInt2BE(byte[] data) {
        return getInt2BE(data, 0);
    }
    public static int getInt2BE(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        return getIntBE(b0, b1);
    }

    public static long getInt3BE(byte[] data) {
        return getInt3BE(data, 0);
    }
    public static long getInt3BE(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        return getIntBE(b0, b1, b2);
    }

    public static long getInt4BE(byte[] data) {
        return getInt4BE(data, 0);
    }
    public static long getInt4BE(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        return getIntBE(b0, b1, b2, b3);
    }

    public static int getIntBE(int i0, int i1) {
        return (i0 << 8) + (i1 << 0);
    }
    public static long getIntBE(int i0, int i1, int i2) {
        return (i0 << 16) + (i1 << 8) + (i2 << 0);
    }
    public static long getIntBE(int i0, int i1, int i2, int i3) {
        return (i0 << 24) + (i1 << 16) + (i2 << 8) + (i3 << 0);
    }


    public static void writeInt2(OutputStream out, int v) throws IOException {
        byte[] b2 = new byte[2];
        putInt2(b2, 0, v);
        out.write(b2);
    }
    public static void putInt2(byte[] data, int offset, int v) {
        int i = offset;
        data[i++] = (byte)((v >>>  0) & 0xFF);
        data[i++] = (byte)((v >>>  8) & 0xFF);
    }

    public static void writeInt3(OutputStream out, long v) throws IOException {
        byte[] b3 = new byte[3];
        putInt3(b3, 0, v);
        out.write(b3);
    }
    public static void putInt3(byte[] data, int offset, long v) {
        int i = offset;
        data[i++] = (byte)((v >>>  0) & 0xFF);
        data[i++] = (byte)((v >>>  8) & 0xFF);
        data[i++] = (byte)((v >>> 16) & 0xFF);
    }

    public static void writeInt4(OutputStream out, long v) throws IOException {
        byte[] b4 = new byte[4];
        putInt4(b4, 0, v);
        out.write(b4);
    }
    public static void putInt4(byte[] data, int offset, long v) {
        int i = offset;
        data[i++] = (byte)((v >>>  0) & 0xFF);
        data[i++] = (byte)((v >>>  8) & 0xFF);
        data[i++] = (byte)((v >>> 16) & 0xFF);
        data[i++] = (byte)((v >>> 24) & 0xFF);
    }

    public static void writeInt5(OutputStream out, long v) throws IOException {
        byte[] b5 = new byte[5];
        putInt5(b5, 0, v);
        out.write(b5);
    }
    public static void putInt5(byte[] data, int offset, long v) {
        int i = offset;
        data[i++] = (byte)((v >>>  0) & 0xFF);
        data[i++] = (byte)((v >>>  8) & 0xFF);
        data[i++] = (byte)((v >>> 16) & 0xFF);
        data[i++] = (byte)((v >>> 24) & 0xFF);
        data[i++] = (byte)((v >>> 32) & 0xFF);
    }

    public static void writeInt8(OutputStream out, long v) throws IOException {
        byte[] b8 = new byte[8];
        putInt8(b8, 0, v);
        out.write(b8);
    }
    public static void putInt8(byte[] data, int offset, long v) {
        int i = offset;
        data[i++] = (byte)((v >>>  0) & 0xFF);
        data[i++] = (byte)((v >>>  8) & 0xFF);
        data[i++] = (byte)((v >>> 16) & 0xFF);
        data[i++] = (byte)((v >>> 24) & 0xFF);
        data[i++] = (byte)((v >>> 32) & 0xFF);
        data[i++] = (byte)((v >>> 40) & 0xFF);
        data[i++] = (byte)((v >>> 48) & 0xFF);
        data[i++] = (byte)((v >>> 56) & 0xFF);
    }


    public static void writeInt2BE(OutputStream out, int v) throws IOException {
        byte[] b2 = new byte[2];
        putInt2BE(b2, 0, v);
        out.write(b2);
    }
    public static void putInt2BE(byte[] data, int offset, int v) {
        int i = offset;
        data[i+1] = (byte)((v >>>  0) & 0xFF);
        data[i+0] = (byte)((v >>>  8) & 0xFF);
    }

    public static void writeInt3BE(OutputStream out, long v) throws IOException {
        byte[] b3 = new byte[3];
        putInt3BE(b3, 0, v);
        out.write(b3);
    }
    public static void putInt3BE(byte[] data, int offset, long v) {
        int i = offset;
        data[i+2] = (byte)((v >>>  0) & 0xFF);
        data[i+1] = (byte)((v >>>  8) & 0xFF);
        data[i+0] = (byte)((v >>> 16) & 0xFF);
    }

    public static void writeInt4BE(OutputStream out, long v) throws IOException {
        byte[] b4 = new byte[4];
        putInt4BE(b4, 0, v);
        out.write(b4);
    }
    public static void putInt4BE(byte[] data, int offset, long v) {
        int i = offset;
        data[i+3] = (byte)((v >>>  0) & 0xFF);
        data[i+2] = (byte)((v >>>  8) & 0xFF);
        data[i+1] = (byte)((v >>> 16) & 0xFF);
        data[i+0] = (byte)((v >>> 24) & 0xFF);
    }

    /**
     * Gets the integer value that is stored in UTF-8 like fashion, in Big Endian.
     * A high bit at the start indicates continuation, count until the first
     *  zero bit to know how many continuation bytes, use the rest, then read
     *  the lower 6 bits of the continuation bytes.
     */
    public static long readUE7(InputStream stream) throws IOException {
        int i = stream.read();
        if (i<0) return -1;

        // Did it fit in 7 bits / 1 byte?
        if (i < 0b10000000) return i;

        // How many bytes did it need? Count the
        //  high bits until you reach a 0
        long v = 0;
        int extraBytes;
        if      (i < 0b11100000) extraBytes = 1;
        else if (i < 0b11110000) extraBytes = 2;
        else if (i < 0b11111000) extraBytes = 3;
        else if (i < 0b11111100) extraBytes = 4;
        else if (i < 0b11111110) extraBytes = 5;
        else                     extraBytes = 6;

        // Get the bits from the end, if any
        int firstByteBits = 6-extraBytes;
        if (firstByteBits > 0) {
           v = i & ((1<<firstByteBits)-1);
        }
        // Process the remaining bytes, lower 6 bits only
        for (int eb=0; eb<extraBytes; eb++) {
           i = stream.read();
           if (i < 0) return -1;
           if ((i & 0b10000000) != 0b10000000) return 0; // Broken encoding
           v = (v << 6) + (i&0b00111111);
        }
        return v;
    }
//   public static void writeUE7(OutputStream out, long value) throws IOException {
//       // TODO Implement
//   }

    /**
     * @param length The length in BYTES
     */
    public static String getUTF8(byte[] data, int offset, int length) {
        return new String(data, offset, length, UTF8);
    }
    /**
     * Strips off any null padding, if any, from the string
     */
    public static String removeNullPadding(String str) {
        int idx = str.indexOf(0);
        if (idx == -1) {
            return str;
        }
        return str.substring(0, idx);
    }

    /**
     * @return The length in BYTES
     */
    public static int putUTF8(byte[] data, int offset, String str) {
        byte[] s = toUTF8Bytes(str);
        System.arraycopy(s, 0, data, offset, s.length);
        return s.length;
    }
    /**
     * @return The length in BYTES
     */
    public static byte[] toUTF8Bytes(String str) {
        return str.getBytes(UTF8);
    }
    /**
     * Writes the string out as UTF-8
     */
    public static void writeUTF8(OutputStream out, String str) throws IOException {
        byte[] s = str.getBytes(UTF8);
        out.write(s);
    }
    /**
     * Writes out a 4 byte integer of the length (in bytes!) of the
     *  String, followed by the String (as UTF-8)
     */
    public static void writeUTF8WithLength(OutputStream out, String str) throws IOException {
        byte[] s = str.getBytes(UTF8);
        writeInt4(out, s.length);
        out.write(s);
    }

    /**
     * Checks to see if the wanted byte pattern is found in the
     *  within bytes from the given offset
     * @param wanted Byte sequence to look for
     * @param within Bytes to find in
     * @param withinOffset Offset to check from
     */
    public static boolean byteRangeMatches(byte[] wanted, byte[] within, int withinOffset) {
        for (int i=0; i<wanted.length; i++) {
            if (wanted[i] != within[i+withinOffset]) return false;
        }
        return true;
    }
}
