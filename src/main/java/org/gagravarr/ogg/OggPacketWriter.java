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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public class OggPacketWriter implements Closeable {
    private boolean closed = false;
    private boolean doneFirstPacket = false;
    private OggFile file;
    private int sid;
    private int sequenceNumber;
    private long currentGranulePosition = 0;

    private ArrayList<OggPage> buffer =
            new ArrayList<OggPage>();

    protected OggPacketWriter(OggFile parentFile, int sid) {
        this.file = parentFile;
        this.sid = sid;

        this.sequenceNumber = 0;
    }

    /**
     * Sets the current granule position.
     * The granule position will be applied to all
     *  un-flushed packets, and all future packets.
     * As such, you should normally either call a flush
     *  just before or just after this call. 
     */
    public void setGranulePosition(long position) {
        currentGranulePosition = position;
        for(OggPage p : buffer) {
            p.setGranulePosition(position);
        }
    }
    public long getCurrentGranulePosition() {
        return currentGranulePosition;
    }

    public int getSid() {
        return sid;
    }

    private OggPage getCurrentPage(boolean forceNew) {
        if(buffer.size() == 0 || forceNew) {
            OggPage page = new OggPage(sid, sequenceNumber++); 
            if(currentGranulePosition > 0) {
                page.setGranulePosition(currentGranulePosition);
            }
            buffer.add( page );
            return page;
        }
        return buffer.get( buffer.size()-1 );
    }

    /**
     * Buffers the given packet up ready for
     *  writing to the stream, but doesn't
     *  write it to disk yet. The granule
     *  position is unchanged.
     */
    public void bufferPacket(OggPacket packet) {
        bufferPacket(packet, currentGranulePosition);
    }
    /**
     * Buffers the given packet up ready for
     *  writing to the stream, but doesn't
     *  write it to disk yet. The granule position
     *  is updated on the page.
     * If writing the packet requires a new page,
     *  then the updated granule position only
     *  applies to the new page
     */
    public void bufferPacket(OggPacket packet, long granulePosition) {
        if(closed) {
            throw new IllegalStateException("Can't buffer packets on a closed stream!");
        }
        if(! doneFirstPacket) {
            packet.setIsBOS();
            doneFirstPacket = true;
        }

        int size = packet.getData().length;
        boolean emptyPacket = (size==0);

        // Add to pages in turn
        OggPage page = getCurrentPage(false);
        int pos = 0;
        while( pos < size || emptyPacket) {
            pos = page.addPacket(packet, pos);
            if(pos < size) {
                page = getCurrentPage(true);
                page.setIsContinuation();
            }
            page.setGranulePosition(granulePosition);
            emptyPacket = false;
        }
        currentGranulePosition = granulePosition;
        packet.setParent(page);
    }

    /**
     * Buffers the given packet up ready for
     *  writing to the file, and then writes
     *  it to the stream if indicated.
     */
    public void bufferPacket(OggPacket packet, boolean flush) throws IOException {
        bufferPacket(packet);
        if(flush) {
            flush();
        }
    }

    /**
     * Returns the number of bytes (excluding headers)
     *  currently waiting to be written to disk.
     * RFC 3533 suggests that pages should normally 
     *  be in the 4-8kb range.
     * If this size exceeds just shy of 64kb, then
     *  multiple pages will be needed in the underlying
     *  stream.
     */
    public int getSizePendingFlush() {
        int size = 0;
        for(OggPage p : buffer) {
            size += p.getDataSize();
        }
        return size;
    }

    /**
     * Returns the size of the page currently being written
     *  to, including its headers.
     * For a new stream, or a stream that has just been
     *  flushed, will return zero.
     * @return Current page size, or 27 (the minimum) if no current page
     */
    public int getCurrentPageSize() {
        if (buffer.isEmpty()) return OggPage.getMinimumPageSize();

        OggPage p = buffer.get( buffer.size()-1 );
        return p.getPageSize();
    }

    /**
     * Writes all pending packets to the stream,
     *  splitting across pages as needed.
     */
    public void flush() throws IOException {
        if(closed) {
            throw new IllegalStateException("Can't flush packets on a closed stream!");
        }

        // Write in one go
        OggPage[] pages = buffer.toArray(new OggPage[buffer.size()]); 
        file.writePages(pages);

        // Get ready for next time!
        buffer.clear();
    }

    /**
     * Writes all pending packets to the stream,
     *  with the last one containing the End Of Stream
     *  Flag, and then closes down.
     */
    public void close() throws IOException {
        if(buffer.size() > 0) {
            buffer.get( buffer.size()-1 ).setIsEOS();
        } else {
            OggPacket p = new OggPacket(new byte[0]);
            p.setIsEOS();
            bufferPacket(p);
        }
        flush();

        closed = true;
    }
}
