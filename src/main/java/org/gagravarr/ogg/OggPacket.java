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

/**
 * Represents a logical group of data.
 * RFC3533 suggests that these should usually be
 *  around 50-200 bytes long.
 */
public class OggPacket extends OggPacketData {
    private OggPage parent; // Last page if split
    private boolean bos;
    private boolean eos;

    /**
     * Creates a new Ogg Packet based on data read
     *  from within an Ogg Page.
     */
    protected OggPacket(OggPage parent, byte[] data, boolean bos, boolean eos) {
        super(data);
        this.parent = parent;
        this.bos = bos;
        this.eos = eos;
    }
    /**
     * Creates a new Ogg Packet filled with data to
     *  be later written.
     * The Sid, and begin/end flags will be available
     *  after the packet has been flushed.
     */
    public OggPacket(byte[] data) {
        super(data);
    }

    protected void setParent(OggPage parent) {
        this.parent = parent;
    }
    protected void setIsBOS() {
        this.bos = true;
    }
    protected void setIsEOS() {
        this.eos = true;
    }

    /** Unit tests only! */
    protected OggPage _getParent() {
        return parent;
    }

    /**
     * Returns the Stream ID (Sid) that
     *  this packet belongs to.
     */
    public int getSid() {
        return parent.getSid();
    }
    /**
     * Returns the granule position of the page
     *  that this packet belongs to. The meaning
     *  of the granule depends on the codec.
     */
    public long getGranulePosition() {
        return parent.getGranulePosition();
    }
    /**
     * Returns the sequence number within the stream
     *  of the page that this packet belongs to.
     * You can use this to detect when pages have
     *  been lost.
     */
    public int getSequenceNumber() {
        return parent.getSequenceNumber();
    }

    /**
     * Returns the number of bytes overhead of the {@link OggPage}
     *  we belong to, if we're the only packet in the page, or
     *  a rough guess if we span multiple pages / share a page.
     */
    public int getOverheadBytes() {
        if (parent == null) return 0;

        double ourShare = 1.0;
        int ourDataLen = getData().length;
        int pageDataLen = parent.getDataSize();
        if (pageDataLen != ourDataLen) {
            // We don't have a page to ourselves, so we can't come up
            //  with a fully accurate overhead at this stage, so get close

            // Do we span multiple pages?
            if (ourDataLen > pageDataLen) {
                // Take a best guess, rounding up
                int approxPages = (int)Math.ceil(ourDataLen/pageDataLen);
                ourShare = approxPages;
            } else {
                // We're probably just a part of a larger page
                // Take a rough guess
                ourShare = ((double)ourDataLen) / pageDataLen;
            }
        }
        // Take the current page's overhead, scale as needed, and return
        return (int)Math.rint(ourShare * (parent.getPageSize() - pageDataLen));
    }

    /**
     * Is this the first packet in the stream?
     * If so, the data should hold the magic
     *  information required to identify which
     *  decoder will be needed.
     */
    public boolean isBeginningOfStream() {
        return bos;
    }
    /**
     * Is this the last packet in the stream?
     */
    public boolean isEndOfStream() {
        return eos;
    }
}
