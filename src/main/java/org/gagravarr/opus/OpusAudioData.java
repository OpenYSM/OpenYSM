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
package org.gagravarr.opus;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggStreamAudioData;

/**
 * Raw, compressed audio data
 */
public class OpusAudioData extends OggStreamAudioData implements OpusPacket {
    /** Opus is special - granule always runs at 48kHz */
    public static final int OPUS_GRANULE_RATE = 48000;
    private int numFrames = -1;
    private int numSamples = -1;
    
    public OpusAudioData(OggPacket pkt) {
        super(pkt);
    }
    public OpusAudioData(byte[] data) {
        super(data);
    }

    protected boolean isEndOfStream() {
        return getOggPacket().isEndOfStream();
    }
    
    public int getNumberOfFrames() {
        if (numFrames == -1) {
            calculateStructure();
        }
        return numFrames;
    }
    public int getNumberOfSamples() {
        if (numSamples == -1) {
            calculateStructure();
        }
        return numSamples;
    }
    
    private void calculateStructure() {
        byte[] d = getData();
        numFrames = packet_get_nb_frames(d);
        numSamples = numFrames * packet_get_samples_per_frame(d, OPUS_GRANULE_RATE);
    }
    
    private static int packet_get_samples_per_frame(byte[] data, int fs) {
        int audiosize;
        if (data.length == 0) {
            audiosize = 0;
        } else if ((data[0]&0x80) != 0) {
            audiosize = ((data[0]>>3)&0x3);
            audiosize = (fs<<audiosize)/400;
        } else if ((data[0]&0x60) == 0x60) {
            audiosize = ((data[0]&0x08) != 0) ? fs/50 : fs/100;
        } else {
            audiosize = ((data[0]>>3)&0x3);
            if (audiosize == 3)
                audiosize = fs*60/1000;
            else
                audiosize = (fs<<audiosize)/100;
        }
        return audiosize;
    }

    private static int packet_get_nb_frames(byte[] packet) {
        int count = 0;
        if (packet.length < 1) {
            return -1;
        }
        count = packet[0]&0x3;
        if (count==0)
            return 1;
        else if (count!=3)
            return 2;
        else if (packet.length<2)
            return -4;
        else
            return packet[1]&0x3F;
    }
}
