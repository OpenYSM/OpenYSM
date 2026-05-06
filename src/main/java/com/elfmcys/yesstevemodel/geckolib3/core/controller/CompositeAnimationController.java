package com.elfmcys.yesstevemodel.geckolib3.core.controller;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;

import java.util.List;
import java.util.function.Consumer;

public class CompositeAnimationController<T extends AnimatableEntity<?>> implements IAnimationController<T> {

    private final String name;

    private final T animatable;

    private final PredicateBasedController<T> controller;

    private final AnimationControllerRuntime<T> animationRuntime;

    private boolean initialized;

    private IAnimationController<T> activeController;

    public CompositeAnimationController(T animatable, String name, float transitionLengthTicks, IAnimationPredicate<T> predicate) {
        this(animatable, name, transitionLengthTicks, predicate, false);
    }

    @Deprecated
    public CompositeAnimationController(T animatable, String name, float transitionLengthTicks, IAnimationPredicate<T> predicate, boolean deprecatedMode) {
        this.name = name;
        this.animatable = animatable;
        this.controller = new PredicateBasedController<>(animatable, name, transitionLengthTicks, predicate, deprecatedMode);
        this.animationRuntime = new AnimationControllerRuntime<>(animatable, name, transitionLengthTicks);
        this.activeController = this.controller;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getCurrentAnimation() {
        if (this.initialized) {
            return this.animationRuntime.isBuiltinAnimation() ? "[builtin] " + this.controller.getCurrentAnimation() : this.animationRuntime.getCurrentAnimation();
        }
        return this.controller.getCurrentAnimation();
    }

    @Override
    public void init(List<BoneTopLevelSnapshot> list, Object2ReferenceMap<String, List<IValue>> object2ReferenceMap) {
        AnimationController controller = this.animatable.getAnimationEntries(this.name);
        if (controller != null) {
            this.initialized = true;
            this.animationRuntime.initWithBones(list, controller);
            this.controller.init(list, object2ReferenceMap);
            this.activeController = this.animationRuntime;
            return;
        }
        this.initialized = false;
        this.controller.init(list, object2ReferenceMap);
        this.animationRuntime.reset();
        this.activeController = this.controller;
    }

    @Override
    public void process(AnimationEvent<T> event, ExpressionEvaluator<AnimationContext<?>> evaluator, boolean z) {
        if (this.initialized) {
            this.animationRuntime.process(event, evaluator, z);
            if (this.animationRuntime.isBuiltinAnimation()) {
                if (this.activeController != this.controller) {
                    this.controller.setInterpolator(this.animationRuntime.getCurrentEntry().getBlendTransition().asInterpolator());
                    this.activeController = this.controller;
                }
                this.controller.process(event, evaluator, z);
                return;
            }
            if (this.activeController != this.animationRuntime) {
                this.controller.evaluateExpressions(evaluator);
                this.controller.clearAnimation();
                this.activeController = this.animationRuntime;
                return;
            }
            return;
        }
        this.controller.process(event, evaluator, z);
    }

    @Override
    public void forEachTransform(Consumer<BoneTransformProvider> consumer) {
        this.activeController.forEachTransform(consumer);
    }

    @Override
    public boolean isDeprecatedMode() {
        return this.activeController.isDeprecatedMode();
    }

    @Override
    public void reset() {
        if (this.initialized) {
            this.animationRuntime.reset();
            this.controller.reset();
        } else {
            this.controller.reset();
        }
    }
}