package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import org.joml.Vector3f;

public abstract class BoneKeyFrame {

    public final float startTick;
    public final float totalTick;
    public final float endTick;
    public final Vector3v beginPoint;

    public BoneKeyFrame(float startTick, float totalTick, Vector3v beginPoint) {
        this.startTick = startTick;
        this.totalTick = totalTick;
        this.endTick = startTick + totalTick;
        this.beginPoint = beginPoint;
    }

    public abstract Vector3f evaluate(ExpressionEvaluator<?> evaluator, float percentCompleted);

    public float getStartTick() {
        return this.startTick;
    }

    public float getTotalTick() {
        return this.totalTick;
    }

    public float getEndTick() {
        return this.endTick;
    }

    // TODO 可能存在精度问题
    // https://github.com/TartaricAcid/TouhouLittleMaid/blob/1.20/src/main/java/com/github/tartaricacid/touhoulittlemaid/geckolib3/core/keyframe/bone/BoneKeyFrame.java#L38
    public static boolean isBegin(float percentCompleted) {
        return percentCompleted < 0.00001f;
    }

    public static boolean isEnd(float percentCompleted) {
        return percentCompleted > 0.99999f;
    }
}