package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.compat.ironsspellbooks.SpellbooksCompat;
import com.elfmcys.yesstevemodel.client.compat.slashblade.SlashBladeCompat;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionSwing;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.StringUtils;

public class ItemHoldAnimationPredicate implements IAnimationPredicate<LivingAnimatable<?>> {
    @Override
    public PlayState predicate(AnimationEvent<LivingAnimatable<?>> event, ExpressionEvaluator<?> evaluator) {
        LivingEntity livingEntity = event.getAnimatable().getEntity();
        if (livingEntity == null || (event.getAnimatable() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        PlayState playState = SpellbooksCompat.resolvePlayState(event, livingEntity);
        if (playState != null) {
            return playState;
        }
        int i = event.getAnimatable().getModelAssembly().getModelData().getFormatVersion();
        if (!livingEntity.isSleeping() && SlashBladeCompat.isSlashBladeItem(livingEntity.getItemInHand(InteractionHand.MAIN_HAND))) {
            if (event.getController().isPlaying()) {
                event.getController().stopTransition();
            }
            String str = SlashBladeCompat.getComboAnimName(event);
            if (StringUtils.isNoneBlank(str)) {
                if (event.getAnimatable().getAnimation(str) != null) {
                    return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.PLAY_ONCE, i);
                }
                return PlayState.CONTINUE;
            }
        }
        if (livingEntity.swinging && !livingEntity.isSleeping()) {
            if (livingEntity.swingTime == 0 && ((LivingAnimatable) event.getAnimatable()).getPositionTracker().markProcessed(1)) {
                event.getController().stopTransition();
            }
            ConditionManager conditionManager = event.getAnimatable().getModelConfig();
            ConditionSwing conditionSwing = livingEntity.swingingArm == InteractionHand.MAIN_HAND ? conditionManager.getSwingMainhand() : conditionManager.getSwingOffhand();
            if (conditionSwing != null) {
                String str2 = conditionSwing.doTest(livingEntity, livingEntity.swingingArm);
                if (StringUtils.isNoneBlank(str2)) {
                    return IAnimationPredicate.playAnimationWithValid(event, str2, ILoopType.EDefaultLoopTypes.PLAY_ONCE, i);
                }
            }
            return IAnimationPredicate.playAnimationWithValid(event, livingEntity.swingingArm == InteractionHand.MAIN_HAND ? "swing_hand" : "swing_offhand", ILoopType.EDefaultLoopTypes.PLAY_ONCE, i);
        }
        return PlayState.CONTINUE;
    }
}
