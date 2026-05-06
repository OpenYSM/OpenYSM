package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.TransitionKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import org.joml.Vector3f;

public class TransitionPoint extends AnimationPoint {

    public final float lerpFactor;

    public final Vector3f offsetPoint;

    public final TransitionKeyFrame dstKeyframe;

    public TransitionPoint(float currentTick, float lerpFactor, float totalTick, Vector3f offsetPoint, TransitionKeyFrame dstKeyframe, AnimationControllerContext context) {
        super(currentTick, totalTick, context);
        this.lerpFactor = lerpFactor;
        this.offsetPoint = offsetPoint;
        this.dstKeyframe = dstKeyframe;
    }

    @Override
    public Vector3f getLerpPoint(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        setupControllerContext(evaluator);
        Vector3f vector3f = this.dstKeyframe.evaluate(evaluator);
        MathUtil.lerpValues(this.lerpFactor, this.offsetPoint, vector3f, vector3f);
        if (this.cachedValue == null) {
            this.cachedValue = new Vector3f(vector3f);
        } else {
            this.cachedValue.set(vector3f);
        }
        return vector3f;
    }

    public Vector3f evaluateRaw(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        setupControllerContext(evaluator);
        return this.dstKeyframe.evaluate(evaluator);
    }

    public Vector3f getOffsetPoint() {
        return this.offsetPoint;
    }

    public float getLerpFactor() {
        return this.lerpFactor;
    }
}