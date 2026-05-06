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
package org.gagravarr.vorbis;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioSetupHeader;

/**
 * Includes extensive CODEC setup information as well as the 
 *  complete VQ and Huffman codebooks needed for decode
 */
public class VorbisSetup extends HighLevelOggStreamPacket implements VorbisPacket, OggAudioSetupHeader {
    public VorbisSetup(OggPacket pkt) {
        super(pkt);

        // Made up of:
        //  Codebooks
        //  Time Domain Transforms
        //  Floors
        //  Residues
        //  Mappings
        //  Modes
    }
    public VorbisSetup() {
        super();
    }

    public int getHeaderSize() {
        return HEADER_LENGTH_METADATA;
    }
    public void populateMetadataHeader(byte[] b, int dataLength) {
        VorbisPacketFactory.populateMetadataHeader(b, TYPE_SETUP, dataLength);
    }

    // Example first bit of decoding
    public int getNumberOfCodebooks() {
        byte[] data = getData();
        int number = -1;
        if(data != null && data.length >= 10) {
            number = IOUtils.toInt(data[8]);
        }
        return (number+1);
    }
}
