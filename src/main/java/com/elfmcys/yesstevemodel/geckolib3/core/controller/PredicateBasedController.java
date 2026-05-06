package com.elfmcys.yesstevemodel.geckolib3.core.controller;

import com.elfmcys.yesstevemodel.audio.PlaybackFlags;
import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.ConstantPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.TransitionPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.AnimationPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimationQueue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable;
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.geckolib3.core.util.TransitionVector3f;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PredicateBasedController<T extends AnimatableEntity<?>> implements IAnimationController<T> {

    private final String name;

    private final IAnimationPredicate<T> predicate;

    private final AnimationControllerInstance transitionInterpolator;

    private final boolean deprecatedMode;

    private final PlaybackFlags playbackFlags;

    private final float transitionLengthTicks;

    @Nullable
    private IValue soundIValue;

    private boolean needsReset;

    public PredicateBasedController(T animatable, String name, float transitionLengthTicks, IAnimationPredicate<T> predicate) {
        this(animatable, name, transitionLengthTicks, predicate, false);
    }

    @Deprecated
    public PredicateBasedController(T animatable, String name, float transitionLengthTicks, IAnimationPredicate<T> predicate, boolean deprecatedMode) {
        this.name = name;
        this.predicate = predicate;
        this.transitionInterpolator = new AnimationControllerInstance(animatable, transitionLengthTicks, true);
        this.transitionLengthTicks = transitionLengthTicks;
        this.deprecatedMode = deprecatedMode;
        this.playbackFlags = new PlaybackFlags(false);
    }

    @Override
    public void process(AnimationEvent<T> event, ExpressionEvaluator<AnimationContext<?>> evaluator, boolean isMoving) {
        event.setController(this);
        PlayState playState = handleSoundExpression(evaluator);
        if (playState == null) {
            playState = this.predicate.predicate(event, evaluator);
        }
        event.setController(null);
        if (playState == PlayState.CONTINUE) {
            this.transitionInterpolator.process(event.currentTick, evaluator, isMoving);
            this.needsReset = false;
        } else if (playState == PlayState.STOP) {
            AnimationState state = this.transitionInterpolator.getAnimationState();
            if (state == AnimationState.BEGINNING_TRANSITION || state == AnimationState.RUNNING) {
                this.transitionInterpolator.beginEndingTransition(event.currentTick);
                this.transitionInterpolator.resetRequestedAnimation();
            }
            if (state == AnimationState.ENDING_TRANSITION) {
                this.transitionInterpolator.process(event.currentTick, evaluator, isMoving);
            }
            this.needsReset = false;
        } else if (playState == PlayState.PAUSE) {
            this.transitionInterpolator.process(event.currentTick, evaluator, false);
            this.transitionInterpolator.resetAllQueues();
            this.needsReset = true;
        }
        event.getAnimatable().setAnimationState(this.name, this.transitionInterpolator.getAnimationState());
    }

    @Nullable
    private PlayState handleSoundExpression(ExpressionEvaluator<AnimationContext<?>> expressionEvaluator) {
        if (this.soundIValue == null) {
            return null;
        }
        this.playbackFlags.setStopped(this.transitionInterpolator.isAnimationFinished());
        this.playbackFlags.setPaused(this.transitionInterpolator.isAnimationFinished());
        expressionEvaluator.entity().setPlaybackFlags(this.playbackFlags);
        expressionEvaluator.entity().setAnimationControllerContext(this.transitionInterpolator.getContext());
        expressionEvaluator.entity().setIsClientSide(true);
        int state = this.soundIValue.evalAsInt(expressionEvaluator);
        expressionEvaluator.entity().setIsClientSide(false);
        expressionEvaluator.entity().setAnimationControllerContext(null);
        expressionEvaluator.entity().setPlaybackFlags(null);
        switch (state) {
            case 2:
                return PlayState.CONTINUE;
            case 3:
                return PlayState.STOP;
            case 4:
                return PlayState.PAUSE;
            default:
                return null;
        }
    }

    @Override
    public void init(List<BoneTopLevelSnapshot> list, Object2ReferenceMap<String, List<IValue>> object2ReferenceMap) {
        this.transitionInterpolator.initBoneQueues(list);
        this.transitionInterpolator.setTransitionInterpolator(new TicksInterpolator(this.transitionLengthTicks));
        this.soundIValue = null;
        List<IValue> list2 = object2ReferenceMap.get(this.name.replace(".", "_ctrl_"));
        if (list2 != null && !list2.isEmpty()) {
            this.soundIValue = list2.get(0);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getCurrentAnimation() {
        if (this.transitionInterpolator.getAnimationState() == AnimationState.IDLE) {
            return "Coded";
        }
        return "Coded -> " + this.transitionInterpolator.getCurrentAnimation().animationName;
    }

    public void setAnimation(String animationName) {
        this.transitionInterpolator.setAnimation(animationName, null);
    }

    public void setAnimation(String animation, @Nullable ILoopType loopType) {
        this.transitionInterpolator.setAnimation(animation, loopType);
    }

    public void setTransitionLengthTicks(float ticks) {
        if (this.transitionInterpolator.getInterpolated() != ticks) {
            this.transitionInterpolator.setTransitionInterpolator(new TicksInterpolator(ticks));
        }
    }

    public void setInterpolator(IInterpolable interpolator) {
        this.transitionInterpolator.setTransitionInterpolator(interpolator);
    }

    public void evaluateExpressions(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        this.transitionInterpolator.executeRenderLayers(evaluator);
    }

    @Override
    public void forEachTransform(Consumer<BoneTransformProvider> consumer) {
        if (!this.needsReset) {
            for (BoneAnimationQueue boneAnimationQueue : this.transitionInterpolator.getActiveBoneAnimationQueues()) {
                consumer.accept(new TransformProviderRecord(boneAnimationQueue));
            }
        }
    }

    public void clearAnimation() {
        this.transitionInterpolator.cancelAnimation();
    }

    @Override
    public void reset() {
        this.transitionInterpolator.fullReset();
        this.soundIValue = null;
    }

    public void stopTransition() {
        this.transitionInterpolator.resetRequestedAnimation();
    }

    public void beginEndTransition(float currentTick) {
        this.transitionInterpolator.beginEndingTransition(currentTick);
    }

    public boolean isPlaying() {
        return this.transitionInterpolator.isAnimationFinished();
    }

    public void markDirty() {
        this.transitionInterpolator.stopSound();
    }

    @Override
    @Deprecated
    public boolean isDeprecatedMode() {
        return this.deprecatedMode && this.transitionInterpolator.getAnimationState() == AnimationState.RUNNING;
    }

    private record TransformProviderRecord(BoneAnimationQueue data) implements BoneTransformProvider {
        @Override
        public BoneTopLevelSnapshot getBoneTarget() {
            return this.data.topLevelSnapshot;
        }

        @Override
        public Optional<TransitionVector3f> getRotation(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            TransitionVector3f result;
            AnimationPoint point = this.data.rotationQueue;
            if (point == null) {
                return Optional.empty();
            }
            if (point instanceof ConstantPoint) {
                result = new TransitionVector3f(point.getLerpPoint(evaluator));
                result.setPercentCompleted(point.getPercentCompleted());
                float blendWeight = this.data.getBlendWeight();
                if (blendWeight != 1.0f) {
                    result.mul(blendWeight);
                }
            } else if (point instanceof TransitionPoint transition) {
                Vector3f vector3fMul = transition.evaluateRaw(evaluator).mul(this.data.getBlendWeight());
                MathUtil.nlerpEulerAngles(transition.getLerpFactor(), transition.getOffsetPoint(), vector3fMul, this.data.topLevelSnapshot.bone.getInitialRotation(), vector3fMul);
                result = new TransitionVector3f(vector3fMul);
                result.setPercentCompleted(0.0f);
            } else {
                result = new TransitionVector3f(point.getLerpPoint(evaluator));
                float blendWeight2 = this.data.getBlendWeight();
                if (blendWeight2 != 1.0f) {
                    result.mul(blendWeight2);
                }
                result.setPercentCompleted(0.0f);
            }
            return Optional.of(result);
        }

        @Override
        public Optional<TransitionVector3f> getPosition(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            AnimationPoint point = this.data.positionQueue;
            if (point == null) {
                return Optional.empty();
            }
            TransitionVector3f result = new TransitionVector3f(point.getLerpPoint(evaluator));
            float blendWeight = this.data.getBlendWeight();
            if (point instanceof ConstantPoint) {
                result.setPercentCompleted(point.getPercentCompleted());
            } else {
                if (point instanceof TransitionPoint) {
                    blendWeight = MathUtil.lerpValues(((TransitionPoint) point).getLerpFactor(), 1.0f, blendWeight);
                }
                result.setPercentCompleted(0.0f);
            }
            if (blendWeight != 1.0f) {
                result.mul(blendWeight);
            }
            return Optional.of(result);
        }

        @Override
        public Optional<TransitionVector3f> getScale(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            AnimationPoint point = this.data.scaleQueue;
            if (point == null) {
                return Optional.empty();
            }
            TransitionVector3f result = new TransitionVector3f(point.getLerpPoint(evaluator));
            float blendWeight = this.data.getBlendWeight();
            if (point instanceof ConstantPoint) {
                result.setPercentCompleted(point.getPercentCompleted());
            } else {
                if (point instanceof TransitionPoint) {
                    blendWeight = MathUtil.lerpValues(((TransitionPoint) point).getLerpFactor(), 1.0f, blendWeight);
                }
                result.setPercentCompleted(0.0f);
            }
            if (blendWeight != 1.0f) {
                MathUtil.lerpAnglesInPlace(result, blendWeight, result);
            }
            return Optional.of(result);
        }
    }
}