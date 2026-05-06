package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.animation;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapability;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidGameRecordManager;

public class MaidGameStateAnimationPredicate implements IAnimationPredicate<MaidCapability> {

    public static final String[] GAME_STATE_ANIMATIONS = {"game_win", "game_lost", "beg"};

    @Override
    public PlayState predicate(AnimationEvent<MaidCapability> event, ExpressionEvaluator<?> evaluator) {
        EntityMaid maid = event.getAnimatable().getEntity();
        if (maid == null || (event.getAnimatable() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (maid.getVehicle() instanceof EntitySit) {
            MaidGameRecordManager gameRecordManager = maid.getGameRecordManager();
            if (gameRecordManager.isWin()) {
                return IAnimationPredicate.playLoopAnimation(event, "game_win");
            }
            if (gameRecordManager.isLost()) {
                return IAnimationPredicate.playLoopAnimation(event, "game_lost");
            }
        }
        if (maid.isBegging()) {
            return IAnimationPredicate.playLoopAnimation(event, "beg");
        }
        return PlayState.STOP;
    }
}