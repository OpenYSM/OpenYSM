package com.elfmcys.yesstevemodel.client.compat.carryon;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

public class CarryOnBinding {
    public static void registerBindings(CtrlBinding binding) {
        binding.livingEntityVar("carryon_type", CarryOnBinding::getCarryOnType);
        binding.livingEntityVar("carryon_is_princess", ctx -> CarryOnDataHelper.isPlayerCarrying(ctx.entity()));
    }

    private static String getCarryOnType(IContext<LivingEntity> context) {
        CarryOnDataHelper.CarryType type;
        Entity entity = context.entity();
        if (!(entity instanceof Player) || (type = CarryOnDataHelper.getCarryType((Player) entity)) == CarryOnDataHelper.CarryType.NONE) {
            return StringPool.EMPTY;
        }
        return type.name().toLowerCase(Locale.ENGLISH);
    }
}