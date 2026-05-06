package com.elfmcys.yesstevemodel.client.compat.slashblade;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.util.ItemTagsConstants;
import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlashBladeStateHelper {
    public static boolean isSlashBlade(ItemStack itemStack) {
        return (itemStack.getItem() instanceof ItemSlashBlade) || itemStack.is(ItemTagsConstants.SLASHBLADE);
    }

    public static String getSlashBladeAnimation(AnimationEvent<? extends AnimatableEntity<? extends LivingEntity>> event) {
        LivingEntity livingEntity = event.getAnimatable().getEntity();
        return getComboState(livingEntity.getMainHandItem(), livingEntity);
    }

    public static String getSlashBladeAnimationFromContext(IContext<? extends LivingEntity> context) {
        LivingEntity livingEntity = context.entity();
        return getComboState(livingEntity.getMainHandItem(), livingEntity);
    }

    public static PlayState handleSlashBladeAnim(AnimationEvent<? extends AnimatableEntity<? extends LivingEntity>> event, String animation, ILoopType loopType) {
        String str2 = "slashblade:" + animation;
        if (event.getAnimatable().getAnimation(str2) != null) {
            return setAnimAndContinue(event, str2, loopType);
        }
        return setAnimAndContinue(event, animation, loopType);
    }

    @NotNull
    private static String getComboState(ItemStack itemStack, LivingEntity livingEntity) {
        if (!SlashBladeCompat.isSlashBladeItem(itemStack)) {
            return StringPool.EMPTY;
        }
        return itemStack.getCapability(CapabilitySlashBlade.BLADESTATE).map(iSlashBladeState -> {
            long gameTime = (livingEntity.level().getGameTime() - iSlashBladeState.getLastActionTime()) * 50;
            if (SlashBladeCompat.hasNewApi()) {
                return SlashBladeComboHelper.getComboState(iSlashBladeState, gameTime, livingEntity);
            }
            if (iSlashBladeState instanceof SlashBladeState) {
                return SlashBladeStateAccess.getComboState((SlashBladeState) iSlashBladeState, gameTime);
            }
            return StringPool.EMPTY;
        }).orElse(StringPool.EMPTY);
    }

    @NotNull
    private static PlayState setAnimAndContinue(AnimationEvent<?> event, String animation, ILoopType loopType) {
        event.getController().setAnimation(animation, loopType);
        return PlayState.CONTINUE;
    }
}