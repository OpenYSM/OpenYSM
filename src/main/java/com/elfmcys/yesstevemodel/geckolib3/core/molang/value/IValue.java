package com.elfmcys.yesstevemodel.geckolib3.core.molang.value;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions;

public interface IValue {
    Object evalUnsafe(ExpressionEvaluator<?> evaluator);

    default float evalAsFloat(ExpressionEvaluator<?> evaluator) {
        return ValueConversions.asFloat(evalSafe(evaluator));
    }

    default int evalAsInt(ExpressionEvaluator<?> evaluator) {
        return ValueConversions.asInt(evalSafe(evaluator));
    }

    default boolean evalAsBoolean(ExpressionEvaluator<?> evaluator) {
        return ValueConversions.asBoolean(evalSafe(evaluator));
    }

    default Object evalSafe(ExpressionEvaluator<?> evaluator) {
        try {
            return evalUnsafe(evaluator);
        } catch (Throwable th) {
            YesSteveModel.LOGGER.debug("Failed to evaluate molang expression.", th);
            return null;
        }
    }
}