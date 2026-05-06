package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.item;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import net.minecraft.world.item.ItemStack;

public abstract class ItemStackFunction extends ContextFunction<ItemStack> {
    @Override
    public boolean validateContext(IContext<?> context) {
        return context.entity() instanceof ItemStack;
    }
}