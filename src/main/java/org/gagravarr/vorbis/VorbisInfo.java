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

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioInfoHeader;

/**
 * The identification header identifies the bitstream as Vorbis, 
 *  Vorbis version, and the simple audio characteristics of the 
 *  stream such as sample rate and number of channels.
 */
public class VorbisInfo extends HighLevelOggStreamPacket implements VorbisPacket, OggAudioInfoHeader {
    private int version;
    private int channels;
    private long rate;
    private int bitrateUpper;
    private int bitrateNominal;
    private int bitrateLower;
    private int blocksizes;

    public VorbisInfo() {
        super();
        version = 0;
    }

    public VorbisInfo(OggPacket pkt) {
        super(pkt);

        // Parse
        byte[] data = getData();
        version = (int)IOUtils.getInt4(data, 7);
        if(version != 0) {
            throw new IllegalArgumentException("Unsupported vorbis version " + version + " detected");
        }

        channels = (int)data[11];
        rate = IOUtils.getInt4(data, 12);
        bitrateUpper =   (int)IOUtils.getInt4(data, 16);
        bitrateNominal = (int)IOUtils.getInt4(data, 20);
        bitrateLower =   (int)IOUtils.getInt4(data, 24);

        blocksizes = IOUtils.toInt(data[28]);
        byte framingBit = data[29];
        if(framingBit == 0) {
            throw new IllegalArgumentException("Framing bit not set, invalid");
        }
    }

    public int getHeaderSize() {
        return HEADER_LENGTH_METADATA;
    }
    public void populateMetadataHeader(byte[] b, int dataLength) {
        VorbisPacketFactory.populateMetadataHeader(b, TYPE_INFO, dataLength);
    }

    @Override
    public OggPacket write() {
        byte[] data = new byte[30];
        populateMetadataHeader(data, data.length);

        IOUtils.putInt4(data, 7, version);
        data[11] = IOUtils.fromInt(channels);
        IOUtils.putInt4(data, 12, rate);
        IOUtils.putInt4(data, 16, bitrateUpper);
        IOUtils.putInt4(data, 20, bitrateNominal);
        IOUtils.putInt4(data, 24, bitrateLower);
        data[28] = IOUtils.fromInt(blocksizes);
        data[29] = 1;

        setData(data);
        return super.write();
    }

    public int getVersion() {
        return version;
    }
    public String getVersionString() {
        return Integer.toString(version);
    }

    public int getChannels() {
        return channels;
    }
    public void setChannels(int channels) {
        this.channels = channels;
    }
    public int getNumChannels() {
        return channels;
    }

    public long getRate() {
        return rate;
    }
    public void setRate(long rate) {
        this.rate = rate;
    }
    public int getSampleRate() {
        return (int)rate;
    }

    public int getPreSkip() {
        return 0;
    }

    public int getBitrateUpper() {
        return bitrateUpper;
    }
    public void setBitrateUpper(int bitrateUpper) {
        this.bitrateUpper = bitrateUpper;
    }

    public int getBitrateNominal() {
        return bitrateNominal;
    }
    public void setBitrateNominal(int bitrateNominal) {
        this.bitrateNominal = bitrateNominal;
    }

    public int getBitrateLower() {
        return bitrateLower;
    }
    public void setBitrateLower(int bitrateLower) {
        this.bitrateLower = bitrateLower;
    }

    public int getBlocksize0() {
        int part = blocksizes & 0x0f;
        return (int)Math.pow(2, part);
    }
    public void setBlocksize0(int blocksize) {
        int part = (int)(Math.log(blocksize) / Math.log(2));
        blocksizes = (blocksizes & 0xf0) + part;
    }

    public int getBlocksize1() {
        int part = (blocksizes & 0xf0) >> 4;
        return (int)Math.pow(2, part);
    }
    public void setBlocksize1(int blocksize) {
        int part = (int)(Math.log(blocksize) / Math.log(2));
        blocksizes = (blocksizes & 0x0f) + (part << 4);
    }
}
