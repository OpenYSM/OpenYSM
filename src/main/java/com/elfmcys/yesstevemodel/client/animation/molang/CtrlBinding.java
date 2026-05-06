package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.client.animation.Priority;
import com.elfmcys.yesstevemodel.client.compat.immersivemelodies.ImmersiveMelodiesCompat;
import com.elfmcys.yesstevemodel.client.compat.ironsspellbooks.SpellbooksCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController;
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl.*;
import com.elfmcys.yesstevemodel.client.compat.sbackpack.SBackpackCompat;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl.HandRenderFunction;
import com.elfmcys.yesstevemodel.client.compat.gun.tacz.TacCompat;
import com.elfmcys.yesstevemodel.client.compat.bettercombat.BetterCombatCompat;
import com.elfmcys.yesstevemodel.client.compat.carryon.CarryOnCompat;
import com.elfmcys.yesstevemodel.client.compat.parcool.ParcoolCompat;
import com.elfmcys.yesstevemodel.client.compat.slashblade.SlashBladeCompat;
import com.elfmcys.yesstevemodel.client.compat.swem.SWEMCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.EntityFrameStateTracker;
import com.elfmcys.yesstevemodel.util.data.LazySupplier;
import com.elfmcys.yesstevemodel.client.compat.create.CreateCompat;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public class CtrlBinding extends ContextBinding {

    public static final LazySupplier<CtrlBinding> INSTANCE = new LazySupplier<>(CtrlBinding::new);

    private static ReferenceArrayList<AnimationStatePredicate>[] data;

    private CtrlBinding() {
        registerLivingEntityState("death", Priority.HIGHEST, LivingEntity::isDeadOrDying);
        registerLivingEntityState("riptide", Priority.HIGHEST, LivingEntity::isAutoSpinAttack);
        registerLivingEntityState("sleep", Priority.HIGHEST, entity -> entity.getPose() == Pose.SLEEPING);
        registerLivingEntityState("swim", Priority.HIGHEST, Entity::isSwimming);
        registerLivingEntityState("climb", Priority.HIGHEST, entity -> entity.getPose() == Pose.SWIMMING && isWalking(entity));
        registerLivingEntityState("climbing", Priority.HIGHEST, entity -> entity.getPose() == Pose.SWIMMING);
        registerLivingEntityState("ladder_up", Priority.HIGHEST, entity -> entity.onClimbable() && getVerticalVelocity(entity) > 0.0f);
        registerLivingEntityState("ladder_stillness", Priority.HIGHEST, entity -> entity.onClimbable() && getVerticalVelocity(entity) == 0.0f);
        registerLivingEntityState("ladder_down", Priority.HIGHEST, entity -> entity.onClimbable() && getVerticalVelocity(entity) < 0.0f);
        registerState("fly", Priority.HIGH, CtrlBinding::isFlying);
        registerLivingEntityState("elytra_fly", Priority.HIGH, entity -> entity.getPose() == Pose.FALL_FLYING && entity.isFallFlying());
        registerLivingEntityState("swim_stand", Priority.NORMAL, entity -> entity.isInWater() && !entity.onGround());
        registerLivingEntityState("attacked", Priority.NORMAL, entity -> entity.hurtTime > 0);
        registerLivingEntityState("jump", Priority.NORMAL, entity -> !entity.onGround() && !entity.isInWater());
        registerLivingEntityState("sneak", Priority.NORMAL, entity -> entity.onGround() && entity.getPose() == Pose.CROUCHING && isWalking(entity));
        registerLivingEntityState("sneaking", Priority.NORMAL, entity -> entity.onGround() && entity.getPose() == Pose.CROUCHING);
        registerLivingEntityState("run", Priority.LOWEST, entity -> entity.onGround() && entity.isSprinting());
        registerLivingEntityState("walk", Priority.LOWEST, entity -> entity.onGround() && isWalking(entity));
        registerLivingEntityState("idle", Priority.LOWEST, entity -> true);

        var("playing_extra_animation", CtrlBinding::isPlayingExtraAnimation);
        function("hold", HandRenderFunction.createAlways());
        function("swing", HandRenderFunction.createWhenSwinging());
        function("use", HandRenderFunction.createWhenUsing());
        function("armor", Armor.create());
        function("ride", Ride.create());
        CarryOnCompat.registerBindings(this);
        TacCompat.registerControllerFunctions(this);
        SWEMCompat.registerControllerFunctions(this);
        ParcoolCompat.registerBindings(this);
        SlashBladeCompat.registerControllerFunctions(this);
        SBackpackCompat.registerControllerFunctions(this);
        CreateCompat.registerCreateFunctions(this);
        BetterCombatCompat.registerBindings(this);
        ImmersiveMelodiesCompat.registerBindings(this);
        SpellbooksCompat.registerBindings(this);
        constValue("state_continue", 2);
        constValue("state_stop", 3);
        constValue("state_pause", 4);
        constValue("state_bypass", 5);
        constValue("loop", 10);
        constValue("play_once", 11);
        constValue("hold_on_last_frame", 12);
        function("set_animation", new SetAnimation());
        function("set_beginning_transition_length", new SetTransitionSpeed());
        function("reset", new Reset());
        function("indicate_reload", new IndicateReload());
    }

    private static boolean isPlayingExtraAnimation(IContext<Object> context) {
        AnimatableEntity<?> animatableEntity = context.geoInstance();
        if (!(animatableEntity instanceof CustomPlayerEntity customPlayerEntity)) {
            return false;
        }
        return customPlayerEntity.isModelSwitching() && customPlayerEntity.getAnimationState(PlayerAnimationController.CAP_CONTROLLER_KEY) != AnimationState.IDLE;
    }

    @SuppressWarnings("unchecked")
    private void registerState(String name, int priority, Predicate<IContext<LivingEntity>> predicate) {
        if (data == null) {
            data = new ReferenceArrayList[Priority.LOWEST + 1];
            for (int i = 0; i < data.length; i++) {
                data[i] = new ReferenceArrayList<>(6);
            }
        }
        data[priority].add(new AnimationStatePredicate(name, priority, predicate));
        livingEntityVar(name, ctx -> evaluateState(name, ctx));
    }

    private void registerLivingEntityState(String name, int priority, EntityCondition predicate) {
        registerState(name, priority, predicate);
    }

    private static boolean evaluateState(String name, IContext<LivingEntity> context) {
        LivingEntity livingEntity = context.entity();
        EntityFrameStateTracker<?> positionTracker = context.geoInstance().getPositionTracker();
        if (positionTracker.getCachedModelId() != null) {
            return name.equals(positionTracker.getCachedModelId());
        }
        if (context.geoInstance() instanceof IPreviewAnimatable) {
            positionTracker.setCachedModelId(StringPool.EMPTY);
            return false;
        }
        if ((livingEntity instanceof Player) && ParcoolCompat.isPlayerParcooling((Player) livingEntity)) {
            positionTracker.setCachedModelId(StringPool.EMPTY);
            return false;
        }
        Entity vehicle = livingEntity.getVehicle();
        if (vehicle != null && vehicle.isAlive()) {
            positionTracker.setCachedModelId(StringPool.EMPTY);
            return false;
        }
        for (int i = 0; i <= 4; i++) {
            for (AnimationStatePredicate animationStatePredicate : data[i]) {
                if (animationStatePredicate.predicate().test(context)) {
                    positionTracker.setCachedModelId(animationStatePredicate.name());
                    return animationStatePredicate.name().equals(name);
                }
            }
        }
        positionTracker.setCachedModelId(StringPool.EMPTY);
        return false;
    }

    private static boolean isWalking(LivingEntity livingEntity) {
        return Math.abs(livingEntity.walkAnimation.speed(Minecraft.getInstance().getPartialTick())) > 0.05f;
    }

    private static float getVerticalVelocity(LivingEntity livingEntity) {
        return 20.0f * ((float) (livingEntity.position().y - livingEntity.yo));
    }

    private static boolean isFlying(IContext<LivingEntity> context) {
        AnimatableEntity<?> animatableEntity = context.geoInstance();
        if (animatableEntity instanceof PlayerCapability cap) {
            if (!cap.isLocalPlayerModel()) {
                return cap.getPositionTracker().isFlying();
            }
        }
        Entity entity = context.entity();
        if (entity instanceof Player) {
            return ((Player) entity).getAbilities().flying;
        }
        return false;
    }

    private record AnimationStatePredicate(String name, int priority, Predicate<IContext<LivingEntity>> predicate) {
    }

    private interface EntityCondition extends Predicate<IContext<LivingEntity>> {
        boolean check(LivingEntity entity);

        @Override
        default boolean test(IContext<LivingEntity> context) {
            return check(context.entity());
        }
    }
}