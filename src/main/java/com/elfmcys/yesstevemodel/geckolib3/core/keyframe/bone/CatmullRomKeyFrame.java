package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import org.joml.Vector3f;

public class CatmullRomKeyFrame extends BoneKeyFrame {

    private final Vector3v leftPoint;

    private final Vector3v endPoint;

    private final Vector3v rightPoint;

    private final Vector3v postPoint;

    public CatmullRomKeyFrame(float startTick, float totalTick, Vector3v leftPoint, Vector3v current, Vector3v endPoint, Vector3v postRight, Vector3v postPoint) {
        super(startTick, totalTick, current);
        this.leftPoint = leftPoint;
        this.endPoint = endPoint;
        this.rightPoint = postRight;
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
        return MathUtil.catmullRom(percentCompleted, this.leftPoint.eval(evaluator), this.beginPoint.eval(evaluator), this.endPoint.eval(evaluator), this.rightPoint.eval(evaluator));
    }
}