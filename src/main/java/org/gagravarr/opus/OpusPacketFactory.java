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

import static org.gagravarr.opus.OpusPacket.MAGIC_HEADER_BYTES;
import static org.gagravarr.opus.OpusPacket.MAGIC_TAGS_BYTES;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * Identifies the right kind of {@link OpusPacket} for a given
 *  incoming {@link OggPacket}, and creates it
 */
public class OpusPacketFactory extends HighLevelOggStreamPacket {
   /**
    * Does this packet (the first in the stream) contain
    *  the magic string indicating that it's an opus
    *  one?
    */
   public static boolean isOpusStream(OggPacket firstPacket) {
       if(! firstPacket.isBeginningOfStream()) {
           return false;
       }
       return isOpusSpecial(firstPacket);
   }

   protected static boolean isOpusSpecial(OggPacket packet) {
       byte[] d = packet.getData();
       
       // Is it an Opus Header or Tags packet?
       if (d.length < 12) return false;
       if (IOUtils.byteRangeMatches(MAGIC_HEADER_BYTES, d, 0)) return true;
       if (IOUtils.byteRangeMatches(MAGIC_TAGS_BYTES, d, 0)) return true;
       
       // Not a known Opus special packet
       return false;
   }

   /**
    * Creates the appropriate {@link OpusPacket}
    *  instance based on the type.
    */
   public static OpusPacket create(OggPacket packet) {
       // Special header types detection
       if(isOpusSpecial(packet)) {
           byte type = packet.getData()[4];
           switch(type) {
           case (byte)'H': // OpusHead
               return new OpusInfo(packet);
           case (byte)'T': // OpusTags
               return new OpusTags(packet);
           }
       }

       return new OpusAudioData(packet);
   }
}