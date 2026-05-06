package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import org.joml.Vector3f;

public class ConstantPoint extends AnimationPoint {

    public final Vector3f value;

    public ConstantPoint(float currentTick, float totalTick, Vector3f value, AnimationControllerContext context) {
        super(currentTick, totalTick, context);
        this.value = value;
    }

    @Override
    public float getPercentCompleted() {
        if (this.totalTick == 0.0f) {
            return this.currentTick == 0.0f ? 0.0f : 1.0f;
        }
        return this.currentTick / this.totalTick;
    }

    @Override
    public Vector3f getLerpPoint(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        if (this.cachedValue == null) {
            this.cachedValue = new Vector3f(this.value);
        } else {
            this.cachedValue.set(this.value);
        }
        return this.value;
    }
}