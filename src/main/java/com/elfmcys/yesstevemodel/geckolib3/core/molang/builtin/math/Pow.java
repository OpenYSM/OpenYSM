package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;

public class Pow implements Function {
    @Override
    public Object evaluate(ExecutionContext<?> context, ArgumentCollection arguments) {
        return Math.pow(arguments.getAsDouble(context, 0),
                arguments.getAsDouble(context, 1));
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 2;
    }
}