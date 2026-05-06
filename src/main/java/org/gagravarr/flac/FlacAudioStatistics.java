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

import java.io.IOException;

/** 
 * For computing statistics around a {@link FlacFile},
 *  such as how long it lasts.
 * Most encoders will include this information in the
 *  {@link FlacInfo} header.
 */
public class FlacAudioStatistics {
   private final FlacFile flac;

   private int audioFrames = 0;
   private double durationSeconds = 0;

   public FlacAudioStatistics(FlacFile flac) {
      this.flac = flac;
   }

   /**
    * Reads the whole file, and calculates the statistics
    *  from the headers and audio frames.
    */
   public void calculate() throws IOException {
      FlacAudioFrame data;

      while ((data = flac.getNextAudioPacket()) != null) {
         handleAudioData(data);
      }
      // TODO Should we calculate anything on efficiency, overhead etc?
   }

   protected void handleAudioData(FlacAudioFrame audio) {
      audioFrames++;
      double frameDuration = (double)audio.getBlockSize() / audio.getSampleRate();
      durationSeconds += frameDuration;
      // TODO Should we calculate any overheads?
   }

   /**
    * Returns the duration of the audio, in seconds.
    */
   public double getDurationSeconds() {
       return durationSeconds;
   }

   /**
    * The number of audio frames in the stream
    */
   public int getAudioFramesCount() {
       return audioFrames;
   }
}
