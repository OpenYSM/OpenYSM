package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;

public class IndicateReload extends ContextFunction<Object> {
    @Override
    public Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
        PredicateBasedController<?> animationController = context.entity().animationEvent().getController();
        if (animationController == null) {
            return null;
        }
        animationController.stopTransition();
        return null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 0;
    }
}