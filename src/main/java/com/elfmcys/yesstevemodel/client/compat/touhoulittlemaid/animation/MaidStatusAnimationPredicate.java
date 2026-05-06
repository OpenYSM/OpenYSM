package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.animation;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapability;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public class MaidStatusAnimationPredicate implements IAnimationPredicate<MaidCapability> {

    public static final String[] RENDER_STATES = {"statue", "garage_kit"};

    @Override
    public PlayState predicate(AnimationEvent<MaidCapability> event, ExpressionEvaluator<?> evaluator) {
        EntityMaid entityMaid = event.getAnimatable().getEntity();
        if (entityMaid == null || (event.getAnimatable() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (entityMaid.renderState == MaidRenderState.STATUE) {
            return IAnimationPredicate.playLoopAnimation(event, "statue");
        }
        if (entityMaid.renderState == MaidRenderState.GARAGE_KIT) {
            return IAnimationPredicate.playLoopAnimation(event, "garage_kit");
        }
        return PlayState.STOP;
    }
}