package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import java.util.Optional;

public class LadderFacing implements IValueEvaluator<Integer, IContext<LivingEntity>> {
    @Override
    public Integer eval(IContext<LivingEntity> ctx) {
        Optional<BlockPos> lastClimbablePos = ctx.entity().getLastClimbablePos();
        if (lastClimbablePos.isPresent()) {
            Optional<Direction> optionalValue = ctx.entity().level().getBlockState(lastClimbablePos.get()).getOptionalValue(HorizontalDirectionalBlock.FACING);
            if (optionalValue.isPresent()) {
                return optionalValue.get().get2DDataValue();
            }
        }
        return 0;
    }
}