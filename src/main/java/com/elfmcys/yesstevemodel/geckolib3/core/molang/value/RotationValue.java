package com.elfmcys.yesstevemodel.geckolib3.core.molang.value;

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

public class RotationValue implements IValue {

    private final IValue inner;

    private final boolean inverse;

    public RotationValue(IValue value, boolean z) {
        this.inner = value;
        this.inverse = z;
    }

    public static float convert(float f, boolean z) {
        float radians = (float) Math.toRadians(f);
        if (z) {
            return -radians;
        }
        return radians;
    }

    @Override
    public float evalAsFloat(ExpressionEvaluator<?> evaluator) {
        return convert(this.inner.evalAsFloat(evaluator), this.inverse);
    }

    @Override
    public Object evalSafe(ExpressionEvaluator<?> evaluator) {
        return Float.valueOf(evalAsFloat(evaluator));
    }

    @Override
    public Object evalUnsafe(ExpressionEvaluator<?> evaluator) {
        return Float.valueOf(evalAsFloat(evaluator));
    }
}