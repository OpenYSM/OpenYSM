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
package org.gagravarr.theora;

import org.gagravarr.ogg.OggStreamPacket;

/**
 * Parent of all Theora (video) packets
 */
public interface TheoraPacket extends OggStreamPacket {
    public static final int TYPE_IDENTIFICATION = 0x80;
    public static final int TYPE_COMMENTS = 0x81;
    public static final int TYPE_SETUP = 0x82;
}
