package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.item;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import net.minecraft.world.item.Item;

public abstract class ItemFunction extends ContextFunction<Item> {
    @Override
    public boolean validateContext(IContext<?> context) {
        return context.entity() instanceof Item;
    }
}