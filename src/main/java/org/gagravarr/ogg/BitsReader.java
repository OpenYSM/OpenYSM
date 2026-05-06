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

import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for reading (and hopefully later writing)
 *  a stream of arbitrary bits, in big endian encoding, eg 
 *  "give me the next 3 bits" or "give me bits to the 
 *  byte boundary"
 */
public class BitsReader {
    private InputStream input;
    
    private int tmp = 0;
    private int remaining = 0;
    private boolean eof = false;
    
    public BitsReader(InputStream input) {
        this.input = input;
    }
    
    public int read(int numBits) throws IOException {
        int res = 0;
        while (numBits > 0 && !eof) {
            if (remaining == 0) {
                tmp = input.read();
                if (tmp == -1) {
                    eof = true;
                    return -1;
                }
                remaining = 8;
            }
            int toNibble = Math.min(remaining, numBits);
            int toLeave = (remaining-toNibble);
            int leaveMask = (1<<toLeave)-1;

            res = res << toNibble;
            res += (tmp>>toLeave);
            tmp = tmp & leaveMask;

            remaining -= toNibble;
            numBits -= toNibble;
        }
        if (eof) return -1;
        return res;
    }
    
    /**
     * Counts the number of bits until the next zero (false)
     *  bit is set
     * <p>eg 1110 is 3, 0 is 0, 10 is 1.
     * @return the number of bits until the next zero
     */
    public int bitsToNextZero() throws IOException {
        int count = 0;
        while (read(1) == 1) {
            count++;
        }
        return count;
    }
    /**
     * Counts the number of bits until the next one (true)
     *  bit is set
     * <p>eg b1 is 0, b001 is 2, b0000001 is 6
     * @return the number of bits until the next one
     */
    public int bitsToNextOne() throws IOException {
        int count = 0;
        while (read(1) == 0) {
            count++;
        }
        return count;
    }
    
    /**
     * Reads the number to the next byte boundary,
     *  or -1 if already there.
     */
    public int readToByteBoundary() throws IOException {
        if (remaining == 0) return -1;
        return read(remaining);
    }

    /**
     * Has the End-Of-File / End-of-Stream been hit?
     */
    public boolean isEOF() {
        return eof;
    }
}
