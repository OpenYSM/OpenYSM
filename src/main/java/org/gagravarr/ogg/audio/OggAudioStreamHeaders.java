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
package org.gagravarr.ogg.audio;

import org.gagravarr.flac.FlacFirstOggPacket;
import org.gagravarr.flac.FlacTags;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggStreamAudioData;
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.ogg.OggStreamIdentifier.OggStreamType;
import org.gagravarr.ogg.OggStreamPacket;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusPacketFactory;
import org.gagravarr.speex.SpeexInfo;
import org.gagravarr.speex.SpeexPacketFactory;
import org.gagravarr.vorbis.VorbisInfo;
import org.gagravarr.vorbis.VorbisPacketFactory;


/**
 * Streaming friendly way to get at the headers at the
 *  start of an {@link OggAudioStream}, allowing for the fact
 *  that they may be interspersed with other streams' data.
 */
public class OggAudioStreamHeaders implements OggAudioHeaders {
    private int sid;
    private OggStreamType type;
    private OggAudioInfoHeader info;
    private OggAudioTagsHeader tags;
    private OggAudioSetupHeader setup;

    private OggAudioStreamHeaders(int sid, OggStreamType type, OggAudioInfoHeader info) {
        this.sid = sid;
        this.type = type;
        this.info = info;
    }

    /**
     * Identifies the type, and returns a partially filled
     *  {@link OggAudioHeaders} for the new stream
     */
    public static OggAudioStreamHeaders create(OggPacket firstPacket) {
        if (firstPacket.isBeginningOfStream() &&
                firstPacket.getData() != null &&
                firstPacket.getData().length > 10) {
            int sid = firstPacket.getSid();
            if (VorbisPacketFactory.isVorbisStream(firstPacket)) {
                return new OggAudioStreamHeaders(sid, 
                        OggStreamIdentifier.OGG_VORBIS,
                        (VorbisInfo)VorbisPacketFactory.create(firstPacket));
            }
            if (SpeexPacketFactory.isSpeexStream(firstPacket)) {
                return new OggAudioStreamHeaders(sid, 
                        OggStreamIdentifier.SPEEX_AUDIO,
                        (SpeexInfo)SpeexPacketFactory.create(firstPacket));
            }
            if (OpusPacketFactory.isOpusStream(firstPacket)) {
                return new OggAudioStreamHeaders(sid, 
                        OggStreamIdentifier.OPUS_AUDIO,
                        (OpusInfo)OpusPacketFactory.create(firstPacket));
            }
            if (FlacFirstOggPacket.isFlacStream(firstPacket)) {
                FlacFirstOggPacket flac = new FlacFirstOggPacket(firstPacket);
                return new OggAudioStreamHeaders(sid, 
                        OggStreamIdentifier.OGG_FLAC,
                        flac.getInfo());
            }
            throw new IllegalArgumentException("Unsupported stream of type " + OggStreamIdentifier.identifyType(firstPacket));
        } else {
            throw new IllegalArgumentException("May only be called for the first packet in a stream, with data");
        }
    }

    /**
     * Creates an appropriate high level packet
     */
    protected OggStreamPacket createNext(OggPacket packet) {
        if (type == OggStreamIdentifier.OGG_VORBIS) {
            return VorbisPacketFactory.create(packet);
        } else if (type == OggStreamIdentifier.SPEEX_AUDIO) {
            return SpeexPacketFactory.create(packet);
        } else if (type == OggStreamIdentifier.OPUS_AUDIO) {
            return OpusPacketFactory.create(packet);
        } else if (type == OggStreamIdentifier.OGG_FLAC) {
            // TODO Finish FLAC support
            return null;
        } else {
            throw new IllegalArgumentException("Unsupported stream of type " + type);
        }
    }

    /**
     * Populates with the next header
     *
     * @return Do any more headers remain to be populated?
     */
    public boolean populate(OggPacket packet) {
        // TODO Finish the flac support properly
        if (type == OggStreamIdentifier.OGG_FLAC) {
            if (tags == null) {
                tags = new FlacTags(packet);
                return true;
            } else {
                // TODO Finish FLAC support
                return false;
            }
        }

        OggStreamPacket sPacket = createNext(packet);
        if (sPacket instanceof OggAudioTagsHeader) {
            tags = (OggAudioTagsHeader)sPacket;

            // Are there more headers to come?
            if (type == OggStreamIdentifier.OGG_VORBIS) {
                return true;
            } else {
                return false;
            }
        }
        if (sPacket instanceof OggAudioSetupHeader) {
            setup = (OggAudioSetupHeader)sPacket;

            // Setup is always last
            return false;
        }

        throw new IllegalArgumentException("Expecting header packet but got " + sPacket);
    }

    /**
     * Creates the Audio Data for a given audio packet
     */
    public OggStreamAudioData createAudio(OggPacket packet) {
        return (OggStreamAudioData)createNext(packet);
    }


    /**
     * @return The stream id of the overall audio stream
     */
    public int getSid() {
        return sid;
    }
    /**
     * @return The type of the audio stream
     */
    public OggStreamType getType() {
        return type;
    }
    /**
     * @return The information / identification of the stream and audio encoding
     */
    public OggAudioInfoHeader getInfo() {
        return info;
    }
    /**
     * @return The Tags / Comments describing the stream
     */
    public OggAudioTagsHeader getTags() {
        return tags;
    }
    /**
     * @return The Setup information for the audio encoding, if used in the format
     */
    public OggAudioSetupHeader getSetup() {
        return setup;
    }
}
