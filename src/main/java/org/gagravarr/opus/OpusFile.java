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
package org.gagravarr.opus;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggPacketWriter;
import org.gagravarr.ogg.OggStreamIdentifier;
import org.gagravarr.ogg.OggStreamIdentifier.OggStreamType;
import org.gagravarr.ogg.audio.OggAudioHeaders;
import org.gagravarr.ogg.audio.OggAudioSetupHeader;
import org.gagravarr.ogg.audio.OggAudioStream;

/**
 * This is a wrapper around an OggFile that lets you
 *  get at all the interesting bits of an Opus file.
 */
public class OpusFile implements OggAudioStream, OggAudioHeaders, Closeable {
    private OggFile ogg;
    private OggPacketReader r;
    private OggPacketWriter w;
    private int sid = -1;

    private OpusInfo info;
    private OpusTags tags;

    private List<OpusAudioData> writtenPackets;
    private int maxPacketsPerPage = 50;

    /**
     * Opens the given file for reading
     */
    public OpusFile(File f) throws IOException, FileNotFoundException {
        this(new OggFile(new FileInputStream(f)));
    }
    /**
     * Opens the given file for reading
     */
    public OpusFile(OggFile ogg) throws IOException {
        this(ogg.getPacketReader());
        this.ogg = ogg;
    }
    /**
     * Loads a Opus File from the given packet reader.
     */
    public OpusFile(OggPacketReader r) throws IOException {	
        this.r = r;

        OggPacket p = null;
        while( (p = r.getNextPacket()) != null ) {
            if (p.isBeginningOfStream() && p.getData().length > 10) {
                if (OpusPacketFactory.isOpusStream(p)) {
                    sid = p.getSid();
                    break;
                }
            }
        }
        if (sid == -1) {
            throw new IllegalArgumentException("Supplied File is not Opus");
        }

        // First two packets are required to be info then tags
        info = (OpusInfo)OpusPacketFactory.create( p );
        tags = (OpusTags)OpusPacketFactory.create( r.getNextPacketWithSid(sid) );

        // Everything else should be audio data
    }

    /**
     * Opens for writing.
     */
    public OpusFile(OutputStream out) {
        this(out, new OpusInfo(), new OpusTags());   
    }
    /**
     * Opens for writing, based on the settings
     *  from a pre-read file. The Steam ID (SID) is
     *  automatically allocated for you.
     */
    public OpusFile(OutputStream out, OpusInfo info, OpusTags tags) {
        this(out, -1, info, tags);
    }
    /**
     * Opens for writing, based on the settings
     *  from a pre-read file, with a specific
     *  Steam ID (SID). You should only set the SID
     *  when copying one file to another!
     */
    public OpusFile(OutputStream out, int sid, OpusInfo info, OpusTags tags) {
        ogg = new OggFile(out);

        if(sid > 0) {
            w = ogg.getPacketWriter(sid);
            this.sid = sid;
        } else {
            w = ogg.getPacketWriter();
            this.sid = w.getSid();
        }

        writtenPackets = new ArrayList<OpusAudioData>();

        this.info = info;
        this.tags = tags;
    }

    public OpusAudioData getNextAudioPacket() throws IOException {
        OggPacket p = null;
        OpusPacket op = null;
        while( (p = r.getNextPacketWithSid(sid)) != null ) {
            op = OpusPacketFactory.create(p);
            if(op instanceof OpusAudioData) {
                return (OpusAudioData)op;
            } else {
                System.err.println("Skipping non audio packet " + op + " mid audio stream");
            }
        }
        return null;
    }

    /**
     * Skips the audio data to the next packet with a granule
     *  of at least the given granule position.
     * Note that skipping backwards is not currently supported!
     */
    public void skipToGranule(long granulePosition) throws IOException {
        r.skipToGranulePosition(sid, granulePosition);
    }

    /**
     * Returns the Ogg Stream ID
     */
    public int getSid() {
        return sid;
    }

    /**
     * This is an Opus file
     */
    public OggStreamType getType() {
        return OggStreamIdentifier.OPUS_AUDIO;
    }

    public OpusInfo getInfo() {
        return info;
    }
    public OpusTags getTags() {
        return tags;
    }
    /**
     * Opus doesn't have setup headers, so this is always null
     */
    public OggAudioSetupHeader getSetup() {
        return null;
    }

    /**
     * Sets the maximum number of opus packets per ogg page.
     * Shorter values will give less efficient storage, but
     *  more accurate Granule values.
     * Set to -1 if you are managing the ganule change when
     *  producing your {@link OpusAudioData} packets.
     */
    public void setMaxPacketsPerPage(int value) {
        maxPacketsPerPage = value;
    }

    public int getMaxPacketsPerPage() {
        return maxPacketsPerPage;
    }

    /**
     * Buffers the given audio ready for writing
     *  out. Data won't be written out yet, you
     *  need to call {@link #close()} to do that,
     *  because we assume you'll still be populating
     *  the Info/Comment/Setup objects
     */
    public void writeAudioData(OpusAudioData data) {
        writtenPackets.add(data);
    }

    /**
     * Same as {@link #writeAudioData(OpusAudioData)}
     * but replaces the whole list of packets at once
     */
    public void setAudioData(List<OpusAudioData> data) {
        writtenPackets = data;
    }

    /**
     * In Reading mode, will close the underlying ogg
     *  file and free its resources.
     * In Writing mode, will write out the Info and
     *  Tags objects, and then the audio data.
     */
    public void close() throws IOException {
        if (r != null) {
            r = null;
            ogg.close();
            ogg = null;
        }
        if (w != null) {
            w.bufferPacket(info.write(), true);
            w.bufferPacket(tags.write(), false);
            
            // The Granule Position on each Ogg Page needs to be
            //  the total number of PCM samples, including the last
            //  full Opus Packet in the page.
            // See https://wiki.xiph.org/OggOpus#Granule_Position

            final List<OpusAudioData> packets = writtenPackets;
            final int packetsSize = packets.size();
            final int maxPacketsPerPage = this.maxPacketsPerPage;

            OpusAudioData packet;
            int pageSize = 0;
            int pageSamples = 0;
            long lastGranule = 0;
            boolean doneFlush = false;
            boolean flushAfter = false;
            for (int i = 0; i < packetsSize; i++) {
                packet = packets.get(i);
                flushAfter = false;
                pageSize++;
                
                // Should we flush before this packet?
                if (maxPacketsPerPage == -1) {
                    // User is handling granule positions
                    // Do we need to flush for them?
                    if (packet.getGranulePosition() >= 0 &&
                            lastGranule != packet.getGranulePosition()) {
                        w.flush();
                        lastGranule = packet.getGranulePosition();
                        w.setGranulePosition(lastGranule);
                        doneFlush = true;
                    }
                } else {
                    // We are doing the granule position
                    
                    // Will we need to flush after this packet?
                    if (pageSize >= maxPacketsPerPage) {
                        flushAfter = true;
                    }
                
                    // Calculate the packet granule
                    pageSamples += packet.getNumberOfSamples();
                    packet.setGranulePosition(lastGranule+pageSamples);
                }

                // Write the data, flushing if needed
                w.bufferPacket(packet.write());
                if (flushAfter || w.getSizePendingFlush() > 16384) {
                    lastGranule = packet.getGranulePosition();
                    w.setGranulePosition(lastGranule);
                    
                    if (i != packetsSize-1) {
                        w.flush();
                        doneFlush = true;
                    }
                }
                if (doneFlush) {   
                    pageSize = 0;
                    pageSamples = 0;
                }
            }

            w.close();
            w = null;
            ogg.close();
            ogg = null;
        }
    }

    /**
     * Returns the underlying Ogg File instance
     * @return
     */
    public OggFile getOggFile() {
        return ogg;
    }
}