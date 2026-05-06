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
package org.gagravarr.speex;

import java.io.OutputStream;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioTagsHeader;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.vorbis.VorbisStyleComments;

/**
 * This is a {@link VorbisComments} with an Speex metadata
 *  block header, rather than the usual vorbis one.
 */
public class SpeexTags extends VorbisStyleComments implements SpeexPacket, OggAudioTagsHeader {
   public SpeexTags(OggPacket packet) {
      super(packet, 0);
      
      // Verify the Packet # and Granule Position
      if (packet.getSequenceNumber() != 1 && packet.getGranulePosition() != 0) {
          throw new IllegalArgumentException("Invalid packet details, not Speex Tags");
      }
   }
   public SpeexTags() {
      super();
   }
   
   /**
    * 0 byte header
    */
   @Override
   protected int getHeaderSize() {
      return 0;
   }
   /**
    * We think that Speex doesn't do a framing bit if the 
    *  tags are null padded
    */
   @Override
   protected boolean hasFramingBit() {
       return false;
   }
   /**
    * There is no header on Speex tags
    */
   @Override
   protected void populateMetadataHeader(byte[] b, int dataLength) {
   }
   @Override
   protected void populateMetadataFooter(OutputStream out) {
       // No footer needed on Speex Tag Packets
   }
}
