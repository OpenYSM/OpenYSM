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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class OggPacketReader {
    private InputStream inp;
    private Iterator<OggPacketData> it;
    private OggPacket nextPacket;

    public OggPacketReader(InputStream inp) {
        this.inp = inp;
    }

    /**
     * Returns the next packet in the file, or
     *  null if no more packets remain.
     * Call {@link OggPacket#isBeginningOfStream()}
     *  to detect if it is the first packet in the
     *  stream or not, and use
     *  {@link OggPacket#getSid()} to track which
     *  stream it belongs to.
     */
    public OggPacket getNextPacket() throws IOException {
        // If we skipped to a point in the stream, and
        //  have a packet waiting, return that
        if(nextPacket != null) {
            OggPacket p = nextPacket;
            nextPacket = null;
            return p;
        }

        // If we're already part way through a page,
        //  then fetch the next packet. If it's a
        //  full one, then we're done.
        OggPacketData leftOver = null;
        if(it != null && it.hasNext()) {
            OggPacketData packet = it.next();
            if(packet instanceof OggPacket) {
                return (OggPacket)packet;
            }
            leftOver = packet;
        }

        // Find the next page, from which
        //  to get our next packet from
        int searched = 0;
        int pos = -1;
        boolean found = false;
        int r;
        while(searched < 65536 && !found) {
            r = inp.read();
            if(r == -1) {
                // No more data
                return null;
            }

            switch(pos) {
            case -1:
                if(r == (int)'O') {
                    pos = 0;
                }
                break;
            case 0:
                if(r == (int)'g') {
                    pos = 1;
                } else {
                    pos = -1;
                }
                break;
            case 1:
                if(r == (int)'g') {
                    pos = 2;
                } else {
                    pos = -1;
                }
                break;
            case 2:
                if(r == (int)'S') {
                    found = true;
                } else {
                    pos = -1;
                }
                break;
            }

            if(!found) {
                searched++;
            }
        }

        if(!found) {
            throw new IOException("Next ogg packet header not found after searching " + searched + " bytes");
        }

        searched -= 3; // OggS
        if(searched > 0) {
            System.err.println("Warning - had to skip " + searched + " bytes of junk data before finding the next packet header");
        }

        // Create the page, and prime the iterator on it
        try {
            OggPage page = new OggPage(inp);
            if (!page.isChecksumValid()) {
                System.err.println("Warning - invalid checksum on page " +
                                   page.getSequenceNumber() + " of stream " +
                                   Integer.toHexString(page.getSid()) + " (" +
                                   page.getSid() + ")");
            }
            it = page.getPacketIterator(leftOver);
            return getNextPacket();
        } catch (EOFException eof) {
            System.err.println("Warning - data ended mid-page: " + eof.getMessage());
            return null;
        }
    }

    /**
     * Returns the next packet with the given SID (Stream ID), or
     *  null if no more packets remain.
     * Any packets from other streams will be silently discarded.
     */
    public OggPacket getNextPacketWithSid(int sid) throws IOException {
        OggPacket p = null;
        while( (p = getNextPacket()) != null ) {
            if(p.getSid() == sid) {
                return p;
            }
        }
        return null;
    }

    /**
     * Un-reads a packet, leaving it ready to be feteched by the
     *  next call to {@link #getNextPacket()}.
     * Only one packet may be unread.
     * Normally used when identifying a stream, to leave the
     *  initial packet ready for a decoder 
     */
    public void unreadPacket(OggPacket packet) {
        if(nextPacket != null) {
            throw new IllegalStateException("Can't un-read twice");
        }
        nextPacket = packet;
    }

    /**
     * Skips forward until the first packet with a Sequence Number
     *  of equal or greater than that specified. Call {@link #getNextPacket()}
     *  to retrieve this packet.
     * This method advances across all streams, but only searches the
     *  specified one.
     * @param sid The ID of the stream who's packets we will search
     * @param sequenceNumber The sequence number we're looking for
     */
    public void skipToSequenceNumber(int sid, int sequenceNumber) throws IOException {
        OggPacket p = null;
        while( (p = getNextPacket()) != null ) {
            if(p.getSid() == sid && p.getSequenceNumber() >= sequenceNumber) {
                nextPacket = p;
                break;
            }
        }
    }

    /**
     * Skips forward until the first packet with a Granule Position
     *  of equal or greater than that specified. Call {@link #getNextPacket()}
     *  to retrieve this packet.
     * This method advances across all streams, but only searches the
     *  specified one.
     * @param sid The ID of the stream who's packets we will search
     * @param granulePosition The granule position we're looking for
     */
    public void skipToGranulePosition(int sid, long granulePosition) throws IOException {
        OggPacket p = null;
        while( (p = getNextPacket()) != null ) {
            if(p.getSid() == sid && p.getGranulePosition() >= granulePosition) {
                nextPacket = p;
                break;
            }
        }
    }
}
