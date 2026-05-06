package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class RelativeBlockName extends EntityFunction {
    @Override
    public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        ResourceLocation key;
        BlockState blockState = MolangUtils.getRelativeBlockState(context, arguments);
        if (blockState == null || (key = ForgeRegistries.BLOCKS.getKey(blockState.getBlock())) == null) {
            return null;
        }
        return key.toString();
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 3;
    }
}