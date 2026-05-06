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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggFile;

/**
 * This wrapper lets you work with FLAC files,
 *  whether they're Ogg or Native framed.
 */
public abstract class FlacFile implements Closeable {
    protected FlacInfo info;
    protected FlacTags tags;
    protected List<FlacMetadataBlock> otherMetadata;

    /**
     * Opens the given file for reading
     */
    public static FlacFile open(File f) throws IOException, FileNotFoundException {
        // Open, in a way that we can skip backwards a few bytes
        InputStream inp = new BufferedInputStream(new FileInputStream(f), 8);
        FlacFile file = open(inp);
        return file;
    }
   /**
    * Opens the given file for reading.
    * @param inp The InputStrem to read from, which must support mark/reset
    */
   public static FlacFile open(InputStream inp) throws IOException, FileNotFoundException {
      inp.mark(4);
      byte[] header = new byte[4];
      IOUtils.readFully(inp, header);
      inp.reset();
      
      if(header[0] == (byte)'O' && header[1] == (byte)'g' &&
         header[2] == (byte)'g' && header[3] == (byte)'S') {
         return new FlacOggFile(new OggFile(inp));
      }
      if(header[0] == (byte)'f' && header[1] == (byte)'L' &&
         header[2] == (byte)'a' && header[3] == (byte)'C') {
         return new FlacNativeFile(inp);
      }
      throw new IllegalArgumentException("File type not recognised");
   }
   /**
    * Opens the given file for reading
    */
   public static FlacFile open(OggFile ogg) throws IOException {
       return new FlacOggFile(ogg);
   }

   public abstract FlacAudioFrame getNextAudioPacket() throws IOException;

   /**
    * Skips the audio data to the next packet with a granule
    *  of at least the given granule position.
    * Note that skipping backwards is not currently supported!
    */
   public abstract void skipToGranule(long granulePosition) throws IOException;

   public FlacInfo getInfo() {
       return info;
   }
   public FlacTags getTags() {
       return tags;
   }
   public List<FlacMetadataBlock> getOtherMetadata() {
      return otherMetadata;
   }

   /**
    * In Reading mode, will close the underlying ogg/flac
    *  file and free its resources.
    * In Writing mode, will write out the Info and 
    *  Comments objects, and then the audio data.
    */
   public abstract void close() throws IOException;
}
