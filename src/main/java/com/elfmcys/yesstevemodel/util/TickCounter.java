package com.elfmcys.yesstevemodel.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public class TickCounter {

    private final LongArrayList timestamps;

    private final int maxCount;

    private long windowMs;

    public TickCounter(int i, float f) {
        this.timestamps = new LongArrayList(i);
        this.maxCount = Math.round(1000.0f / f);
        for (int i2 = 0; i2 < i; i2++) {
            this.timestamps.add(0L);
        }
    }

    public boolean tryIncrement() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (jCurrentTimeMillis < this.windowMs) {
            return false;
        }
        LongArrayList longArrayList = this.timestamps;
        longArrayList.removeLong(longArrayList.size() - 1);
        longArrayList.add(0, jCurrentTimeMillis);
        long j = this.maxCount;
        long jMin = Long.MAX_VALUE;
        for (int i = 0; i < longArrayList.size(); i++) {
            jMin = Math.min(jMin, longArrayList.getLong(i) + (j * (i + 1)));
        }
        this.windowMs = jMin;
        return true;
    }
}