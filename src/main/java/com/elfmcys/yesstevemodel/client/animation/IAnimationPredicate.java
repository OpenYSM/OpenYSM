package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IAnimationPredicate<T extends AnimatableEntity<?>> {
    PlayState predicate(AnimationEvent<T> event, ExpressionEvaluator<?> evaluator);

    @NotNull
    static <T extends AnimatableEntity<?>> PlayState playAnimationWithLoop(AnimationEvent<T> event, String animationName, ILoopType loopType) {
        event.getController().setAnimation(animationName, loopType);
        return PlayState.CONTINUE;
    }

    @NotNull
    static <P extends AnimatableEntity<?>> PlayState predicate(AnimationEvent<P> event, String animationName) {
        event.getController().setAnimation(animationName);
        return PlayState.CONTINUE;
    }

    @NotNull
    static <P extends AnimatableEntity<?>> PlayState playAnimationWithValid(AnimationEvent<P> event, String animationName, ILoopType loopType, int version) {
        if (AnimationFormatValidator.validate(event, animationName, version)) {
            event.getController().setAnimation(animationName);
        } else {
            event.getController().setAnimation(animationName, loopType);
        }
        return PlayState.CONTINUE;
    }

    @NotNull
    static <T extends AnimatableEntity<?>> PlayState playLoopAnimation(AnimationEvent<T> event, String str) {
        return playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP);
    }
}