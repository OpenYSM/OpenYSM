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
package org.gagravarr.ogg;

import org.gagravarr.flac.FlacFirstOggPacket;
import org.gagravarr.ogg.OggStreamIdentifier.OggStreamType.Kind;
import org.gagravarr.opus.OpusPacketFactory;
import org.gagravarr.skeleton.SkeletonPacketFactory;
import org.gagravarr.speex.SpeexPacketFactory;
import org.gagravarr.theora.TheoraPacketFactory;
import org.gagravarr.vorbis.VorbisPacketFactory;

/**
 * Detector for identifying the kind of data stored in a given stream.
 * This is normally used on the first packet in a stream, to work out 
 *  the type, if recognised.
 * Note - the mime types and descriptions should be kept roughly in sync
 *  with those in Apache Tika
 */
public class OggStreamIdentifier {
    public static class OggStreamType {
        public final String mimetype;
        public final String description;
        public final Kind kind;
        public enum Kind { GENERAL, AUDIO, VIDEO, METADATA };
        protected OggStreamType(String mimetype, String description, Kind kind) {
            this.mimetype = mimetype;
            this.description = description;
            this.kind = kind;
        }
        public String toString() {
            return kind + " - " + description + " as " + mimetype;
        }
    }

    // General types
    public static final OggStreamType OGG_GENERAL = new OggStreamType(
                                      "application/ogg", "Ogg", Kind.GENERAL);
    public static final OggStreamType OGG_VIDEO = new OggStreamType(
                                      "video/ogg", "Ogg Video", Kind.VIDEO);
    public static final OggStreamType OGG_AUDIO = new OggStreamType(
                                      "audio/ogg", "Ogg Audio", Kind.AUDIO);
    public static final OggStreamType UNKNOWN = new OggStreamType(
                                      "application/octet-stream", "Unknown", Kind.GENERAL);
   
   // Audio types
   public static final OggStreamType OGG_VORBIS = new OggStreamType(
                                     "audio/vorbis", "Vorbis", Kind.AUDIO);
   public static final OggStreamType OPUS_AUDIO = new OggStreamType(
                                     "audio/opus", "Opus", Kind.AUDIO);
   public static final OggStreamType OPUS_AUDIO_ALT = new OggStreamType(
                                     "audio/ogg; codecs=opus", "Opus", Kind.AUDIO);
   public static final OggStreamType SPEEX_AUDIO = new OggStreamType(
                                     "audio/speex", "Speex", Kind.AUDIO);
   public static final OggStreamType SPEEX_AUDIO_ALT = new OggStreamType(
                                     "audio/ogg; codecs=speex", "Speex", Kind.AUDIO);
   public static final OggStreamType OGG_PCM = new OggStreamType(
                                     "audio/x-oggpcm", "Ogg PCM", Kind.AUDIO);
   
   public static final OggStreamType NATIVE_FLAC = new OggStreamType(
                                     "audio/x-flac", "FLAC", Kind.AUDIO);
   public static final OggStreamType OGG_FLAC = new OggStreamType(
                                     "audio/x-oggflac", "FLAC", Kind.AUDIO);
   
   // Video types
   public static final OggStreamType THEORA_VIDEO = new OggStreamType(
                                     "video/theora", "Theora", Kind.VIDEO);
   public static final OggStreamType THEORA_VIDEO_ALT = new OggStreamType(
                                     "video/x-theora", "Theora", Kind.VIDEO);
   public static final OggStreamType DAALA_VIDEO = new OggStreamType(
                                     "video/daala", "Daala", Kind.VIDEO);
   public static final OggStreamType DIRAC_VIDEO = new OggStreamType(
                                     "video/x-dirac", "Dirac", Kind.VIDEO);
   public static final OggStreamType OGM_VIDEO = new OggStreamType(
                                     "video/x-ogm", "Ogg OGM", Kind.VIDEO);
   
   public static final OggStreamType OGG_UVS = new OggStreamType(
                                     "video/x-ogguvs", "Ogg UVS", Kind.VIDEO);
   public static final OggStreamType OGG_YUV = new OggStreamType(
                                     "video/x-oggyuv", "Ogg YUV", Kind.VIDEO);
   public static final OggStreamType OGG_RGB = new OggStreamType(
                                     "video/x-oggrgb", "Ogg RGB", Kind.VIDEO);

   // Metadata types
   public static final OggStreamType SKELETON = new OggStreamType(
                                     "application/annodex", "Skeleton Annodex", Kind.METADATA);
   public static final OggStreamType CMML = new OggStreamType(
                                    "text/x-cmml", "CMML", Kind.METADATA);
   public static final OggStreamType KATE = new OggStreamType(
                                     "application/kate", "Kate", Kind.METADATA);
   
   public static OggStreamType identifyType(OggPacket p) {
       if (! p.isBeginningOfStream()) {
           // All streams so far can be identified from their first packet
           // Very few can be identified past about their 2nd or 3rd
           // So, we only support identifying off the first one
           throw new IllegalArgumentException("Can only Identify from the first packet in a stream");
       } else {
           if (p.getData() != null && p.getData().length > 10) {
               // Is it a Metadata related stream?
               if (SkeletonPacketFactory.isSkeletonStream(p)) {
                   return SKELETON;
               }
               if (isAnnodex2Stream(p)) {
                   return SKELETON;
               }
               if (isCMMLStream(p)) {
                   return CMML;
               }
               if (isKateStream(p)) {
                   return KATE;
               }

               // Is it an Audio stream?
               if (VorbisPacketFactory.isVorbisStream(p)) {
                   // Vorbis Audio stream
                   return OGG_VORBIS;
               }
               if (SpeexPacketFactory.isSpeexStream(p)) {
                   // Speex Audio stream
                   return SPEEX_AUDIO;
               }
               if (OpusPacketFactory.isOpusStream(p)) {
                   // Opus Audio stream
                   return OPUS_AUDIO;
               }
               if (FlacFirstOggPacket.isFlacStream(p)) {
                   // FLAC-in-Ogg Audio stream
                   return OGG_FLAC;
               }
               if (isOggPCMStream(p)) {
                   // PCM-in-Ogg Audio stream
                   return OGG_PCM;
               }

               // Is it a video stream?
               if (TheoraPacketFactory.isTheoraStream(p)) {
                   return THEORA_VIDEO;
               }
               if (isDaalaStream(p)) {
                   return DAALA_VIDEO;
               }
               if (isDiracStream(p)) {
                   return DIRAC_VIDEO;
               }
               if (isOggOGMStream(p)) {
                   return OGM_VIDEO;
               }
               if (isOggUVSStream(p)) {
                   return OGG_UVS;
               }
               if (isOggYUVStream(p)) {
                   return OGG_YUV;
               }
               if (isOggRGBStream(p)) {
                   return OGG_RGB;
               }
           }
           // Couldn't determine what it is
           return UNKNOWN;
       }
   }

   // These methods provide first packet type detection for the
   //  various Ogg-based formats we lack general support for
   protected static final byte[] MAGIC_OGG_PCM = IOUtils.toUTF8Bytes("PCM     ");
   protected static boolean isOggPCMStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_PCM, p.getData(), 0);
   }

   protected static final byte[] MAGIC_DAALA = new byte[8];
   static {
       MAGIC_DAALA[0] = (byte)0x80;
       IOUtils.putUTF8(MAGIC_DAALA, 1, "daala");
       // Remaining 2 bytes are all zero
   }
   protected static boolean isDaalaStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_DAALA, p.getData(), 0);
   }
   protected static final byte[] MAGIC_DIRAC = IOUtils.toUTF8Bytes("BBCD");
   protected static boolean isDiracStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_DIRAC, p.getData(), 0);
   }
   protected static final byte[] MAGIC_OGG_OGM = IOUtils.toUTF8Bytes("video");
   protected static boolean isOggOGMStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_OGM, p.getData(), 0);
   }
   protected static final byte[] MAGIC_OGG_UVS = IOUtils.toUTF8Bytes("UVS ");
   protected static boolean isOggUVSStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_UVS, p.getData(), 0);
   }
   protected static final byte[] MAGIC_OGG_YUV = IOUtils.toUTF8Bytes("\1YUV");
   protected static boolean isOggYUVStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_YUV, p.getData(), 0);
   }
   protected static final byte[] MAGIC_OGG_RGB = IOUtils.toUTF8Bytes("\1GBP");
   protected static boolean isOggRGBStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_OGG_RGB, p.getData(), 0);
   }

   protected static final byte[] MAGIC_CMML = IOUtils.toUTF8Bytes("CMML\0\0\0\0");
   protected static boolean isCMMLStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_CMML, p.getData(), 0);
   }
   protected static final byte[] MAGIC_KATE = new byte[8];
   static {
       MAGIC_KATE[0] = (byte)0x80;
       IOUtils.putUTF8(MAGIC_KATE, 1, "kate");
       // Remaining 3 bytes are all zero
   }
   protected static boolean isKateStream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_KATE, p.getData(), 0);
   }
   protected static final byte[] MAGIC_ANNODEX2 = IOUtils.toUTF8Bytes("Annodex\0");
   protected static boolean isAnnodex2Stream(OggPacket p) {
       return IOUtils.byteRangeMatches(MAGIC_ANNODEX2, p.getData(), 0);
   }
}
