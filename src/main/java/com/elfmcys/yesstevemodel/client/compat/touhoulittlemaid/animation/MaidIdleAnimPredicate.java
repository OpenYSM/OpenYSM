package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.animation;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapability;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

public class MaidIdleAnimPredicate implements IAnimationPredicate<MaidCapability> {
    @Override
    public PlayState predicate(AnimationEvent<MaidCapability> event, ExpressionEvaluator<?> evaluator) {
        MaidCapability capability = event.getAnimatable();
        if (capability instanceof IPreviewAnimatable previewAnimatable) {
            if (previewAnimatable.getAnimationStateMachine().hasAnimation()) {
                return IAnimationPredicate.playLoopAnimation(event, previewAnimatable.getAnimationStateMachine().getCurrentAnimation());
            }
            return PlayState.STOP;
        }
        if (capability.isModelAvailable()) {
            if (capability.hasModel()) {
                capability.refreshModel();
                event.getController().stopTransition();
            }
            return IAnimationPredicate.predicate(event, capability.getModelTextureId());
        }
        return PlayState.STOP;
    }
}