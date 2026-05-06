package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;

public class Random extends ContextFunction<Object> {
    @Override
    public boolean validateArgumentSize(int size) {
        return size == 2 || size == 3;  // 出于兼容性考虑，允许3参数
    }

    @Override
    protected Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
        float min = arguments.getAsFloat(context, 0);
        float range = arguments.getAsFloat(context, 1);
        if(min > range) {
            float temp = min;
            min = range;
            range = temp - range;
        } else {
            range -= min;
        }
        return min + context.entity().random().nextFloat() * range;
    }
}