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
package org.gagravarr.skeleton;

import static org.gagravarr.skeleton.SkeletonPacket.MAGIC_FISHEAD_BYTES;
import static org.gagravarr.skeleton.SkeletonPacket.MAGIC_FISBONE_BYTES;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * Identifies the right kind of {@link SkeletonPacket} for a given
 *  incoming {@link OggPacket}, and creates it
 */
public class SkeletonPacketFactory extends HighLevelOggStreamPacket {
   /**
    * Does this packet (the first in the stream) contain
    *  the magic string indicating that it's a skeleton fis(head|bone)
    *  one?
    */
   public static boolean isSkeletonStream(OggPacket firstPacket) {
       if(! firstPacket.isBeginningOfStream()) {
           return false;
       }
       return isSkeletonSpecial(firstPacket);
   }

   protected static boolean isSkeletonSpecial(OggPacket packet) {
       byte[] d = packet.getData();
       
       // Is it a Skeleton Fishead or Fisbone packet?
       if (d.length < 52) return false;
       if (IOUtils.byteRangeMatches(MAGIC_FISHEAD_BYTES, d, 0)) return true;
       if (IOUtils.byteRangeMatches(MAGIC_FISBONE_BYTES, d, 0)) return true;
       
       // Not a known Skeleton special packet
       return false;
   }

   /**
    * Creates the appropriate {@link SkeletonPacket}
    *  instance based on the type.
    */
   public static SkeletonPacket create(OggPacket packet) {
       // Special header types detection
       if(isSkeletonSpecial(packet)) {
           byte type = packet.getData()[3];
           switch(type) {
           case (byte)'h': // fishead
               return new SkeletonFishead(packet);
           case (byte)'b': // fisbone
               return new SkeletonFisbone(packet);
           }
       }

       // Only Skeleton 4+ has key frames
       // Skeleton 3 just has the two fis* packets
       return new SkeletonKeyFramePacket(packet);
   }
}