package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;

public class SetTransitionSpeed extends ContextFunction<Object> {
    @Override
    public Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
        PredicateBasedController<?> animationController = context.entity().animationEvent().getController();
        if (animationController == null) {
            return null;
        }
        float second = arguments.getAsFloat(context, 0);
        if (second < 0.0f) {
            return null;
        }
        animationController.setTransitionLengthTicks(second);
        return null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}