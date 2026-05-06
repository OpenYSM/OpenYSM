package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.compat.carryon.CarryOnCompat;
import com.elfmcys.yesstevemodel.client.compat.swem.SWEMCompat;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionChair;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionVehicle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LivingMovementAnimationPredicate implements IAnimationPredicate<LivingAnimatable<?>> {
    @Override
    public PlayState predicate(AnimationEvent<LivingAnimatable<?>> event, ExpressionEvaluator<?> evaluator) {
        return Objects.requireNonNullElse(renderRidingAnimation(event), PlayState.STOP);
    }

    @Nullable
    public PlayState renderRidingAnimation(AnimationEvent<LivingAnimatable<?>> event) {
        Entity vehicle;
        ConditionChair conditionChair;
        Player player = (Player) ((LivingAnimatable) event.getAnimatable()).getEntity();
        if (player == null || (event.getAnimatable() instanceof IPreviewAnimatable) || (vehicle = player.getVehicle()) == null || !vehicle.isAlive()) {
            return null;
        }
        String str = SWEMCompat.getHorseGaitName(player);
        if (StringUtils.isNoneBlank(str)) {
            return IAnimationPredicate.playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP);
        }
        ConditionManager conditionManager = event.getAnimatable().getModelConfig();
        if (TouhouLittleMaidCompat.isLoaded() && (conditionChair = conditionManager.getChair()) != null) {
            String str2 = conditionChair.doTest(player);
            if (StringUtils.isNoneBlank(str2)) {
                return IAnimationPredicate.playAnimationWithLoop(event, str2, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        ConditionVehicle conditionVehicle = conditionManager.getVehicle();
        if (conditionVehicle != null) {
            String str3 = conditionVehicle.doTest(player);
            if (StringUtils.isNoneBlank(str3)) {
                return IAnimationPredicate.playAnimationWithLoop(event, str3, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        if (vehicle instanceof Pig) {
            return IAnimationPredicate.playAnimationWithLoop(event, "ride_pig", ILoopType.EDefaultLoopTypes.LOOP);
        }
        if (vehicle instanceof Saddleable) {
            return IAnimationPredicate.playAnimationWithLoop(event, "ride", ILoopType.EDefaultLoopTypes.LOOP);
        }
        if (vehicle instanceof Boat) {
            return IAnimationPredicate.playAnimationWithLoop(event, "boat", ILoopType.EDefaultLoopTypes.LOOP);
        }
        boolean z = (player instanceof Player) && CarryOnCompat.isPlayerCarrying(player);
        boolean z2 = TouhouLittleMaidCompat.isMaidEntity(player) && (player.getVehicle() instanceof Player);
        if (z || z2) {
            return IAnimationPredicate.playAnimationWithLoop(event, "carryon:princess", ILoopType.EDefaultLoopTypes.LOOP);
        }
        PlayState playState = TouhouLittleMaidCompat.handleMaidInteraction(event, player, vehicle);
        if (playState != null) {
            return playState;
        }
        return IAnimationPredicate.playAnimationWithLoop(event, "sit", ILoopType.EDefaultLoopTypes.LOOP);
    }
}