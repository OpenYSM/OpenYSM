package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.AnimationState;
import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.slashblade.SlashBladeCompat;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapability;
import com.elfmcys.yesstevemodel.client.compat.gun.tacz.TacCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.world.entity.Entity;

import java.util.Objects;

public class TouhouMaidAnimationPredicate implements IAnimationPredicate<MaidCapability> {

    private static final ReferenceArrayList<AnimationState<EntityMaid, MaidCapability>>[] priorityHandlers = new ReferenceArrayList[5];

    static {
        for (int i = 0; i < priorityHandlers.length; i++) {
            priorityHandlers[i] = new ReferenceArrayList<>(6);
        }
    }

    public static void registerHandler(AnimationState<EntityMaid, MaidCapability> animationState) {
        priorityHandlers[animationState.getPriority()].add(animationState);
    }

    @Override
    public PlayState predicate(AnimationEvent<MaidCapability> event, ExpressionEvaluator<?> evaluator) {
        EntityMaid entity = event.getAnimatable().getEntity();
        if (entity == null || (event.getAnimatable() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (entity.renderState != MaidRenderState.ENTITY) {
            return PlayState.STOP;
        }
        Entity vehicle = entity.getVehicle();
        if (vehicle != null && vehicle.isAlive()) {
            return PlayState.STOP;
        }
        for (int i = 0; i <= 4; i++) {
            ObjectListIterator it = priorityHandlers[i].iterator();
            while (it.hasNext()) {
                AnimationState animationState = (AnimationState) it.next();
                if (animationState.getPredicate().test(entity, event)) {
                    String str = animationState.getAnimationName();
                    ILoopType loopType = animationState.getLoopType();
                    PlayState playState = SlashBladeCompat.handleSlashBladeAnim(entity, event, str, loopType);
                    if (playState != null) {
                        return playState;
                    }
                    return Objects.requireNonNullElseGet(TacCompat.handleTaczAnimState(entity, event, str, loopType), () -> {
                        return IAnimationPredicate.playAnimationWithLoop(event, str, loopType);
                    });
                }
            }
        }
        return PlayState.STOP;
    }
}