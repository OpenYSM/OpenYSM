package com.elfmcys.yesstevemodel.geckolib3.core.util;

public class RateLimiter {

    private float interval = 0.008333334f;

    private float aggregate = 1.0f;

    private float lastRequestTime = 0.0f;

    /**
     * 设置动画的目标帧率
     * @param limitPerSec 每秒更新的帧数 (例如: 24, 30, 60)
     */
    public void setRefreshRate(int limitPerSec) {
        this.interval = 1.0f / limitPerSec;
    }

    public boolean request(float time) {
        this.aggregate += time - this.lastRequestTime;
        this.lastRequestTime = time;
        if (this.aggregate < this.interval) {
            return false;
        }
        this.aggregate %= this.interval;
        return true;
    }

    public float getInterval() {
        return this.interval;
    }

    public void reset() {
        this.aggregate = this.interval;
        this.lastRequestTime = 0.0f;
    }
}