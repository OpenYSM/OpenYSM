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
package org.gagravarr.theora;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.OggPacket;

/**
 * The setup includes the limit values for the loop filter, the setup
 *  details for the dequantization tables, and the DCT unpacking
 *  Huffman tables
 */
public class TheoraSetup extends HighLevelOggStreamPacket implements TheoraPacket {
    public TheoraSetup() {
        super();
    }

    public TheoraSetup(OggPacket pkt) {
        super(pkt);

        // Made up of:
        //  Loop Filter Table limits
        //  Quantization Parameters
        //  Quantization Matrix
        //  DCT Token Huffman Tables
        // Note - this data isn't octet aligned
    }
}
