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

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * Identifies the right kind of {@link SpeexPacket} for a given
 *  incoming {@link OggPacket}, and creates it
 */
public class SpeexPacketFactory extends HighLevelOggStreamPacket {
   /**
    * Does this packet (the first in the stream) contain
    *  the magic string indicating that it's an speex
    *  one?
    */
   public static boolean isSpeexStream(OggPacket firstPacket) {
       if(! firstPacket.isBeginningOfStream()) {
           return false;
       }
       return isSpeexSpecial(firstPacket);
   }

   protected static boolean isSpeexSpecial(OggPacket packet) {
       byte[] d = packet.getData();
       
       // Is it a Speex Info packet?
       if (d.length < 72) return false;
       if (IOUtils.byteRangeMatches(SpeexPacket.MAGIC_HEADER_BYTES, d, 0)) return true;
       
       // Not a known Speex special packet
       return false;
   }

   /**
    * Creates the appropriate {@link SpeexPacket}
    *  instance based on the type.
    */
   public static SpeexPacket create(OggPacket packet) {
       // Special header types detection
       if(isSpeexSpecial(packet)) {
           return new SpeexInfo(packet);
       }
       if (packet.getSequenceNumber() == 1 && packet.getGranulePosition() == 0) {
           return new SpeexTags(packet);
       }

       return new SpeexAudioData(packet);
   }
}