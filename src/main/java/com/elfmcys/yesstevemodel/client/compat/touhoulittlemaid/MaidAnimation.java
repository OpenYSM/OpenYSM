package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.client.animation.AnimationState;
import com.elfmcys.yesstevemodel.client.animation.Priority;
import com.elfmcys.yesstevemodel.client.animation.predicate.TouhouMaidAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapability;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.function.BiPredicate;

public class MaidAnimation {

    private static final float MOVEMENT_THRESHOLD = 0.05f;

    public static void registerAnimationStates() {
        registerState("death", ILoopType.EDefaultLoopTypes.PLAY_ONCE, Priority.HIGHEST, (entityMaid, event) -> entityMaid.isDeadOrDying());
        registerLoopState("sleep", Priority.HIGHEST, (maid, event) -> maid.getPose() == Pose.SLEEPING);
        registerLoopState("swim", Priority.HIGHEST, (maid, event) -> maid.isSwimming());
        registerLoopState("ladder_up", Priority.HIGHEST, (maid, event) -> maid.onClimbable() && getVerticalSpeed(maid) > 0.0f);
        registerLoopState("ladder_stillness", Priority.HIGHEST, (maid, event) -> maid.onClimbable() && getVerticalSpeed(maid) == 0.0f);
        registerLoopState("ladder_down", Priority.HIGHEST, (maid, event) -> maid.onClimbable() && getVerticalSpeed(maid) < 0.0f);
        registerLoopState("sit", Priority.HIGH, (maid, event) -> maid.isMaidInSittingPose());
        registerLoopState("swim_stand", Priority.NORMAL, (maid, event) -> maid.isInWater() && !maid.onGround());
        registerState("attacked", ILoopType.EDefaultLoopTypes.PLAY_ONCE, Priority.NORMAL, (maid, event) -> maid.hurtTime > 0);
        registerLoopState("jump", Priority.NORMAL, (maid, event) -> !maid.onGround() && !maid.isInWater());
        registerLoopState("run", Priority.LOW, (maid, event) -> maid.onGround() && maid.isSprinting());
        registerLoopState("walk", Priority.LOW, (maid, event) -> maid.onGround() && event.getLimbSwingAmount() > MOVEMENT_THRESHOLD);
        registerLoopState("idle", Priority.LOWEST, (maid, event) -> true);
    }

    private static void registerState(String name, ILoopType loopType, int priority, BiPredicate<EntityMaid, AnimationEvent<MaidCapability>> predicate) {
        TouhouMaidAnimationPredicate.registerHandler(new AnimationState(name, loopType, priority, predicate));
    }

    private static void registerLoopState(String name, int priority, BiPredicate<EntityMaid, AnimationEvent<MaidCapability>> predicate) {
        registerState(name, ILoopType.EDefaultLoopTypes.LOOP, priority, predicate);
    }

    private static float getVerticalSpeed(LivingEntity livingEntity) {
        return 20.0f * ((float) (livingEntity.position().y - livingEntity.yo));
    }
}