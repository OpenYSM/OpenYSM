package com.elfmcys.yesstevemodel.geckolib3.core.molang.value;

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

public class FloatValue implements IValue {

    public static final FloatValue ONE = new FloatValue(1.0f);

    public static final FloatValue ZERO = new FloatValue(0.0f);

    private final float value;

    public FloatValue(float value) {
        if (!Float.isNaN(value)) {
            this.value = value;
        } else {
            this.value = 0.0f;
        }
    }

    @Override
    public float evalAsFloat(ExpressionEvaluator<?> evaluator) {
        return this.value;
    }

    @Override
    public boolean evalAsBoolean(ExpressionEvaluator<?> evaluator) {
        return this.value != 0.0f;
    }

    @Override
    public Object evalSafe(ExpressionEvaluator<?> evaluator) {
        return this.value;
    }

    @Override
    public Object evalUnsafe(ExpressionEvaluator<?> evaluator) {
        return this.value;
    }

    public float value() {
        return this.value;
    }
}