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

import java.io.IOException;

import org.gagravarr.ogg.OggStreamAudioData;

/**
 * Interface for reading a stream of {@link OggStreamAudioData}
 *  packets, either in a sequential (streaming) fashion, or by
 *  skipping to a certain point.
 */
public interface OggAudioStream {
    /**
     * Returns the next {@link OggStreamAudioData} packet in the
     *  stream, or null if no more remain
     */
    public OggStreamAudioData getNextAudioPacket() throws IOException;

    /**
     * Skips the audio data to the next packet with a granule
     *  of at least the given granule position.
     * Note that skipping backwards may not be supported!
     */
    public void skipToGranule(long granulePosition) throws IOException;
}
