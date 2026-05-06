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
 * A high level stream packet sat atop
 *  of an OggPacket.
 * Provides support for reading and writing
 *  new and existing OggPacket instances.
 */
public abstract class HighLevelOggStreamPacket implements OggStreamPacket {
    private OggPacket oggPacket;
    private byte[] data;

    protected HighLevelOggStreamPacket(OggPacket oggPacket) {
        this.oggPacket = oggPacket;
    }
    protected HighLevelOggStreamPacket() {
        this.oggPacket = null;
    }

    protected OggPacket getOggPacket() {
        return oggPacket;
    }

    public byte[] getData() {
        if(data != null) {
            return data;
        }
        if(oggPacket != null) {
            return oggPacket.getData();
        }
        return null;
    }
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Returns the approximate number of bytes overhead
     *  from the underlying {@link OggPacket} / {@link OggPage}
     *  structures into which this data is stored.
     * <p>Will return 0 for packets not yet associated with a page.
     * <p>This information is normally only of interest to information,
     *  diagnostic and debugging tools.
     */
    public int getOggOverheadSize() {
        if (oggPacket != null) {
            return oggPacket.getOverheadBytes();
        } else {
            return 0;
        }
    }

    public OggPacket write() {
        this.oggPacket = new OggPacket(getData());
        return this.oggPacket;
    }
}
