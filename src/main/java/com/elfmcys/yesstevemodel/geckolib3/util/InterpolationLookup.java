package com.elfmcys.yesstevemodel.geckolib3.util;

import java.util.List;

public class InterpolationLookup<T> {

    private final List<T> keyframes;

    private final float startTime;

    private final float endTime;

    private final FrameTimeProvider<T> timeProvider;

    private int currentIndex = 0;

    private float f3;

    private float f4;

    @FunctionalInterface
    public interface FrameTimeProvider<T> {
        float apply(T t);
    }

    public InterpolationLookup(List<T> list, float f, FrameTimeProvider<T> frameTimeProvider) {
        this.keyframes = list;
        this.startTime = f;
        this.endTime = frameTimeProvider.apply(list.get(list.size() - 1));
        this.timeProvider = frameTimeProvider;
        this.f3 = f;
        this.f4 = this.timeProvider.apply(list.get(0));
        if (f > this.f4 || f > this.endTime || this.f4 > this.endTime) {
            throw new IllegalArgumentException();
        }
    }

    public T getAtTime(float f) {
        if (this.keyframes.size() == 1) {
            return this.keyframes.get(0);
        }
        if (f < this.f3) {
            T t = this.keyframes.get(0);
            if (this.currentIndex == 0) {
                return t;
            }
            this.currentIndex = 0;
            this.f3 = this.startTime;
            this.f4 = this.timeProvider.apply(t);
            if (f >= this.f4) {
                return getAtTime(f);
            }
            return t;
        }
        if (f == this.f3 || f < this.f4 || this.f4 == this.endTime) {
            return this.keyframes.get(this.currentIndex);
        }
        if (f >= this.endTime) {
            T t2 = this.keyframes.get(this.keyframes.size() - 1);
            float fApply = this.timeProvider.apply(this.keyframes.get(this.keyframes.size() - 2));
            float fApply2 = this.timeProvider.apply(t2);
            if (fApply > fApply2) {
                throw new IllegalArgumentException();
            }
            this.currentIndex = this.keyframes.size() - 1;
            this.f3 = fApply;
            this.f4 = fApply2;
            return t2;
        }
        float f2 = this.f4;
        int i = this.currentIndex + 1;
        while (true) {
            T t3 = this.keyframes.get(i);
            float fApply3 = this.timeProvider.apply(t3);
            if (f2 > fApply3) {
                throw new IllegalArgumentException();
            }
            if (f < fApply3) {
                this.currentIndex = i;
                this.f3 = f2;
                this.f4 = fApply3;
                return t3;
            }
            f2 = fApply3;
            i++;
        }
    }

    public float getStartTime() {
        return this.startTime;
    }

    public float getEndTime() {
        return this.endTime;
    }
}