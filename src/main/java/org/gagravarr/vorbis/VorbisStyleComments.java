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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.gagravarr.ogg.HighLevelOggStreamPacket;
import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioTagsHeader;

/**
 * General class for all Vorbis-style comments/tags, as used
 *  by things like Vorbis, Opus and FLAC.
 */
public abstract class VorbisStyleComments extends HighLevelOggStreamPacket implements OggAudioTagsHeader {
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_ALBUM = "album";
    public static final String KEY_TITLE = "title";
    public static final String KEY_GENRE = "genre";
    public static final String KEY_TRACKNUMBER = "tracknumber";
    public static final String KEY_DATE = "date";

    private String vendor;
    private Map<String, List<String>> comments =
                             new HashMap<String, List<String>>();

    public VorbisStyleComments(OggPacket pkt, int dataBeginsAt) {
        super(pkt);
        byte[] d = pkt.getData();

        int vlen = getInt4(d, dataBeginsAt);
        vendor = IOUtils.getUTF8(d, dataBeginsAt+4, vlen);

        int offset = dataBeginsAt + 4 + vlen;
        int numComments = getInt4(d, offset);
        offset += 4;

        for(int i=0; i<numComments; i++) {
            int len = getInt4(d, offset);
            offset += 4;
            String c = IOUtils.getUTF8(d, offset, len);
            offset += len;

            int equals = c.indexOf('=');
            if(equals == -1) {
                System.err.println("Warning - unable to parse comment '"+c+"'");
            } else {
                String tag = normaliseTag(c.substring(0, equals));
                String value = c.substring(equals+1);
                addComment(tag, value);
            }
        }

        if(offset < d.length && hasFramingBit()) {
            byte framingBit = d[offset];
            if(framingBit == 0) {
                throw new IllegalArgumentException("Framing bit not set, invalid");
            }
        }
    }

    public VorbisStyleComments() {
        super();
        vendor = "Gagravarr.org Java Vorbis Tools v0.8 20160217";
    }

    public String getVendor() {
        return vendor;
    }
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    /**
     * The tag name is case-insensitive and may consist of ASCII 0x20 
     *  through 0x7D, 0x3D (’=’) excluded. ASCII 0x41 through 0x5A 
     *  inclusive (characters A-Z) is to be considered equivalent to 
     *  ASCII 0x61 through 0x7A inclusive (characters a-z).
     */
    protected static String normaliseTag(String tag) {
        StringBuffer nt = new StringBuffer();
        for(char c : tag.toLowerCase(Locale.ROOT).toCharArray()) {
            if((int)c >= 0x20 && (int)c <= 0x7d &&
                    (int)c != 0x3d) {
                nt.append(c);
            }
        }
        return nt.toString();
    }

    protected String getSingleComment(String normalisedTag) {
        List<String> c = comments.get(normalisedTag);
        if(c != null && c.size() > 0) {
            return c.get(0);
        }
        return null;
    }


    /**
     * Returns the (first) Artist, or null if no
     *  Artist tags present.
     */
    public String getArtist() {
        return getSingleComment(KEY_ARTIST);
    }
    /**
     * Returns the (first) Album, or null if no
     *  Album tags present.
     */
    public String getAlbum() {
        return getSingleComment(KEY_ALBUM);
    }
    /**
     * Returns the (first) Title, or null if no
     *  Title tags present.
     */
    public String getTitle() {
        return getSingleComment(KEY_TITLE);
    }
    /**
     * Returns the (first) Genre, or null if no
     *  Genre tags present.
     */
    public String getGenre() {
        return getSingleComment(KEY_GENRE);
    }
    /**
     * Returns the (first) track number as a literal
     *  string, eg "4" or "09", or null if
     *  no track number tags present;
     */
    public String getTrackNumber() {
        return getSingleComment(KEY_TRACKNUMBER);
    }
    /**
     * Returns the track number, as converted into
     *  an integer, or -1 if not available / not numeric
     */
    public int getTrackNumberNumeric() {
        String number = getTrackNumber();

        if(number == null) return -1;
        try {
            return Integer.parseInt(number);
        } catch(NumberFormatException e) {
            return -1;
        }
    }
    /**
     * Returns the (first) Date, or null if no
     *  Date tags present. Dates are normally stored
     *  in ISO8601 date format, i.e. YYYY-MM-DD
     */
    public String getDate() {
        return getSingleComment("date");
    }

    /**
     * Returns all comments for a given tag, in
     *  file order. Will return an empty list for
     *  tags which aren't present.
     */
    public List<String> getComments(String tag) {
        List<String> c = comments.get( normaliseTag(tag) );
        if(c == null) {
            return new ArrayList<String>();
        } else {
            return c;
        }
    }

    /**
     * Removes all comments for a given tag.
     */
    public void removeComments(String tag) {
        comments.remove( normaliseTag(tag) );
    }
    /**
     * Removes all comments across all tags
     */
    public void removeAllComments() {
        comments.clear();
    }

    /**
     * Adds a comment for a given tag
     */
    public void addComment(String tag, String comment) {
        String nt = normaliseTag(tag);
        if(! comments.containsKey(nt)) {
            comments.put(nt, new ArrayList<String>());
        }
        comments.get(nt).add(comment);
    }
    /**
     * Removes any existing comments for a given tag,
     *  and replaces them with the supplied list
     */
    public void setComments(String tag, List<String> comments) {
        String nt = normaliseTag(tag);
        if(this.comments.containsKey(nt)) {
            this.comments.remove(nt);
        }
        this.comments.put(nt, comments);
    }


    /**
     * Returns all the comments
     */
    public Map<String, List<String>> getAllComments() {
        return comments;
    }

    protected abstract int getHeaderSize();
    protected abstract boolean hasFramingBit();
    protected abstract void populateMetadataHeader(byte[] data, int packetLength);
    protected abstract void populateMetadataFooter(OutputStream out);

    protected int getInt4(byte[] d, int offset) {
        return (int)IOUtils.getInt4(d, offset);
    }

    @Override
    public OggPacket write() {
        // Serialise the comments
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Pad for the header (size isn't known yet, so can't fully write)
            byte[] headerPadding = new byte[getHeaderSize()];
            baos.write(headerPadding);

            // Do the vendor string
            IOUtils.writeUTF8WithLength(baos, vendor);

            // Next is the number of comments
            int numComments = 0;
            for(List<String> c : comments.values()) {
                numComments += c.size();
            }
            IOUtils.writeInt4(baos, numComments);

            // Write out the tags. While the spec doesn't require
            //  an order, unit testing does!
            String[] tags = comments.keySet().toArray(new String[comments.size()]);
            Arrays.sort(tags);
            for(String tag : tags) {
                for(String value : comments.get(tag)) {
                    String comment = tag + '=' + value;

                    IOUtils.writeUTF8WithLength(baos, comment);
                }
            }

            // Do a header, if required for the format
            populateMetadataFooter(baos);
        } catch(IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }

        // Now fill in the header
        byte[] data = baos.toByteArray();
        populateMetadataHeader(data, data.length);

        // Record the data
        setData(data);

        // Now write
        return super.write();
    }
}
