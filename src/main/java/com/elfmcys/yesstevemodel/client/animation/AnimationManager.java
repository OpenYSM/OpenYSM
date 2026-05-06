package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.compat.parcool.ParcoolCompat;
import com.elfmcys.yesstevemodel.client.compat.slashblade.SlashBladeCompat;
import com.elfmcys.yesstevemodel.client.compat.gun.swarfare.SWarfareCompat;
import com.elfmcys.yesstevemodel.client.compat.gun.tacz.TacCompat;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.client.compat.create.CreateCompat;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public class AnimationManager implements IAnimationPredicate<CustomPlayerEntity> {

    private static final ReferenceArrayList<AnimationState<Player, CustomPlayerEntity>>[] data = new ReferenceArrayList[Priority.LOWEST + 1];

    static {
        for (int i = 0; i < data.length; i++) {
            data[i] = new ReferenceArrayList<>(6);
        }
    }

    public static void register(AnimationState<Player, CustomPlayerEntity> state) {
        data[state.getPriority()].add(state);
    }

    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        Player player = event.getAnimatable().getEntity();
        if (player == null) {
            return PlayState.STOP;
        }
        if (event.getAnimatable() instanceof IPreviewAnimatable) {
            return PlayState.STOP;
        }
        if (ParcoolCompat.isPlayerParcooling(player)) {
            return PlayState.STOP;
        }
        Entity vehicle = player.getVehicle();
        if (vehicle != null && vehicle.isAlive()) {
            return PlayState.STOP;
        }
        if (CreateCompat.isPlayerOnCreateContraption(player)) {
            return IAnimationPredicate.predicate(event, "parcool:ride_zipline");
        }
        for (int i = Priority.HIGHEST; i <= Priority.LOWEST; i++) {
            for (AnimationState<Player, CustomPlayerEntity> animationState : data[i]) {
                if (animationState.getPredicate().test(player, event)) {
                    String name = animationState.getAnimationName();
                    ILoopType loopType = animationState.getLoopType();
                    PlayState slashBladePlayState = SlashBladeCompat.handleSlashBladeAnim(player, event, name, loopType);
                    if (slashBladePlayState != null) {
                        return slashBladePlayState;
                    }
                    PlayState taczPlayState = TacCompat.handleTaczAnimState(player, event, name, loopType);
                    if (taczPlayState == null) {
                        taczPlayState = SWarfareCompat.handleTaczAnim(player, event, name, loopType);
                    }
                    return Objects.requireNonNullElseGet(taczPlayState, () -> IAnimationPredicate.playAnimationWithLoop(event, name, loopType));
                }
            }
        }
        return PlayState.STOP;
    }
}