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
package org.gagravarr.speex;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioInfoHeader;

/**
 * The identification header identifies the bitstream as Speex, 
 *  and includes the Speex version, the simple audio characteristics 
 *  of the stream such as sample rate and number of channels etc.
 */
public class SpeexInfo extends HighLevelOggStreamPacket implements SpeexPacket, OggAudioInfoHeader {
    private String versionString;
    private int versionId;
    private long rate;
    private int mode;
    private int modeBitstreamVersion;
    private int channels;
    private int bitrate;
    private int frameSize;
    private int vbr;
    private int framesPerPacket;
    private int extraHeaders;
    private int reserved1;
    private int reserved2;
    
    public SpeexInfo() {
        super();
        versionString = "Gagravarr Ogg v0.8";
        versionId = 1;
    }

    public SpeexInfo(OggPacket pkt) {
        super(pkt);
        
        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_HEADER_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Speex Header");
        }

        // Parse
        versionString = IOUtils.removeNullPadding( IOUtils.getUTF8(data, 8, 20) );
        versionId = (int)IOUtils.getInt4(data, 28);
        
        int headerSize = (int)IOUtils.getInt4(data, 32);
        if (headerSize != data.length) {
            throw new IllegalArgumentException("Invalid Speex Header, expected " + headerSize + " bytes, found " + data.length);
        }

        rate = IOUtils.getInt4(data, 36);
        mode = (int)IOUtils.getInt4(data, 40);
        modeBitstreamVersion = (int)IOUtils.getInt4(data, 44);
        channels  = (int)IOUtils.getInt4(data, 48);
        bitrate   = (int)IOUtils.getInt4(data, 52);
        frameSize = (int)IOUtils.getInt4(data, 56);
        vbr       = (int)IOUtils.getInt4(data, 60);
        framesPerPacket = (int)IOUtils.getInt4(data, 64);
        extraHeaders = (int)IOUtils.getInt4(data, 68);
        reserved1    = (int)IOUtils.getInt4(data, 72);
        reserved2    = (int)IOUtils.getInt4(data, 76);
    }

    @Override
    public OggPacket write() {
        byte[] data = new byte[80];
        System.arraycopy(MAGIC_HEADER_BYTES, 0, data, 0, 8);
        
        IOUtils.putUTF8(data, 8, versionString);
        IOUtils.putInt4(data, 28, versionId);
        
        IOUtils.putInt4(data, 32, data.length);
        
        IOUtils.putInt4(data, 36, rate);
        IOUtils.putInt4(data, 40, mode);
        IOUtils.putInt4(data, 44, modeBitstreamVersion);
        IOUtils.putInt4(data, 48, channels);
        IOUtils.putInt4(data, 52, bitrate);
        IOUtils.putInt4(data, 56, frameSize);
        IOUtils.putInt4(data, 60, vbr);
        IOUtils.putInt4(data, 64, framesPerPacket);
        IOUtils.putInt4(data, 68, extraHeaders);
        IOUtils.putInt4(data, 72, reserved1);
        IOUtils.putInt4(data, 76, reserved2);
        
        setData(data);
        return super.write();
    }

    public String getVersionString() {
        return versionString;
    }
    public void setVersionString(String versionString) {
        if (versionString.length() > 20) {
            versionString = versionString.substring(0, 20);
        }
        this.versionString = versionString;
    }

    public int getVersionId() {
        return versionId;
    }
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public long getRate() {
        return rate;
    }
    public int getSampleRate() {
        return (int)rate;
    }
    public void setRate(long rate) {
        this.rate = rate;
    }

    public int getMode() {
        return mode;
    }
    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getModeBitstreamVersion() {
        return modeBitstreamVersion;
    }
    public void setModeBitstreamVersion(int modeBitstreamVersion) {
        this.modeBitstreamVersion = modeBitstreamVersion;
    }

    public int getNumChannels() {
        return channels;
    }
    public void setNumChannels(int channels) {
        this.channels = channels;
    }

    public int getBitrate() {
        return bitrate;
    }
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getPreSkip() {
        return 0;
    }

    public int getFrameSize() {
        return frameSize;
    }
    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public int getVbr() {
        return vbr;
    }
    public void setVbr(int vbr) {
        this.vbr = vbr;
    }

    public int getFramesPerPacket() {
        return framesPerPacket;
    }
    public void setFramesPerPacket(int framesPerPacket) {
        this.framesPerPacket = framesPerPacket;
    }

    public int getExtraHeaders() {
        return extraHeaders;
    }
    public int getReserved1() {
        return reserved1;
    }
    public int getReserved2() {
        return reserved2;
    }
}
