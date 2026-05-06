package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator;
import net.minecraft.world.entity.player.Player;

public class TextureName implements IValueEvaluator<String, IContext<Player>> {
    @Override
    public String eval(IContext<Player> context) {
        AnimatableEntity<?> animatableEntity = context.geoInstance();
        if (animatableEntity instanceof CustomPlayerEntity) {
            return ((CustomPlayerEntity) animatableEntity).getCurrentTextureName();
        }
        return null;
    }
}