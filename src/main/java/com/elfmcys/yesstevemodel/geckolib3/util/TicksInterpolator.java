package com.elfmcys.yesstevemodel.geckolib3.util;

public class TicksInterpolator implements IInterpolable {

    private final float tickDuration;

    public TicksInterpolator(float f) {
        this.tickDuration = f * 20.0f;
    }

    @Override
    public float interpolate(float f) {
        if (this.tickDuration != 0.0f) {
            return f / this.tickDuration;
        }
        return 1.0f;
    }

    @Override
    public float getProgress() {
        return this.tickDuration;
    }
}