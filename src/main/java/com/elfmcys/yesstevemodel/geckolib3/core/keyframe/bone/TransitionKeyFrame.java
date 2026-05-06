package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import org.joml.Vector3f;

public class TransitionKeyFrame extends BoneKeyFrame {

    private final Vector3v postPoint;

    public TransitionKeyFrame(float firstStartTick, Vector3v firstPoint, Vector3v postPoint) {
        super(0.0f, firstStartTick, firstPoint);
        this.postPoint = postPoint;
    }

    @Override
    public Vector3f evaluate(ExpressionEvaluator<?> evaluator, float percentCompleted) {
        if (!isEnd(percentCompleted)) {
            return this.beginPoint.eval(evaluator);
        }
        return this.postPoint.eval(evaluator);
    }

    public Vector3f evaluate(ExpressionEvaluator<?> evaluator) {
        return this.beginPoint.eval(evaluator);
    }
}