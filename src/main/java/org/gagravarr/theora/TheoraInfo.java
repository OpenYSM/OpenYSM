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
package org.gagravarr.theora;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * The identification header identifies the bitstream as Theora, 
 *  and includes the Theora version, the frame details, the picture
 *  region details and similar.
 */
public class TheoraInfo extends HighLevelOggStreamPacket implements TheoraPacket {
    private int majorVersion;
    private int minorVersion;
    private int revisionVersion;

    private int frameWidthMB;
    private int frameHeightMB;
    private long frameNumSuperBlocks;
    private long frameNumBlocks;
    private long frameNumMacroBlocks;

    private long pictureRegionWidth;
    private long pictureRegionHeight;
    private int pictureRegionXOffset;
    private int pictureRegionYOffset;

    private long frameRateNumerator;
    private long frameRateDenominator;

    private long pixelAspectNumerator;
    private long pixelAspectDenomerator;

    private int colourSpace;
    private int pixelFormat;

    private long nominalBitrate;
    private int qualityHint;
    private int keyFrameNumberGranuleShift;

    public TheoraInfo() {
        super();
        majorVersion = 3;
        minorVersion = 2;
        revisionVersion = 1;
    }

    public TheoraInfo(OggPacket pkt) {
        super(pkt);

        // Parse
        byte[] data = getData();

        majorVersion = (int)data[7];
        minorVersion = (int)data[8];
        revisionVersion = (int)data[9];
        if (majorVersion != 3) {
            throw new IllegalArgumentException("Unsupported Theora version " + getVersion() + " detected");
        }

        frameWidthMB  = IOUtils.getInt2BE(data, 10);
        frameHeightMB = IOUtils.getInt2BE(data, 12);

        pictureRegionWidth  = IOUtils.getInt3BE(data, 14);
        pictureRegionHeight = IOUtils.getInt3BE(data, 17);
        pictureRegionXOffset = (int)data[20];
        pictureRegionYOffset = (int)data[21];

        frameRateNumerator   = IOUtils.getInt4BE(data, 22);
        frameRateDenominator = IOUtils.getInt4BE(data, 26);

        pixelAspectNumerator   = IOUtils.getInt3BE(data, 30);
        pixelAspectDenomerator = IOUtils.getInt3BE(data, 33);

        colourSpace = (int)data[36];
        nominalBitrate = IOUtils.getInt3BE(data, 37);

        // Last two bytes are complicated...
        int lastTwo = IOUtils.getInt2BE(data, 40);
        qualityHint = (lastTwo >> 10); // 6 bits
        keyFrameNumberGranuleShift = (lastTwo >> 5) & 31; // 5 bits
        pixelFormat = (lastTwo >> 3) & 3; // 2 bits
    }

    @Override
    public OggPacket write() {
        byte[] data = new byte[42];
        TheoraPacketFactory.populateMetadataHeader(data, TYPE_IDENTIFICATION, data.length);

        data[7] = IOUtils.fromInt(majorVersion);
        data[8] = IOUtils.fromInt(minorVersion);
        data[9] = IOUtils.fromInt(revisionVersion);

        IOUtils.putInt2BE(data, 10, frameWidthMB);
        IOUtils.putInt2BE(data, 12, frameHeightMB);
        IOUtils.putInt3BE(data, 14, pictureRegionWidth);
        IOUtils.putInt3BE(data, 17, pictureRegionHeight);
        data[20] = IOUtils.fromInt(pictureRegionXOffset);
        data[21] = IOUtils.fromInt(pictureRegionYOffset);

        IOUtils.putInt4BE(data, 22, frameRateNumerator);
        IOUtils.putInt4BE(data, 26, frameRateDenominator);

        IOUtils.putInt3BE(data, 30, pixelAspectNumerator);
        IOUtils.putInt3BE(data, 33, pixelAspectDenomerator);

        data[36] = IOUtils.fromInt(colourSpace);
        IOUtils.putInt3BE(data, 37, nominalBitrate);

        // Last two bytes are complicated...
        int lastTwo = ((qualityHint << 6) + keyFrameNumberGranuleShift);
        lastTwo = ((lastTwo) << 2) + pixelFormat;
        lastTwo = lastTwo << 3; // last 3 bits padding
        IOUtils.putInt2BE(data, 40, lastTwo);

        setData(data);
        return super.write();
    }

    public String getVersion() {
        return majorVersion + "." + minorVersion + "." + revisionVersion;
    }
    public int getMajorVersion() {
        return majorVersion;
    }
    public int getMinorVersion() {
        return minorVersion;
    }
    public int getRevisionVersion() {
        return revisionVersion;
    }

    /**
     * The width of a frame, in Pixels
     */
    public int getFrameWidth() {
        return frameWidthMB << 4;
    }
    /**
     * The height of a frame, in Pixels
     */
    public int getFrameHeight() {
        return frameHeightMB << 4;
    }

    /**
     * The width of a frame, in Macro Blocks
     */
    public int getFrameWidthMB() {
        return frameWidthMB;
    }
    public void setFrameWidthMB(int frameWidthMB) {
        this.frameWidthMB = frameWidthMB;
    }

    /**
     * The height of a frame, in Macro Blocks
     */
    public int getFrameHeightMB() {
        return frameHeightMB;
    }
    public void setFrameHeightMB(int frameHeightMB) {
        this.frameHeightMB = frameHeightMB;
    }

    /**
     * The number of super blocks in a frame
     */
    public long getFrameNumSuperBlocks() {
        return frameNumSuperBlocks;
    }
    public void setFrameNumSuperBlocks(long frameNumSuperBlocks) {
        this.frameNumSuperBlocks = frameNumSuperBlocks;
    }

    /**
     * The number of blocks in a frame
     */
    public long getFrameNumBlocks() {
        return frameNumBlocks;
    }
    public void setFrameNumBlocks(long frameNumBlocks) {
        this.frameNumBlocks = frameNumBlocks;
    }

    /**
     * The number of marco blocks in a frame
     */
    public long getFrameNumMacroBlocks() {
        return frameNumMacroBlocks;
    }
    public void setFrameNumMacroBlocks(long frameNumMacroBlocks) {
        this.frameNumMacroBlocks = frameNumMacroBlocks;
    }

    /**
     * The width of the picture region, in pixels
     */
    public long getPictureRegionWidth() {
        return pictureRegionWidth;
    }
    public void setPictureRegionWidth(long pictureRegionWidth) {
        this.pictureRegionWidth = pictureRegionWidth;
    }

    /**
     * The height of the picture region, in pixels
     */
    public long getPictureRegionHeight() {
        return pictureRegionHeight;
    }
    public void setPictureRegionHeight(long pictureRegionHeight) {
        this.pictureRegionHeight = pictureRegionHeight;
    }

    /**
     * The x offset to the start of the picture region, in pixels
     */
    public int getPictureRegionXOffset() {
        return pictureRegionXOffset;
    }
    public void setPictureRegionXOffset(int pictureRegionXOffset) {
        this.pictureRegionXOffset = pictureRegionXOffset;
    }

    /**
     * The y offset to the start of the picture region, in pixels
     */
    public int getPictureRegionYOffset() {
        return pictureRegionYOffset;
    }
    public void setPictureRegionYOffset(int pictureRegionYOffset) {
        this.pictureRegionYOffset = pictureRegionYOffset;
    }

    /**
     * The frame rate numerator
     */
    public long getFrameRateNumerator() {
        return frameRateNumerator;
    }
    public void setFrameRateNumerator(long frameRateNumerator) {
        this.frameRateNumerator = frameRateNumerator;
    }

    /**
     * The frame rate denominator
     */
    public long getFrameRateDenominator() {
        return frameRateDenominator;
    }
    public void setFrameRateDenominator(long frameRateDenominator) {
        this.frameRateDenominator = frameRateDenominator;
    }

    /**
     * Pixel aspect ratio numerator
     */
    public long getPixelAspectNumerator() {
        return pixelAspectNumerator;
    }
    public void setPixelAspectNumerator(long pixelAspectNumerator) {
        this.pixelAspectNumerator = pixelAspectNumerator;
    }

    /**
     * Pixel aspect ratio denomerator
     */
    public long getPixelAspectDenomerator() {
        return pixelAspectDenomerator;
    }
    public void setPixelAspectDenomerator(long pixelAspectDenomerator) {
        this.pixelAspectDenomerator = pixelAspectDenomerator;
    }

    /**
     * Colour space, from the indexed list
     */
    public int getColourSpace() {
        return colourSpace;
    }
    public void setColourSpace(int colourSpace) {
        this.colourSpace = colourSpace;
    }

    /**
     * ????
     */
    public int getPixelFormat() {
        return pixelFormat;
    }
    public void setPixelFormat(int pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    /**
     * Nominal bitrate, in bits per second, or zero if the
     *  encoder couldn't guess
     */
    public long getNominalBitrate() {
        return nominalBitrate;
    }
    public void setNominalBitrate(long nominalBitrate) {
        this.nominalBitrate = nominalBitrate;
    }

    /**
     * Quality hint - higher is better
     */
    public int getQualityHint() {
        return qualityHint;
    }
    public void setQualityHint(int qualityHint) {
        this.qualityHint = qualityHint;
    }

    /**
     * Shift for splitting the granule position between
     *  the frame number of the last frame, and the number
     *  of frames since then
     */
    public int getKeyFrameNumberGranuleShift() {
        return keyFrameNumberGranuleShift;
    }
    public void setKeyFrameNumberGranuleShift(int keyFrameNumberGranuleShift) {
        this.keyFrameNumberGranuleShift = keyFrameNumberGranuleShift;
    }
}
