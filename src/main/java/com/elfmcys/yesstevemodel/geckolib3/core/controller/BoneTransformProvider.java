package com.elfmcys.yesstevemodel.geckolib3.core.controller;

import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.geckolib3.core.util.TransitionVector3f;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import java.util.Optional;

public interface BoneTransformProvider {
    BoneTopLevelSnapshot getBoneTarget();

    Optional<TransitionVector3f> getRotation(ExpressionEvaluator<AnimationContext<?>> evaluator);

    Optional<TransitionVector3f> getPosition(ExpressionEvaluator<AnimationContext<?>> evaluator);

    Optional<TransitionVector3f> getScale(ExpressionEvaluator<AnimationContext<?>> evaluator);
}