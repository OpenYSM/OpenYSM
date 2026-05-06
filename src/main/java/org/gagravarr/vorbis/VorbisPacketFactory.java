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

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import static org.gagravarr.vorbis.VorbisPacket.TYPE_COMMENTS;
import static org.gagravarr.vorbis.VorbisPacket.TYPE_INFO;
import static org.gagravarr.vorbis.VorbisPacket.TYPE_SETUP;

/**
 * Identifies the right kind of {@link VorbisPacket} for a given
 *  incoming {@link OggPacket}, and creates it
 */
public class VorbisPacketFactory {
    /**
     * Popupulates the metadata packet header,
     *  which is "#vorbis" where # is the type.
     */
    protected static void populateMetadataHeader(byte[] b, int type, int dataLength) {
        b[0] = IOUtils.fromInt(type);
        b[1] = (byte)'v';
        b[2] = (byte)'o';
        b[3] = (byte)'r';
        b[4] = (byte)'b';
        b[5] = (byte)'i';
        b[6] = (byte)'s';
    }
	
    /**
     * Does this packet (the first in the stream) contain
     *  the magic string indicating that it's a vorbis
     *  one?
     */
    public static boolean isVorbisStream(OggPacket firstPacket) {
        if(! firstPacket.isBeginningOfStream()) {
            return false;
        }
        return isVorbisSpecial(firstPacket);
    }
	
    protected static boolean isVorbisSpecial(OggPacket packet) {
        byte[] d = packet.getData();
        if (d.length < 16) return false;

        // Ensure "vorbis" on the special types
        byte type = d[0];
        if(type == 1 || type == 3 || type == 5) {
            if(d[1] == (byte)'v' &&
               d[2] == (byte)'o' &&
               d[3] == (byte)'r' &&
               d[4] == (byte)'b' &&
               d[5] == (byte)'i' &&
               d[6] == (byte)'s') {
                    return true;
            }
        }
        return false;
    }
	
    /**
     * Creates the appropriate {@link VorbisPacket}
     *  instance based on the type.
     */
    public static VorbisPacket create(OggPacket packet) {
        // Special header types detection
        if (isVorbisSpecial(packet)) {
            byte type = packet.getData()[0];
            switch(type) {
            case TYPE_INFO:
                return new VorbisInfo(packet);
            case TYPE_COMMENTS:
                return new VorbisComments(packet);
            case TYPE_SETUP:
                return new VorbisSetup(packet);
            }
        }

        return new VorbisAudioData(packet);
    }
}
