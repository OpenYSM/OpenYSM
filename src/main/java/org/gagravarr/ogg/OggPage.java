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
package org.gagravarr.ogg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import static org.gagravarr.ogg.IOUtils.readOrEOF;

public class OggPage {
    private int sid;
    private int seqNum;
    private long checksum;
    private long granulePosition;

    private boolean isBOS;
    private boolean isEOS;
    private boolean isContinue;

    private int numLVs = 0;
    private byte[] lvs = new byte[255];
    private byte[] data;
    private ByteArrayOutputStream tmpData;

    protected OggPage(int sid, int seqNum) {
        this.sid = sid;
        this.seqNum = seqNum;
        this.tmpData = new ByteArrayOutputStream();
    }
    /**
     * InputStream should be positioned *just after*
     *  the OggS capture pattern.
     */
    protected OggPage(InputStream inp) throws IOException {
        int version = readOrEOF(inp);
        if(version != 0) {
            throw new UnsupportedOperationException("Found Ogg page in format " + version + " but we only support version 0");
        }

        int flags = readOrEOF(inp);
        if((flags & 0x01) == 0x01) {
            isContinue = true;
        }
        if((flags & 0x02) == 0x02) {
            isBOS = true;
        }
        if((flags & 0x04) == 0x04) {
            isEOS = true;
        }

        granulePosition = IOUtils.getInt(
                readOrEOF(inp), readOrEOF(inp), readOrEOF(inp), readOrEOF(inp),
                readOrEOF(inp), readOrEOF(inp), readOrEOF(inp), readOrEOF(inp)
        );
        sid = (int)IOUtils.getInt(
                readOrEOF(inp), readOrEOF(inp), readOrEOF(inp), readOrEOF(inp)
        );
        seqNum = (int)IOUtils.getInt(
                readOrEOF(inp), readOrEOF(inp), readOrEOF(inp), readOrEOF(inp)
        );
        checksum = IOUtils.getInt(
                readOrEOF(inp), readOrEOF(inp), readOrEOF(inp), readOrEOF(inp)
        );

        numLVs = readOrEOF(inp);
        lvs = new byte[numLVs];
        IOUtils.readFully(inp, lvs);

        data = new byte[ getDataSize() ];
        IOUtils.readFully(inp, data);
    }

    /**
     * Adds as much of the packet's data as
     *  we can do.
     */
    protected int addPacket(OggPacket packet, int offset) {
        if(packet.isBeginningOfStream()) {
            isBOS = true;
        }
        if(packet.isEndOfStream()) {
            isEOS = true;
        }

        // Add on in 255 byte chunks
        int size = packet.getData().length;
        for(int i = numLVs; i< 255; i++) {
            int remains = size - offset;

            int toAdd = 255;
            if(remains < 255) {
                toAdd = remains;
            }
            lvs[i] = IOUtils.fromInt(toAdd);
            tmpData.write(packet.getData(), offset, toAdd);

            numLVs++;
            offset += toAdd;
            if(toAdd < 255) {
                break;
            }
        }

        return offset;
    }

    /**
     * Is the checksum for the page valid?
     */
    public boolean isChecksumValid() {
        if(checksum == 0)
            return true;

        int crc = CRCUtils.getCRC(getHeader());
        if(data != null && data.length > 0) {
            crc = CRCUtils.getCRC(data, crc);
        }

        return (checksum == crc);
    }
    protected long getChecksum() {
        return checksum;
    }

    /**
     * Does this Page have space for the given
     *  number of bytes?
     */
    protected boolean hasSpaceFor(int bytes) {
        // Do we have enough lvs spare?
        // (Each LV holds up to 255 bytes, and we're
        //  not allowed more than 255 of them)
        int reqLVs = (int)Math.ceil(bytes / 255.0);

        if(numLVs + reqLVs > 255) {
            return false;
        }
        return true;
    }

    /**
     * Returns the minimum size of a page, which is 27
     *  bytes for the headers
     */
    public static int getMinimumPageSize() {
        return MINIMUM_PAGE_SIZE;
    }
    private static final int MINIMUM_PAGE_SIZE = 27;

    /**
     * How big is the page, including headers?
     */
    public int getPageSize() {
        // Header is 27 bytes + number of headers
        int size = MINIMUM_PAGE_SIZE + numLVs;
        // Data size is given by lvs
        size += getDataSize();
        return size;
    }
    /**
     * How big is the page, excluding headers?
     */
    public int getDataSize() {
        // Data size is given by lvs
        int size = 0;
        for(int i=0; i<numLVs; i++) {
            size += IOUtils.toInt(lvs[i]);
        }
        return size;
    }


    public int getSid() {
        return sid;
    }
    public int getSequenceNumber () {
        return seqNum;
    }
    public long getGranulePosition() {
        return granulePosition;
    }
    public byte[] getData() {
        if(tmpData != null) {
            if(data == null || tmpData.size() != data.length) {
                data = tmpData.toByteArray();
            }
        }
        return data;
    }

    protected void setGranulePosition(long position) {
        this.granulePosition = position;
    }

    /**
     * Is there a subsequent page containing the
     *  remainder of the packets?
     */
    public boolean hasContinuation() {
        // Has a continuation if the last LV
        //  is 255. 
        // Normally one would expect to have 
        //  the full 255 LVs, with the
        //  last one at 255, but technically
        //  you can force a continue without
        //  using all your LVs up
        if(numLVs == 0) {
            return false;
        }
        if(IOUtils.toInt( lvs[numLVs-1] ) == 255) {
            return true;
        }
        return false;
    }
    /**
     * Is this carrying on the packets from
     *  a previous page?
     */
    public boolean isContinuation() {
        return isContinue;
    }
    protected void setIsContinuation() {
        isContinue = true;
    }

    /**
     * This should only ever be called by
     *  {@link OggPacketWriter#close()} !
     */
    protected void setIsEOS() {
        isEOS = true;
    }

    /**
     * For unit testing only!
     */
    protected int getNumLVs() {
        return numLVs;
    }


    public void writeHeader(OutputStream out) throws IOException {
        byte[] header = getHeader();

        // Ensure we've moved from tmpdata to data
        getData();

        // Generate the checksum and store
        int crc = CRCUtils.getCRC(header);
        if(data != null && data.length > 0) {
            crc = CRCUtils.getCRC(data, crc);
        }
        IOUtils.putInt4(header, 22, crc);
        checksum = crc;

        // Write out
        out.write(header);
    }
    /**
     * Gets the header, but with a blank CRC field
     */
    protected byte[] getHeader() {
        byte[] header = new byte[MINIMUM_PAGE_SIZE + numLVs];
        header[0] = (byte)'O';
        header[1] = (byte)'g';
        header[2] = (byte)'g';
        header[3] = (byte)'S';

        header[4] = 0; // Version

        byte flags = 0;
        if(isContinue) {
            flags += 1;
        }
        if(isBOS) {
            flags += 2;
        }
        if(isEOS) {
            flags += 4;
        }
        header[5] = flags;

        IOUtils.putInt8(header, 6, granulePosition);
        IOUtils.putInt4(header, 14, sid);
        IOUtils.putInt4(header, 18, seqNum);

        // Checksum @ 22 left blank for now

        header[26] = IOUtils.fromInt(numLVs);
        System.arraycopy(lvs, 0, header, MINIMUM_PAGE_SIZE, numLVs);

        return header;
    }


    public String toString() {
        return "Ogg Page - " + getSid() + " @ " + getSequenceNumber() +
                " - " + numLVs + " LVs";
    }


    public OggPacketIterator getPacketIterator() {
        return new OggPacketIterator(null);
    }
    public OggPacketIterator getPacketIterator(OggPacketData previousPart) {
        return new OggPacketIterator(previousPart);
    }
    /**
     * Returns a full {@link OggPacket} if it can, otherwise
     *  just the {@link OggPacketData} if the rest of the
     *  packet is in another {@link OggPage}
     */
    protected class OggPacketIterator implements Iterator<OggPacketData> {
        private OggPacketData prevPart;
        private int currentLV = 0;
        private int currentOffset = 0;

        private OggPacketIterator(OggPacketData previousPart) {
            this.prevPart = previousPart;
        }

        public boolean hasNext() {
            if(currentLV < numLVs) {
                return true;
            }
            // Special case for an empty page
            if(currentLV == 0 && numLVs == 0) {
                return true;
            }

            return false;
        }

        public OggPacketData next() {
            boolean continues = false;
            int packetLVs = 0;
            int packetSize = 0;

            // How much data to we have?
            for(int i=currentLV; i< numLVs; i++) {
                int size = IOUtils.toInt( lvs[i] );
                packetSize += size;
                packetLVs++;

                if(size < 255) {
                    break;
                }
                if(i == (numLVs-1) && size == 255) {
                    continues = true;
                }
            }

            // Get the data
            byte[] pd = new byte[packetSize];
            for(int i=currentLV; i<(currentLV + packetLVs); i++) {
                int size = IOUtils.toInt( lvs[i] );
                int offset = (i-currentLV)*255;
                System.arraycopy(data, currentOffset+offset, pd, offset, size);
            }
            // Tack on anything spare from last time too
            if(prevPart != null) {
                int prevSize = prevPart.getData().length;
                byte[] fpd = new byte[prevSize+pd.length];
                System.arraycopy(prevPart.getData(), 0, fpd, 0, prevSize);
                System.arraycopy(pd, 0, fpd, prevSize, pd.length);

                prevPart = null;
                pd = fpd;
            }

            // Create
            OggPacketData packet;
            if(continues) {
                packet = new OggPacketData(pd);
            } else {
                boolean packetBOS = false;
                boolean packetEOS = false;
                if(isBOS && currentLV == 0) {
                    packetBOS = true;
                }
                if(isEOS && (currentLV+packetLVs) == numLVs) {
                    packetEOS = true;
                }

                packet = new OggPacket(OggPage.this, pd, packetBOS, packetEOS);
            }

            // Wind on
            currentLV += packetLVs;
            currentOffset += packetSize;
            // Empty page special case wind-on
            if(currentLV == 0)
                currentLV = 1;

            // Done!
            return packet;
        }

        public void remove() {
            throw new IllegalStateException("Remove not supported");
        }
    }
}
