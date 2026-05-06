package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionHold;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.entity.LivingEntityFrameState;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

public class OffHandHoldPredicate implements IAnimationPredicate<LivingAnimatable<?>> {
    @Override
    public PlayState predicate(AnimationEvent<LivingAnimatable<?>> event, ExpressionEvaluator<?> evaluator) {
        LivingEntity entity = event.getAnimatable().getEntity();
        if (entity == null || (event.getAnimatable() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (!checkSwingAndUse(entity, InteractionHand.OFF_HAND)) {
            return PlayState.PAUSE;
        }
        int i = event.getAnimatable().getModelAssembly().getModelData().getFormatVersion();
        ItemStack itemInHand = entity.getItemInHand(InteractionHand.OFF_HAND);
        if (itemInHand.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemInHand)) {
            return IAnimationPredicate.playAnimationWithValid(event, "hold_offhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP, i);
        }
        LivingEntityFrameState<?> c0675x43c72e02Mo1215x3cfc56ba = ((LivingAnimatable) event.getAnimatable()).getPositionTracker();
        if (!isSameItem(itemInHand, c0675x43c72e02Mo1215x3cfc56ba, InteractionHand.OFF_HAND)) {
            c0675x43c72e02Mo1215x3cfc56ba.setHandItemsForAnimation(itemInHand, InteractionHand.OFF_HAND);
            event.getController().stopTransition();
        }
        ConditionHold conditionHold = event.getAnimatable().getModelConfig().getHoldOffhand();
        if (conditionHold != null) {
            String str = conditionHold.doTest(entity, InteractionHand.OFF_HAND);
            if (StringUtils.isNoneBlank(str)) {
                return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.LOOP, i);
            }
        }
        return PlayState.STOP;
    }

    private boolean isSameItem(ItemStack stack, LivingEntityFrameState<?> frameState, InteractionHand hand) {
        ItemStack preItem = frameState.getHandItemsForAnimation(hand);
        if (preItem.isDamaged()) {
            return ItemStack.isSameItem(stack, preItem);
        }
        return ItemStack.matches(stack, preItem);
    }

    private boolean checkSwingAndUse(LivingEntity entity, InteractionHand hand) {
        if (entity.swinging && entity.swingingArm == hand) {
            return false;
        }
        return !entity.isUsingItem() || entity.getUsedItemHand() != hand;
    }
}