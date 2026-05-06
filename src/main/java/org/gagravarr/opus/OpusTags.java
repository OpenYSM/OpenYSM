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
package org.gagravarr.opus;

import java.io.OutputStream;

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioTagsHeader;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.vorbis.VorbisStyleComments;

/**
 * This is a {@link VorbisComments} with an Opus metadata
 *  block header, rather than the usual vorbis one.
 */
public class OpusTags extends VorbisStyleComments implements OpusPacket, OggAudioTagsHeader {
   public OpusTags(OggPacket packet) {
      super(packet, MAGIC_TAGS_BYTES.length);
      
      // Verify the type
      if (! IOUtils.byteRangeMatches(MAGIC_TAGS_BYTES, getData(), 0)) {
          throw new IllegalArgumentException("Invalid type, not a Opus Header");
      }
   }
   public OpusTags() {
      super();
   }
   
   /**
    * 8 byte OpusTags
    */
   @Override
   protected int getHeaderSize() {
      return 8;
   }
   /**
    * Opus doesn't do the framing bit if the tags are
    *  null padded.
    */
   @Override
   protected boolean hasFramingBit() {
       return false;
   }
   /**
    * Magic string
    */
   @Override
   protected void populateMetadataHeader(byte[] b, int dataLength) {
       System.arraycopy(MAGIC_TAGS_BYTES, 0, b, 0, MAGIC_TAGS_BYTES.length);
   }
   @Override
   protected void populateMetadataFooter(OutputStream out) {
       // No footer needed on Opus Tag Packets
   }
}
