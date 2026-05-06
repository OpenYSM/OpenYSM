package com.elfmcys.yesstevemodel.client.animation.molang.struct;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

public record RoamingSyncBatch(int modelHashId, Int2FloatOpenHashMap changedVariables) {

    public RoamingSyncBatch(int modelHashId, int initialCapacity) {
        this(modelHashId, new Int2FloatOpenHashMap(initialCapacity));
    }
}