package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiPredicate;

public class AnimationRegister {
    private static final float MIN_SPEED = 0.05f;

    public static void registerAnimationState() {
        register("death", ILoopType.EDefaultLoopTypes.PLAY_ONCE, Priority.HIGHEST, (player, event) -> player.isDeadOrDying());
        register("riptide", Priority.HIGHEST, (player, event) -> player.isAutoSpinAttack());
        register("sleep", Priority.HIGHEST, (player, event) -> player.getPose() == Pose.SLEEPING);
        register("swim", Priority.HIGHEST, (player, event) -> player.isSwimming());
        register("climb", Priority.HIGHEST, (player, event) -> player.getPose() == Pose.SWIMMING && Math.abs(event.getLimbSwingAmount()) > MIN_SPEED);
        register("climbing", Priority.HIGHEST, (player, event) -> player.getPose() == Pose.SWIMMING);
        register("ladder_up", Priority.HIGHEST, (player, event) -> player.onClimbable() && getVerticalSpeed(player) > 0.0f);
        register("ladder_stillness", Priority.HIGHEST, (player, event) -> player.onClimbable() && getVerticalSpeed(player) == 0.0f);
        register("ladder_down", Priority.HIGHEST, (player, event) -> player.onClimbable() && getVerticalSpeed(player) < 0.0f);
        register("fly", Priority.HIGH, (player, event) -> {
            AnimatableEntity<Player> animatable = event.getAnimatable();
            if (animatable instanceof PlayerCapability cap) {
                if (!cap.isLocalPlayerModel()) {
                    return cap.getPositionTracker().isFlying();
                }
            }
            return player.getAbilities().flying;
        });
        register("elytra_fly", Priority.HIGH, (player, event) -> player.getPose() == Pose.FALL_FLYING && player.isFallFlying());
        register("swim_stand", Priority.NORMAL, (player, event) -> player.isInWater() && !player.onGround());
        register("attacked", ILoopType.EDefaultLoopTypes.PLAY_ONCE, 2, (player, event) -> player.hurtTime > 0);
        register("jump", Priority.NORMAL, (player, event) -> !player.onGround() && !player.isInWater());
        register("sneak", Priority.NORMAL, (player, event) -> player.onGround() && player.getPose() == Pose.CROUCHING && Math.abs(event.getLimbSwingAmount()) > MIN_SPEED);
        register("sneaking", Priority.NORMAL, (player, event) -> player.onGround() && player.getPose() == Pose.CROUCHING);
        register("run", Priority.LOW, (player, event) -> player.onGround() && player.isSprinting());
        register("walk", Priority.LOW, (player, event) -> player.onGround() && event.getLimbSwingAmount() > MIN_SPEED);
        register("idle", Priority.LOWEST, (player, event) -> true);
    }

    private static void register(String animationName, ILoopType loopType, int priority, BiPredicate<Player, AnimationEvent<CustomPlayerEntity>> predicate) {
        AnimationManager.register(new AnimationState<>(animationName, loopType, priority, predicate));
    }

    private static void register(String animationName, int priority, BiPredicate<Player, AnimationEvent<CustomPlayerEntity>> predicate) {
        register(animationName, ILoopType.EDefaultLoopTypes.LOOP, priority, predicate);
    }

    private static float getVerticalSpeed(Player player) {
        return 20.0f * ((float) (player.position().y - player.yo));
    }
}