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
package org.gagravarr.skeleton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;

/**
 * The Fishead (note - one h) provides some basic information
 *  on the Skeleton / Annodex stream
 */
public class SkeletonFishead extends HighLevelOggStreamPacket implements SkeletonPacket {
    private int versionMajor;
    private int versionMinor;
    private long presentationTimeNumerator;
    private long presentationTimeDenominator;
    private long baseTimeNumerator;
    private long baseTimeDenominator;
    private String utc;
    private long segmentLength; // v4 only
    private long contentOffset; // v4 only
    
    public SkeletonFishead() {
        super();
        versionMajor = 4;
        versionMinor = 0;
    }

    public SkeletonFishead(OggPacket pkt) {
        super(pkt);
        
        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_FISHEAD_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Skeleton Fishead Header");
        }

        // Parse
        versionMajor = IOUtils.getInt2(data, 8);
        versionMinor = IOUtils.getInt2(data, 10);
        if (versionMajor < 3 || versionMajor > 4) {
            throw new IllegalArgumentException("Unsupported Skeleton version " + versionMajor + " detected");
        }

        presentationTimeNumerator   = IOUtils.getInt8(data, 12);
        presentationTimeDenominator = IOUtils.getInt8(data, 20);
        baseTimeNumerator   = IOUtils.getInt8(data, 28);
        baseTimeDenominator = IOUtils.getInt8(data, 36);

        // UTC is either all-null, or an ISO-8601 date string
        if (data[44] == 0 && data[45] == 0) {
            // Treat as empty
            utc = null;
        } else {
            utc = IOUtils.getUTF8(data, 44, 20);
        }

        if (versionMajor == 4) {
            segmentLength = IOUtils.getInt8(data, 64);
            contentOffset = IOUtils.getInt8(data, 72);
        }
    }

    @Override
    public OggPacket write() {
        int len = 64;
        if (versionMajor == 4) {
            len = 80;
        }
        byte[] data = new byte[len];

        IOUtils.putUTF8(data, 0, MAGIC_FISHEAD_STR);
        IOUtils.putInt2(data, 8, versionMajor);
        IOUtils.putInt2(data, 10, versionMinor);

        IOUtils.putInt8(data, 12, presentationTimeNumerator);
        IOUtils.putInt8(data, 20, presentationTimeDenominator);
        IOUtils.putInt8(data, 28, baseTimeNumerator);
        IOUtils.putInt8(data, 36, baseTimeDenominator);

        if (utc != null) {
            IOUtils.putUTF8(data, 44, utc);
        } else {
            // Leave as all zeros
        }

        if (versionMajor == 4) {
            IOUtils.putInt8(data, 64, segmentLength);
            IOUtils.putInt8(data, 72, contentOffset);
        }

        setData(data);
        return super.write();
    }

    public int getVersionMajor() {
        return versionMajor;
    }
    public void setVersionMajor(int versionMajor) {
        this.versionMajor = versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }
    public void setVersionMinor(int versionMinor) {
        this.versionMinor = versionMinor;
    }

    public String getVersion() {
        return versionMajor + "." + versionMinor;
    }

    public long getPresentationTimeNumerator() {
        return presentationTimeNumerator;
    }
    public void setPresentationTimeNumerator(long presentationTimeNumerator) {
        this.presentationTimeNumerator = presentationTimeNumerator;
    }

    public long getPresentationTimeDenominator() {
        return presentationTimeDenominator;
    }
    public void setPresentationTimeDenominator(long presentationTimeDenominator) {
        this.presentationTimeDenominator = presentationTimeDenominator;
    }

    public long getBaseTimeNumerator() {
        return baseTimeNumerator;
    }
    public void setBaseTimeNumerator(long baseTimeNumerator) {
        this.baseTimeNumerator = baseTimeNumerator;
    }

    public long getBaseTimeDenominator() {
        return baseTimeDenominator;
    }
    public void setBaseTimeDenominator(long baseTimeDenominator) {
        this.baseTimeDenominator = baseTimeDenominator;
    }

    /**
     * Returns the ISO-8601 UTC time of the file,
     *  YYYYMMDDTHHMMSS.sssZ, or null if unset
     */
    public String getUtc() {
        return utc;
    }
    /**
     * Sets the ISO-8601 UTC time of the file, which
     *  must be YYYYMMDDTHHMMSS.sssZ or null
     */
    public void setUtc(String utc) {
        if (utc == null) {
            this.utc = null;
        } else {
            if (utc.length() != 20) {
                throw new IllegalArgumentException("Must be of the form YYYYMMDDTHHMMSS.sssZ");
            }
        }
    }
    public void setUtc(Date utcDate) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT);
        this.utc = fmt.format(utcDate);
    }

    public long getSegmentLength() {
        return segmentLength;
    }
    public void setSegmentLength(long segmentLength) {
        this.segmentLength = segmentLength;
    }

    public long getContentOffset() {
        return contentOffset;
    }
    public void setContentOffset(long contentOffset) {
        this.contentOffset = contentOffset;
    }
}
