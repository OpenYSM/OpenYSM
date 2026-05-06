package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiPredicate;

public class AnimationState<TE extends LivingEntity, AE extends AnimatableEntity<TE>> {

    private final String animationName;

    private final ILoopType loopType;

    private final int priority;

    private final BiPredicate<TE, AnimationEvent<AE>> predicate;

    public AnimationState(String animationName, ILoopType loopType, int priority, BiPredicate<TE, AnimationEvent<AE>> predicate) {
        this.animationName = animationName;
        this.loopType = loopType;
        this.priority = Mth.clamp(priority, Priority.HIGHEST, Priority.LOWEST);
        this.predicate = predicate;
    }

    public BiPredicate<TE, AnimationEvent<AE>> getPredicate() {
        return this.predicate;
    }

    public String getAnimationName() {
        return this.animationName;
    }

    public ILoopType getLoopType() {
        return this.loopType;
    }

    public int getPriority() {
        return this.priority;
    }
}