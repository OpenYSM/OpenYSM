package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import org.joml.Vector3f;

public class LinearKeyFrame extends BoneKeyFrame {

    private final Vector3v endPoint;

    private final Vector3v postPoint;

    public LinearKeyFrame(float startTick, float totalTick, Vector3v beginPoint, Vector3v endPoint, Vector3v postPoint) {
        super(startTick, totalTick, beginPoint);
        this.endPoint = endPoint;
        this.postPoint = postPoint;
    }

    @Override
    public Vector3f evaluate(ExpressionEvaluator<?> evaluator, float percentCompleted) {
        if (isBegin(percentCompleted)) {
            return this.beginPoint.eval(evaluator);
        }
        if (isEnd(percentCompleted)) {
            return this.postPoint.eval(evaluator);
        }
        return MathUtil.lerpValues(percentCompleted, this.beginPoint.eval(evaluator), this.endPoint.eval(evaluator));
    }
}