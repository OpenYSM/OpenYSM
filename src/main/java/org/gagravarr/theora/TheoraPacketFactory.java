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
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

import static org.gagravarr.theora.TheoraPacket.TYPE_IDENTIFICATION;
import static org.gagravarr.theora.TheoraPacket.TYPE_COMMENTS;
import static org.gagravarr.theora.TheoraPacket.TYPE_SETUP;

/**
 * Identifies the right kind of {@link TheoraPacket} for a given
 *  incoming {@link OggPacket}, and creates it
 */
public class TheoraPacketFactory extends HighLevelOggStreamPacket {
    /**
     * Popupulates the metadata packet header,
     *  which is "#theora" where # is the type.
     */
    protected static void populateMetadataHeader(byte[] b, int type, int dataLength) {
        b[0] = IOUtils.fromInt(type);
        b[1] = (byte)'t';
        b[2] = (byte)'h';
        b[3] = (byte)'e';
        b[4] = (byte)'o';
        b[5] = (byte)'r';
        b[6] = (byte)'a';
    }

    /**
     * Does this packet (the first in the stream) contain
     *  the magic string indicating that it's an theora
     *  one?
     */
    public static boolean isTheoraStream(OggPacket firstPacket) {
        if(! firstPacket.isBeginningOfStream()) {
            return false;
        }
        return isTheoraSpecial(firstPacket);
    }

    protected static boolean isTheoraSpecial(OggPacket packet) {
        byte[] d = packet.getData();
        if (d.length < 16) return false;

        // Ensure it's the right special type, then theora
        byte type = d[0];
        if(type == (byte)TYPE_IDENTIFICATION ||
           type == (byte)TYPE_COMMENTS ||
           type == (byte)TYPE_SETUP) {
            if (d[1] == (byte)'t' &&
                d[2] == (byte)'h' &&
                d[3] == (byte)'e' &&
                d[4] == (byte)'o' &&
                d[5] == (byte)'r' &&
                d[6] == (byte)'a') {
                   return true;
            }
        }
        return false;
    }

    /**
     * Creates the appropriate {@link TheoraPacket}
     *  instance based on the type.
     */
    public static TheoraPacket create(OggPacket packet) {
        // Special header types detection
        if(isTheoraSpecial(packet)) {
            byte type = packet.getData()[0];
            switch(type) {
            case (byte)TYPE_IDENTIFICATION:
                return new TheoraInfo(packet);
            case (byte)TYPE_COMMENTS:
                return new TheoraComments(packet);
            case (byte)TYPE_SETUP:
                return new TheoraSetup(packet);
            }
        }

        return new TheoraVideoData(packet);
    }
}
