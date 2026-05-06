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
package org.gagravarr.flac;

import java.io.IOException;
import java.io.OutputStream;

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioTagsHeader;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.vorbis.VorbisStyleComments;

/**
 * This is a {@link VorbisComments} with a Flac metadata
 *  block header, rather than the usual vorbis one.
 */
public class FlacTags extends VorbisStyleComments implements OggAudioTagsHeader {
   public FlacTags(OggPacket packet) {
      super(packet, 4);
      
      // Verify the type
      byte type = getData()[0];
      if(type != FlacMetadataBlock.VORBIS_COMMENT) {
         throw new IllegalArgumentException("Invalid type " + type);
      }
   }
   public FlacTags() {
      super();
   }
   
   /**
    * Type plus three byte length
    */
   @Override
   public int getHeaderSize() {
      return 4;
   }
   /**
    * Flac doesn't do the framing bit if the tags are
    *  null padded.
    */
   @Override
   protected boolean hasFramingBit() {
       return false;
   }
   /**
    * Type plus three byte length
    */
   @Override
   public void populateMetadataHeader(byte[] b, int dataLength) {
      b[0] = FlacMetadataBlock.VORBIS_COMMENT;
      IOUtils.putInt3BE(b, 1, dataLength);
   }
   @Override
   protected void populateMetadataFooter(OutputStream out) {
       // No footer needed on FLAC Tag Packets
   }
   
   protected static class FlacTagsAsMetadata extends FlacMetadataBlock {
      private FlacTags tags;

      public FlacTagsAsMetadata(byte type, byte[] data) {
         super(type);
         // This is the only metadata which needs the type
         //  and length in addition to the main data
         byte[] d = new byte[data.length+4];
         d[0] = FlacMetadataBlock.VORBIS_COMMENT;
         System.arraycopy(data, 0, d, 4, data.length);
         this.tags = new FlacTags(new OggPacket(d));
      }

      @Override
      public byte[] getData() {
         return tags.getData();
      }
      
      @Override
      protected void write(OutputStream out) throws IOException {
         throw new IllegalStateException("Must not call directly");
      }

      public FlacTags getTags() {
         return tags;
      }
   }
}
