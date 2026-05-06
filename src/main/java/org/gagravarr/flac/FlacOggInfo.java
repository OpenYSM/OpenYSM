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
package org.gagravarr.flac;

import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.audio.OggAudioInfoHeader;

/**
 * The {@link FlacInfo} plus the version data from
 *  {@link FlacFirstOggPacket}
 */
public class FlacOggInfo extends FlacInfo implements OggAudioInfoHeader {
    private FlacFirstOggPacket parent;

    /**
     * Creates a new, empty info
     */
    public FlacOggInfo() {
        super();
    }
    /**
     * Reads the Info from the specified data
     */
    public FlacOggInfo(byte[] data, int offset, FlacFirstOggPacket parent) {
        super(data, offset);
        this.parent = parent;
    }
    /**
     * Supplies the FlacFirstOggPacket for a new info
     */
    protected void setFlacFirstOggPacket(FlacFirstOggPacket parent) {
        this.parent = parent;
    }

    /**
     * The version comes from the parent packet
     */
    public String getVersionString() {
        return parent.getMajorVersion() + "." + parent.getMinorVersion();
    }

    /**
     * Data setting directly not supported
     */
    public void setData(byte[] data) {
        throw new IllegalStateException("Not supported for FLAC");
    }
    /**
     * The overhead comes from the parent packet
     */
    public int getOggOverheadSize() {
        return parent.getOggOverheadSize();
    }
    /**
     * Data writing passes through to the parent packet
     */
    public OggPacket write() {
        return parent.write();
    }
}
