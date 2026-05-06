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
package org.gagravarr.ogg.audio;

import java.util.List;
import java.util.Map;

import org.gagravarr.ogg.OggStreamPacket;

/**
 * Common interface for the Tags (Comments) header near
 *  the start of an {@link OggAudioStream}
 */
public interface OggAudioTagsHeader extends OggStreamPacket {
    public String getVendor();

    /**
     * Returns the (first) Artist, or null if no
     *  Artist tags present.
     */
    public String getArtist();
    /**
     * Returns the (first) Album, or null if no
     *  Album tags present.
     */
    public String getAlbum();
    /**
     * Returns the (first) Title, or null if no
     *  Title tags present.
     */
    public String getTitle();
    /**
     * Returns the (first) Genre, or null if no
     *  Genre tags present.
     */
    public String getGenre();
    /**
     * Returns the (first) track number as a literal
     *  string, eg "4" or "09", or null if
     *  no track number tags present;
     */
    public String getTrackNumber();
    /**
     * Returns the track number, as converted into
     *  an integer, or -1 if not available / not numeric
     */
    public int getTrackNumberNumeric();
    /**
     * Returns the (first) Date, or null if no
     *  Date tags present. Dates are normally stored
     *  in ISO8601 date format, i.e. YYYY-MM-DD
     */
    public String getDate();
    /**
     * Returns all comments for a given tag, in
     *  file order. Will return an empty list for
     *  tags which aren't present.
     */
    public List<String> getComments(String tag);
    /**
     * Returns all the comments, across all tags
     */
    public Map<String, List<String>> getAllComments();
}
