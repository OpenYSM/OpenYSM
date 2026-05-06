package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionUse;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.StringUtils;

public class InteractionHandAnimationPredicate implements IAnimationPredicate<LivingAnimatable<?>> {
    @Override
    public PlayState predicate(AnimationEvent<LivingAnimatable<?>> event, ExpressionEvaluator<?> evaluator) {
        LivingEntity livingEntity = (LivingEntity) ((LivingAnimatable) event.getAnimatable()).getEntity();
        if (livingEntity == null || (event.getAnimatable() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        int i = event.getAnimatable().getModelAssembly().getModelData().getFormatVersion();
        if (livingEntity.isUsingItem() && !livingEntity.isSleeping()) {
            if (livingEntity.getTicksUsingItem() == 1 && ((LivingAnimatable) event.getAnimatable()).getPositionTracker().markProcessed(2)) {
                event.getController().stopTransition();
            }
            ConditionManager conditionManager = event.getAnimatable().getModelConfig();
            if (livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                ConditionUse conditionUse = conditionManager.getUseMainhand();
                if (conditionUse != null) {
                    String str = conditionUse.doTest(livingEntity, InteractionHand.MAIN_HAND);
                    if (StringUtils.isNoneBlank(str)) {
                        return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.LOOP, i);
                    }
                }
                return IAnimationPredicate.playAnimationWithValid(event, "use_mainhand", ILoopType.EDefaultLoopTypes.LOOP, i);
            }
            ConditionUse conditionUse2 = conditionManager.getUseOffhand();
            if (conditionUse2 != null) {
                String str2 = conditionUse2.doTest(livingEntity, InteractionHand.OFF_HAND);
                if (StringUtils.isNoneBlank(str2)) {
                    return IAnimationPredicate.playAnimationWithValid(event, str2, ILoopType.EDefaultLoopTypes.LOOP, i);
                }
            }
            return IAnimationPredicate.playAnimationWithValid(event, "use_offhand", ILoopType.EDefaultLoopTypes.LOOP, i);
        }
        return PlayState.STOP;
    }
}
