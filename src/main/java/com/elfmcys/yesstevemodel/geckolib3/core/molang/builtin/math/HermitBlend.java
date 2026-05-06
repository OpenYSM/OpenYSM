package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import net.minecraft.util.Mth;

public class HermitBlend implements Function {
    @Override
    public Object evaluate(ExecutionContext<?> context, ArgumentCollection arguments) {
        double min = Mth.ceil(arguments.getAsFloat(context, 0));
        return Mth.floor((3.0d * Math.pow(min, 2.0d)) - (2.0d * Math.pow(min, 3.0d)));
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}