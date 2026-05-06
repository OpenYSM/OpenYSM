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

import java.io.OutputStream;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.vorbis.VorbisStyleComments;

/**
 * This is a {@link VorbisComments} with an Theora metadata
 *  block header, rather than the usual Vorbis one.
 */
public class TheoraComments extends VorbisStyleComments implements TheoraPacket {
   public TheoraComments(OggPacket packet) {
      super(packet, 7);
      
      // Verify the type
      if (getData()[0] != (byte)TYPE_COMMENTS) {
          throw new IllegalArgumentException("Invalid type, not a Theora Commetns");
      }
   }
   public TheoraComments() {
      super();
   }

   /**
    * 8 bytes - type + theora
    */
   @Override
   protected int getHeaderSize() {
      return 8;
   }
   /**
    * We think that Theora follows the Vorbis model, and has
    *  a framing bit if the comments are null-padded
    */
   @Override
   protected boolean hasFramingBit() {
       return true;
   }
   /**
    * Magic string
    */
   @Override
   protected void populateMetadataHeader(byte[] b, int dataLength) {
       TheoraPacketFactory.populateMetadataHeader(b, TYPE_COMMENTS, dataLength);
   }
   @Override
   protected void populateMetadataFooter(OutputStream out) {
       // No footer needed on Theora Comment Packets
   }
}
