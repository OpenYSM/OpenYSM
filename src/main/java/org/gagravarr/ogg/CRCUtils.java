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

public class CRCUtils {
    protected static final int CRC_POLYNOMIAL = 0x04c11db7;
    private static int[] CRC_TABLE = new int[256];

    static {
        int crc;
        for(int i=0; i<256; i++) {
            crc = i << 24;
            for(int j=0; j<8; j++) {
                if( (crc & 0x80000000) != 0 ) {
                    crc = ((crc << 1) ^ CRC_POLYNOMIAL);
                } else {
                    crc <<= 1;
                }
            }
            CRC_TABLE[i] = crc;
        }
    }

    public static int getCRC(byte[] data) {
        return getCRC(data, 0);
    }
    public static int getCRC(byte[] data, int previous) {
        int crc = previous;
        int a,b;

        for(int i=0; i<data.length; i++) {
            a = crc << 8;
            b = CRC_TABLE[ ((crc>>>24) & 0xff) ^ (data[i] & 0xff) ];
            crc = a ^ b;
        }

        return crc;
    }
}
