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
package org.gagravarr.skeleton;

import org.gagravarr.ogg.IOUtils;
import org.gagravarr.ogg.OggStreamPacket;

/**
 * Parent of all Skeleton (Annodex) packets
 */
public interface SkeletonPacket extends OggStreamPacket {
   public static final String MAGIC_FISHEAD_STR = "fishead\0";
   public static final String MAGIC_FISBONE_STR = "fisbone\0";
   public static final byte[] MAGIC_FISHEAD_BYTES = IOUtils.toUTF8Bytes(MAGIC_FISHEAD_STR);
   public static final byte[] MAGIC_FISBONE_BYTES = IOUtils.toUTF8Bytes(MAGIC_FISBONE_STR);
}
