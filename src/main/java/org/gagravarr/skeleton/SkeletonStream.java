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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gagravarr.ogg.OggPacket;

/**
 * A Skeleton Stream is made up of a single Fishead,
 *  one Fisbone per described stream, and optionally
 *  key frame data per stream.
 */
public class SkeletonStream {
    private int sid = -1;
    private boolean hasWholeStream;
    private SkeletonFishead fishead;
    private List<SkeletonFisbone> fisbones;
    private Map<Integer,SkeletonFisbone> bonesByStream;
    private List<SkeletonKeyFramePacket> keyFrames;

    /**
     * Starts tracking a new Skeleton Stream,
     *  from the given packet (which must hold
     *  the fishead)
     */
    public SkeletonStream(OggPacket packet) {
        this.sid = packet.getSid();
        this.hasWholeStream = false;
        this.fisbones = new ArrayList<SkeletonFisbone>();
        this.bonesByStream = new HashMap<Integer, SkeletonFisbone>();
        this.keyFrames = new ArrayList<SkeletonKeyFramePacket>();

        processPacket(packet);
    }
    /**
     * Creates a new Skeleton stream, with empty fisbones
     *  referencing the specified streams (by their stream ids /
     *  serial numbers)
     */
    public SkeletonStream(int[] sids) {
        this.fishead = new SkeletonFishead();
        for (int sid : sids) {
            addBoneForStream(sid);
        }
    }

    /**
     * Processes and tracks the next packet for
     *  the stream
     */
    public void processPacket(OggPacket packet) {
        SkeletonPacket skel = SkeletonPacketFactory.create(packet);

        // First packet must be the head
        if (packet.isBeginningOfStream()) {
            fishead = (SkeletonFishead)skel;
        } else if (skel instanceof SkeletonFisbone) {
            SkeletonFisbone bone = (SkeletonFisbone)skel;
            fisbones.add(bone);
            bonesByStream.put(bone.getSerialNumber(), bone);
        } else if (skel instanceof SkeletonKeyFramePacket) {
            keyFrames.add((SkeletonKeyFramePacket)skel);
        } else {
            throw new IllegalStateException("Unexpected Skeleton " + skel);
        }

        if (packet.isEndOfStream()) {
            hasWholeStream = true;
        }
    }

    /**
     * Returns the Ogg Stream ID of the Skeleton
     */
    public int getSid() {
        return sid;
    }

    /**
     * Have all the packets in the Skeleton stream
     *  been received and processed yet?
     */
    public boolean hasWholeStream() {
        return hasWholeStream;
    }

    public SkeletonFishead getFishead() {
        return fishead;
    }

    /**
     * Get all known fisbones
     */
    public List<SkeletonFisbone> getFisbones() {
        return fisbones;
    }

    /**
     * Get the fisbone for a given stream, or null if
     *  the stream isn't described
     */
    public SkeletonFisbone getBoneForStream(int sid) {
        return bonesByStream.get(sid);
    }

    /**
     * Adds a new fisbone for the given stream
     */
    public SkeletonFisbone addBoneForStream(int sid) {
        SkeletonFisbone bone = new SkeletonFisbone();
        bone.setSerialNumber(sid);
        fisbones.add(bone);

        if (sid == -1 || bonesByStream.containsKey(sid)) {
            throw new IllegalArgumentException("Invalid / duplicate sid " + sid);
        }
        bonesByStream.put(sid, bone);

        return bone;
    }

    /**
     * Get all known key frames
     */
    public List<SkeletonKeyFramePacket> getKeyFrames() {
        return keyFrames;
    }
}
