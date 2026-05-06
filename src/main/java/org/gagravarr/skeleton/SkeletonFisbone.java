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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggStreamIdentifier;

/**
 * The Fisbone (note - no h) provides details about
 *  what the other streams in the file are.
 * See http://wiki.xiph.org/SkeletonHeaders for a list of
 *  the suggested "Message Headers", and what they mean.
 */
public class SkeletonFisbone extends HighLevelOggStreamPacket implements SkeletonPacket {
    private static final int MESSAGE_HEADER_OFFSET = 52 - MAGIC_FISBONE_BYTES.length;
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private int messageHeaderOffset;
    private int serialNumber;
    private int numHeaderPackets;
    private long granulerateNumerator;
    private long granulerateDenominator;
    private long baseGranule;
    private int preroll;
    private byte granuleShift;

    private String contentType;
    private Map<String,String> messageHeaders = new HashMap<String, String>();

    public SkeletonFisbone() {
        super();
        messageHeaderOffset = MESSAGE_HEADER_OFFSET;
        contentType = OggStreamIdentifier.UNKNOWN.mimetype;
    }

    public SkeletonFisbone(OggPacket pkt) {
        super(pkt);
        
        // Verify the type
        byte[] data = getData();
        if (! IOUtils.byteRangeMatches(MAGIC_FISBONE_BYTES, data, 0)) {
            throw new IllegalArgumentException("Invalid type, not a Skeleton Fisbone Header");
        }

        // Parse
        messageHeaderOffset = (int)IOUtils.getInt4(data, 8);
        if (messageHeaderOffset != MESSAGE_HEADER_OFFSET) {
            throw new UnsupportedOperationException("Unsupported Skeleton message offset " + messageHeaderOffset + " detected");
        }

        serialNumber = (int)IOUtils.getInt4(data, 12);
        numHeaderPackets = (int)IOUtils.getInt4(data, 16);
        granulerateNumerator = IOUtils.getInt8(data, 20);
        granulerateDenominator = IOUtils.getInt8(data, 28);
        baseGranule = IOUtils.getInt8(data, 36);
        preroll = (int)IOUtils.getInt4(data, 44);
        granuleShift = data[48];
        // Next 3 are padding

        // Rest should be the message headers, in html/mime style
        String headers = IOUtils.getUTF8(data, 52, data.length-52);
        if (! headers.contains(HEADER_CONTENT_TYPE)) {
            throw new UnsupportedOperationException("No Content Type header found in " + headers);
        }
        StringTokenizer st = new StringTokenizer(headers, "\r\n");
        while (st.hasMoreElements()) {
            String line = st.nextToken();
            int splitAt = line.indexOf(": ");
            String k = line.substring(0, splitAt);
            String v = line.substring(splitAt+2);
            messageHeaders.put(k, v);

            if (HEADER_CONTENT_TYPE.equals(k)) {
                contentType = v;
            }
        }
    }

    @Override
    public OggPacket write() {
        // Encode the message headers first
        StringBuffer headersStr = new StringBuffer();
        for (String k : messageHeaders.keySet()) {
            headersStr.append(k);
            headersStr.append(": ");
            headersStr.append(messageHeaders.get(k));
            headersStr.append("\r\n");
        }
        byte[] headers = IOUtils.toUTF8Bytes(headersStr.toString());

        // Now calculate the size
        int size = 52 + headers.length;

        byte[] data = new byte[size];
        System.arraycopy(MAGIC_FISBONE_BYTES, 0, data, 0, 8);

        IOUtils.putInt4(data, 8, messageHeaderOffset);
        IOUtils.putInt4(data, 12, serialNumber);
        IOUtils.putInt4(data, 16, numHeaderPackets);
        IOUtils.putInt8(data, 20, granulerateNumerator);
        IOUtils.putInt8(data, 28, granulerateDenominator);
        IOUtils.putInt8(data, 36, baseGranule);
        IOUtils.putInt4(data, 44, preroll);
        data[48] = granuleShift;
        // Next 3 are zero padding

        // Finally the message headers
        System.arraycopy(headers, 0, data, 52, headers.length);

        setData(data);
        return super.write();
    }

    public int getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getNumHeaderPackets() {
        return numHeaderPackets;
    }
    public void setNumHeaderPackets(int numHeaderPackets) {
        this.numHeaderPackets = numHeaderPackets;
    }

    public long getGranulerateNumerator() {
        return granulerateNumerator;
    }
    public void setGranulerateNumerator(long granulerateNumerator) {
        this.granulerateNumerator = granulerateNumerator;
    }

    public long getGranulerateDenominator() {
        return granulerateDenominator;
    }
    public void setGranulerateDenominator(long granulerateDenominator) {
        this.granulerateDenominator = granulerateDenominator;
    }

    public long getBaseGranule() {
        return baseGranule;
    }
    public void setBaseGranule(long baseGranule) {
        this.baseGranule = baseGranule;
    }

    public int getPreroll() {
        return preroll;
    }
    public void setPreroll(int preroll) {
        this.preroll = preroll;
    }

    public byte getGranuleShift() {
        return granuleShift;
    }
    public void setGranuleShift(byte granuleShift) {
        this.granuleShift = granuleShift;
    }

    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
        messageHeaders.put(HEADER_CONTENT_TYPE, contentType);
    }

    /**
     * Provides read and write access to the Message Headers,
     *  which are used to describe the stream.
     * http://wiki.xiph.org/SkeletonHeaders provides documentation
     *  on the common headers, and the meaning of their values.
     */
    public Map<String, String> getMessageHeaders() {
        return messageHeaders;
    }
}
