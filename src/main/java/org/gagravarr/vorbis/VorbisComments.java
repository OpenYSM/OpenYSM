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

import java.io.IOException;
import java.io.OutputStream;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioTagsHeader;

/**
 * Holds encoder information and user specified tags
 */
public class VorbisComments extends VorbisStyleComments implements VorbisPacket, OggAudioTagsHeader {
    public VorbisComments(OggPacket pkt) {
        super(pkt, HEADER_LENGTH_METADATA);
    }
    public VorbisComments() {
        super();
    }
    
    /**
     * Vorbis Comments have framing bits if there's padding
     *  after the end of the defined comments
     */
    @Override
    protected boolean hasFramingBit() {
        return true;
    }
    @Override
    public int getHeaderSize() {
        return HEADER_LENGTH_METADATA;
    }
    @Override
    public void populateMetadataHeader(byte[] b, int dataLength) {
        VorbisPacketFactory.populateMetadataHeader(b, TYPE_COMMENTS, dataLength);
    }
    @Override
    public void populateMetadataFooter(OutputStream out) {
        // Vorbis requires a single framing bit at the end
        try {
            out.write(1);
        } catch (IOException e) {
            // Shouldn't happen here!
            throw new RuntimeException(e);
        }
    }
}
