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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class takes care of reading and writing
 *  files using the Ogg container format.
 */
public class OggFile implements Closeable {
    private InputStream inp;
    private OutputStream out;
    private boolean writing = true;

    private Set<Integer> seenSIDs = new HashSet<Integer>();

    /**
     * Opens a file for writing.
     * Call {@link #getPacketWriter()} to
     *  begin writing your data.
     */
    public OggFile(OutputStream output) {
        this.out = output;
        this.writing = true;
    }

    /**
     * Opens a file for reading in
     *  blocking (non event) mode.
     * Call {@link #getPacketReader()} to 
     *  begin reading the file.
     */
    public OggFile(InputStream input) {
        this.inp = input;
        this.writing = false;
    }

    /**
     * Opens a file for reading in non-blocking
     *  (event) mode.
     * Will begin processing the file and notifying
     *  your listener immediately.
     */
    public OggFile(InputStream input, OggStreamListener listener) throws IOException {
        this(input);

        Map<Integer,OggStreamReader[]> readers = new HashMap<Integer, OggStreamReader[]>();
        OggPacketReader reader = getPacketReader();
        OggPacket packet = null;
        while( (packet = reader.getNextPacket()) != null ) {
            if(packet.isBeginningOfStream()) {
                OggStreamReader[] streams = listener.processNewStream(packet.getSid(), packet.getData());
                if(streams != null && streams.length > 0) {
                    readers.put(packet.getSid(), streams);
                }
            } else {
                OggStreamReader[] streams = readers.get(packet.getSid());
                if(streams != null) {
                    for(OggStreamReader r : streams) {
                        r.processPacket(packet);
                    }
                }
            }
            if(packet.isEndOfStream()) {
                listener.processStreamEnd(packet.getSid());
            }
        }
    }


    /**
     * Closes our streams. It's up to you
     *  to close any {@link OggPacketWriter} instances
     *  first!
     */
    public void close() throws IOException {
        if(inp != null)
            inp.close();
        if(out != null)
            out.close();
    }

    /**
     * Returns a reader that will allow you to read packets
     *  from the file, across all Logical Bit Streams, 
     *  in the order that they occur.
     */
    public OggPacketReader getPacketReader() {
        if(writing || inp == null) {
            throw new IllegalStateException("Can only read from a file opened with an InputStream");
        }
        return new OggPacketReader(inp);
    }

    /**
     * Creates a new Logical Bit Stream in the file,
     *  and returns a Writer for putting data
     *  into it.
     */
    public OggPacketWriter getPacketWriter() {
        return getPacketWriter(getUnusedSerialNumber());
    }

    /**
     * Creates a new Logical Bit Stream in the file,
     *  and returns a Writer for putting data
     *  into it.
     */
    public OggPacketWriter getPacketWriter(int sid) {
        if(!writing) {
            throw new IllegalStateException("Can only write to a file opened with an OutputStream");
        }
        seenSIDs.add(sid);
        return new OggPacketWriter(this, sid);
    }

    /**
     * Writes a (possibly series) of pages to the
     *  stream in one go. 
     */
    protected synchronized void writePages(OggPage[] pages) throws IOException {
        for(OggPage page : pages) {
            page.writeHeader( out );
            out.write( page.getData() );
        }
        out.flush();
    }


    /**
     * Returns a random, but previously un-used serial
     *  number for use by a new stream
     */
    protected int getUnusedSerialNumber() {
        while(true) {
            int sid = (int)(Math.random() * Short.MAX_VALUE);
            if(! seenSIDs.contains(sid)) {
                return sid;
            }
        }
    }
}
