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

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioInfoHeader;

/**
 * The identification header identifies the bitstream as Opus, 
 *  and includes the Opus version, the simple audio characteristics 
 *  of the stream such as sample rate and number of channels etc.
 */
public class OpusInfo extends HighLevelOggStreamPacket implements OpusPacket, OggAudioInfoHeader {
    private byte version;
    private int majorVersion;
    private int minorVersion;

    private int channels;
    private int preSkip;
    private long rate;
    private int outputGain;
    private byte channelMappingFamily;
    private byte streamCount;
    private byte twoChannelStreamCount;
    private byte[] channelMapping;

    public OpusInfo() {
        super();
        version = 1;
    }

    public OpusInfo(OggPacket pkt) {
        super(pkt);
        
        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_HEADER_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Opus Header");
        }

        // Parse
        version = data[8];
        parseVersion();
        if (majorVersion != 0) {
            throw new IllegalArgumentException("Unsupported Opus version " + version + " at major version " + majorVersion + " detected");
        }

        channels = (int)data[9];
        preSkip = IOUtils.getInt2(data, 10);
        rate    = IOUtils.getInt4(data, 12);
        outputGain = IOUtils.getInt2(data, 16);
        
        channelMappingFamily = data[18];
        if (channelMappingFamily != 0) {
            streamCount = data[19];
            twoChannelStreamCount = data[20];
            channelMapping = new byte[channels];
            System.arraycopy(data, 21, channelMapping, 0, channels);
        }
    }

    @Override
    public OggPacket write() {
        int length = 19;
        if (channelMappingFamily != 0) {
            length += 2;
            length += channels;
        }
        byte[] data = new byte[length];
        System.arraycopy(MAGIC_HEADER_BYTES, 0, data, 0, 8);

        data[8] = version;
        data[9] = (byte)channels; 
        IOUtils.putInt2(data, 10, preSkip);
        IOUtils.putInt4(data, 12, rate);
        IOUtils.putInt2(data, 16, outputGain);
        
        data[18] = channelMappingFamily;
        if (channelMappingFamily != 0) {
            data[19] = streamCount;
            data[20] = twoChannelStreamCount;
            System.arraycopy(channelMapping, 0, data, 21, channels);
        }
        
        setData(data);
        return super.write();
    }
    
    private void parseVersion() {
        minorVersion = version & 0xf;
        majorVersion = version >> 4;
    }

    public byte getVersion() {
        return version;
    }
    public int getMajorVersion() {
        return majorVersion;
    }
    public int getMinorVersion() {
        return minorVersion;
    }
    public String getVersionString() {
        return majorVersion + "." + minorVersion;
    }

    public int getNumChannels() {
        return channels;
    }
    public void setNumChannels(int channels) {
        this.channels = channels;
    }

    public int getPreSkip() {
        return preSkip;
    }
    public void setPreSkip(int preSkip) {
        this.preSkip = preSkip;
    }

    public long getRate() {
        return rate;
    }
    public int getSampleRate() {
        return (int)rate;
    }
    public void setSampleRate(long rate) {
        this.rate = rate;
    }

    public int getOutputGain() {
        return outputGain;
    }
    public void setOutputGain(int outputGain) {
        this.outputGain = outputGain;
    }

    public byte getChannelMappingFamily() {
        return channelMappingFamily;
    }
    public byte getStreamCount() {
        return streamCount;
    }
    public byte getTwoChannelStreamCount() {
        return twoChannelStreamCount;
    }
    public byte[] getChannelMapping() {
        return channelMapping;
    }
}
