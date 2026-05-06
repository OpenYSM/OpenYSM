package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import net.minecraft.world.entity.TamableAnimal;

public abstract class TamableEntityFunction extends ContextFunction<TamableAnimal> {
    @Override
    public boolean validateContext(IContext<?> context) {
        return context.entity() instanceof TamableAnimal;
    }
}