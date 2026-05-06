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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.gagravarr.ogg.IOUtils;


/**
 * This comes before the audio data.
 * Made up of a series of:
 *  1 byte type
 *  3 byte length
 *  <data>
 */
public abstract class FlacMetadataBlock extends FlacFrame {
   public static final byte STREAMINFO = 0;
   public static final byte PADDING = 1;
   public static final byte APPLICATION = 2;
   public static final byte SEEKTABLE = 3;
   public static final byte VORBIS_COMMENT = 4;
   public static final byte CUESHEET = 5;
   public static final byte PICTURE = 6;
   // 7-126 : reserved
   // 127 : invalid, to avoid confusion with a frame sync code
   public static final int MASK_BLOCKTYPE = 0b01111111;
   public static final int MASK_LASTBLOCK = 0b10000000;

   private byte type;

   public static FlacMetadataBlock create(InputStream inp) throws IOException {
      int typeI = inp.read();
      if(typeI == -1) {
         throw new IllegalArgumentException();
      }
      byte type = IOUtils.fromInt(typeI);
      byte[] l = new byte[3];
      IOUtils.readFully(inp, l);
      int length = (int)IOUtils.getInt3BE(l);

      byte[] data = new byte[length];
      IOUtils.readFully(inp, data);

      // Grab the type, whether the last block or not
      int  blockType = type & MASK_BLOCKTYPE;

      switch(blockType) {
         case STREAMINFO:
            return new FlacInfo(data, 0);
         case VORBIS_COMMENT:
            return new FlacTags.FlacTagsAsMetadata(type, data);
         default:
            return new FlacUnhandledMetadataBlock(type, data);
      }
   }

   protected FlacMetadataBlock(byte type) {
       this.type = type;
   }

   public int getType() {
       return type & MASK_BLOCKTYPE;
   }

   public boolean isLastMetadataBlock() {
       // Top bit of the type is the flag for this
       return (type & MASK_LASTBLOCK) != 0;
   }

   public byte[] getData() {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();

       try {
           // Type goes first
           baos.write(type);
           // Pad length, will do later
           baos.write(new byte[3]);

           // Do the main data
           write(baos);
       } catch(IOException e) {
           // Shouldn't ever happen!
           throw new RuntimeException(e);
       }

       // Fix the length
       byte[] data = baos.toByteArray();
       IOUtils.putInt3BE(data, 1, data.length);

       // All done
       return data;
   }

   protected abstract void write(OutputStream out) throws IOException;
}
