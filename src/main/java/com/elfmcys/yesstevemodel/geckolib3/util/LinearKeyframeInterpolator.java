package com.elfmcys.yesstevemodel.geckolib3.util;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.List;

public class LinearKeyframeInterpolator implements IInterpolable {

    private final List<Segment> segments;

    private final InterpolationLookup<Segment> lookup;

    //"blend_transition": {
    //	"0.0": 1,
    //	"0.01": 0.896,
    //	"0.02": 0.648,
    //	"0.03": 0.352,
    //	"0.04": 0.104,
    //	"0.05": 0
    //}
    // var1=[0.0, 0.01, 0.02, 0.03, 0.04, 0.05]
    // var2=[1.0, 0.896, 0.648, 0.352, 0.104, 0.0]
    public LinearKeyframeInterpolator(float[] keys, float[] values) {
        ReferenceArrayList<Segment> segmentList = new ReferenceArrayList<>(keys.length - 1);

        for (int i = 0; i < keys.length - 1; i++) {
            float startTimeTick = keys[i] * 20.0f;
            float endTimeTick = keys[i + 1] * 20.0f;
            float startValue = 1.0f - values[i];
            float endValue = 1.0f - values[i + 1];

            segmentList.add(new Segment(startTimeTick, endTimeTick, startValue, endValue));
        }

        this.segments = segmentList;
        this.lookup = new InterpolationLookup<>(this.segments, 0.0f, controlPoint -> controlPoint.endTime);
    }

    private LinearKeyframeInterpolator(List<Segment> list) {
        this.segments = list;
        this.lookup = new InterpolationLookup<>(this.segments, 0.0f, controlPoint -> controlPoint.endTime);
    }

    @Override
    public float interpolate(float time) {
        Segment segment = this.lookup.getAtTime(time);
        if (time <= segment.startTime) {
            return segment.startValue;
        }
        if (time >= segment.endTime) {
            return segment.startValue + segment.valueDelta;
        }

        // 分段线性插值计算
        return segment.startValue + (segment.valueDelta * ((time - segment.startTime) / segment.duration));
    }

    @Override
    public float getProgress() {
        return this.lookup.getEndTime();
    }

    @Override
    public LinearKeyframeInterpolator asInterpolator() {
        return new LinearKeyframeInterpolator(this.segments);
    }

    private static class Segment {

        public final float startTime;

        public final float duration;

        public final float endTime;

        public final float startValue;

        public final float valueDelta;

        public Segment(float startTime, float endTime, float startValue, float endValue) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = endTime - startTime;
            this.startValue = startValue;
            this.valueDelta = endValue - startValue;
        }
    }
}