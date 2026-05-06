package com.elfmcys.yesstevemodel.geckolib3.core.controller;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.*;
import com.elfmcys.yesstevemodel.geckolib3.core.event.SoundKeyFrameExecutor;
import com.elfmcys.yesstevemodel.geckolib3.core.event.InstructionKeyFrameExecutor;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.TransitionKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable;
import com.elfmcys.yesstevemodel.geckolib3.util.InterpolationLookup;
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class  AnimationControllerInstance {
    private final Int2ReferenceOpenHashMap<BoneAnimationQueue> boneAnimationQueues;

    private final ReferenceArrayList<BoneAnimationQueue> activeBoneAnimationQueues;

    private final AnimationControllerContext context;

    /**
     * 实体对象
     */
    private final AnimatableEntity<?> animatable;

    private boolean isScaleTransitionSpecial;

    private final float defaultTransitionTick = 3.0f;

    private AnimationState animationState;

    private float tickOffset;

    private IInterpolable transitionInterpolator;

    private float savedEndingTick;

    private Pair<ILoopType, String> lastRequestedAnimation;

    private Pair<ILoopType, Animation> pendingAnimation;

    private Animation currentAnimation;

    private ILoopType currentAnimationLoop;

    private InstructionKeyFrameExecutor instructionExecutor;

    private SoundKeyFrameExecutor soundExecutor;

    private boolean isAnimationFinished;

    public AnimationControllerInstance(AnimatableEntity<?> animatable, float transitionLengthTicks) {
        this(animatable, transitionLengthTicks, false);
    }

    public AnimationControllerInstance(AnimatableEntity<?> animatable, float transitionLengthTicks, boolean isScaleTransitionSpecial) {
        this.boneAnimationQueues = new Int2ReferenceOpenHashMap<>();
        this.activeBoneAnimationQueues = new ReferenceArrayList<>();
        this.context = new AnimationControllerContext();
        this.animationState = AnimationState.IDLE;
        this.lastRequestedAnimation = null;
        this.pendingAnimation = null;
        this.isAnimationFinished = true;
        this.animatable = animatable;
        this.transitionInterpolator = new TicksInterpolator(transitionLengthTicks);
        this.isScaleTransitionSpecial = isScaleTransitionSpecial;
        this.tickOffset = 0.0f;
    }

    public void initBoneQueues(List<BoneTopLevelSnapshot> list) {
        fullReset();
        for (BoneTopLevelSnapshot boneTopLevelSnapshot : list) {
            this.boneAnimationQueues.put(boneTopLevelSnapshot.boneId, new BoneAnimationQueue(boneTopLevelSnapshot));
        }
    }

    public void setAnimation(@Nullable String animationName) {
        setAnimation(animationName, null);
    }

    public void setAnimation(@Nullable String animationName, @Nullable ILoopType loopType) {
        if (animationName == null) {
            cancelAnimation();
            return;
        }
        if (this.lastRequestedAnimation != null && this.lastRequestedAnimation.getSecond().equals(animationName) && this.lastRequestedAnimation.getFirst() == loopType) {
            return;
        }
        clearAnimation();
        this.lastRequestedAnimation = new Pair<>(loopType, animationName);
        Animation animation = this.animatable.getAnimation(animationName);
        if (animation == null) {
            return;
        }
        this.pendingAnimation = new Pair<>(loopType != null ? loopType : animation.loop, animation);
    }

    public void process(float tick, ExpressionEvaluator<AnimationContext<?>> evaluator, boolean z) {
        evaluator.entity().setAnimationControllerContext(this.context);
        float adjustedTick = adjustTick(tick);
        if (this.animationState == AnimationState.ENDING_TRANSITION && adjustedTick >= defaultTransitionTick) {
            clearAnimation();
        }
        if (this.animationState == AnimationState.RUNNING && this.currentAnimationLoop == ILoopType.EDefaultLoopTypes.PLAY_ONCE && adjustedTick >= this.currentAnimation.animationLength) {
            executeRemainingEvents(evaluator, z);
            startEndingTransition(tick);
            this.context.executeRenderLayers(evaluator);
            adjustedTick = adjustTick(tick);
        }
        if (this.animationState == AnimationState.IDLE) {
            this.context.executeRenderLayers(evaluator);
            if (!applyPendingAnimation()) {
                return;
            }
            this.tickOffset = tick;
            adjustedTick = 0.0f;
            if (this.transitionInterpolator.getProgress() > 0.0f) {
                this.animationState = AnimationState.BEGINNING_TRANSITION;
            } else {
                this.animationState = AnimationState.RUNNING;
            }
        }
        resetAllQueues();
        if (this.animationState == AnimationState.BEGINNING_TRANSITION) {
            if (adjustedTick < this.transitionInterpolator.getProgress()) {
                this.context.setAnimTime(0.0f);
                processBeginningTransition(evaluator, adjustedTick);
                return;
            } else {
                adjustedTick -= this.transitionInterpolator.getProgress();
                this.tickOffset = tick - adjustedTick;
                this.animationState = AnimationState.RUNNING;
            }
        }
        if (this.animationState == AnimationState.RUNNING) {
            if (adjustedTick > this.currentAnimation.animationLength) {
                this.isAnimationFinished = true;
                if (this.currentAnimationLoop == ILoopType.EDefaultLoopTypes.LOOP) {
                    this.context.executeRenderLayers(evaluator);
                    if (this.currentAnimation.animationLength > 0.0f) {
                        adjustedTick %= this.currentAnimation.animationLength;
                    } else {
                        adjustedTick = 0.0f;
                    }
                    executeRemainingEvents(evaluator, z);
                    this.tickOffset = tick - adjustedTick;
                } else if (this.currentAnimationLoop == ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME) {
                    adjustedTick = this.currentAnimation.animationLength;
                }
            }
            this.context.setAnimTime(adjustedTick / 20.0f);
            executeTimelineEvents(evaluator, adjustedTick, z);
            processRunningAnimation(evaluator, adjustedTick);
            return;
        }
        if (this.animationState == AnimationState.ENDING_TRANSITION) {
            if (adjustedTick > defaultTransitionTick) {
                adjustedTick = defaultTransitionTick;
            }
            this.context.setAnimTime(this.savedEndingTick / 20.0f);
            processEndingTransition(evaluator, adjustedTick);
        }
    }

    public AnimationControllerContext getContext() {
        return this.context;
    }

    private void executeRemainingEvents(ExpressionEvaluator<AnimationContext<?>> animationTick, boolean z) {
        this.context.setAnimTime(this.currentAnimation.animationLength / 20.0f);
        if (this.instructionExecutor != null) {
            this.instructionExecutor.executeRemaining(animationTick, z);
            this.instructionExecutor.reset();
        }
        if (this.soundExecutor != null) {
            this.soundExecutor.reset();
        }
    }

    public void executeRenderLayers(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        evaluator.entity().setAnimationControllerContext(this.context);
        this.context.executeRenderLayers(evaluator);
        evaluator.entity().setAnimationControllerContext(null);
    }

    private void executeTimelineEvents(ExpressionEvaluator<AnimationContext<?>> evaluator, float currentTick, boolean isActive) {
        if (this.soundExecutor != null) {
            this.soundExecutor.playSound(this.animatable, currentTick, isActive);
        }
        if (this.instructionExecutor != null) {
            this.instructionExecutor.executeTo(evaluator, currentTick, isActive);
        }
    }

    private void startEndingTransition(float tick) {
        if (this.animationState == AnimationState.RUNNING || this.animationState == AnimationState.BEGINNING_TRANSITION) {
            float adjustedTick = adjustTick(tick);
            for (BoneAnimationQueue animationQueue : this.activeBoneAnimationQueues) {
                if (animationQueue.rotationQueue != null && animationQueue.rotationQueue.cachedValue != null) {
                    animationQueue.positionOutput = new Vector3f(animationQueue.rotationQueue.cachedValue);
                }
                if (animationQueue.positionQueue != null && animationQueue.positionQueue.cachedValue != null) {
                    animationQueue.rotationOutput = new Vector3f(animationQueue.positionQueue.cachedValue);
                }
                if (animationQueue.scaleQueue != null && animationQueue.scaleQueue.cachedValue != null) {
                    animationQueue.scaleOutput = new Vector3f(animationQueue.scaleQueue.cachedValue);
                }
            }
            this.tickOffset = tick;
            if (this.animationState == AnimationState.RUNNING) {
                if (adjustedTick > this.currentAnimation.animationLength) {
                    adjustedTick = this.currentAnimation.animationLength;
                }
                this.savedEndingTick = adjustedTick;
            } else {
                this.savedEndingTick = 0.0f;
            }
            this.isAnimationFinished = true;
            this.animationState = AnimationState.ENDING_TRANSITION;
        }
    }

    private void processBeginningTransition(ExpressionEvaluator<AnimationContext<?>> evaluator, float tick) {
        TransitionPoint transitionPoint;
        float blendWeight = this.currentAnimation.blendWeight != null ? this.currentAnimation.blendWeight.evalAsFloat(evaluator) : 1.0f;
        float lerpFactor = this.transitionInterpolator.interpolate(tick);
        for (BoneAnimationQueue boneAnimationQueue : this.activeBoneAnimationQueues) {
            boneAnimationQueue.setBlendWeight(blendWeight);
            BoneSnapshot boneSnapshot = boneAnimationQueue.snapshot();
            if (boneAnimationQueue.rotationTimeline != null) {
                boneAnimationQueue.rotationQueue = getTransitionPointAtTick(boneAnimationQueue.rotationTimeline, tick, lerpFactor, boneSnapshot.rotation);
            }
            if (boneAnimationQueue.positionTimeline != null) {
                boneAnimationQueue.positionQueue = getTransitionPointAtTick(boneAnimationQueue.positionTimeline, tick, lerpFactor, boneSnapshot.position);
            }
            if (boneAnimationQueue.scaleTimeline != null) {
                if (this.isScaleTransitionSpecial) {
                    transitionPoint = getTransitionPointAtTick(boneAnimationQueue.scaleTimeline, this.transitionInterpolator.getProgress(), 1.0f, boneSnapshot.scale);
                } else {
                    transitionPoint = getTransitionPointAtTick(boneAnimationQueue.scaleTimeline, tick, lerpFactor, boneSnapshot.scale);
                }
                boneAnimationQueue.scaleQueue = transitionPoint;
            }
        }
    }

    private void processRunningAnimation(ExpressionEvaluator<AnimationContext<?>> evaluator, float tick) {
        float blendWeight = this.currentAnimation.blendWeight != null ? this.currentAnimation.blendWeight.evalAsFloat(evaluator) : 1.0f;
        for (BoneAnimationQueue boneAnimationQueue : this.activeBoneAnimationQueues) {
            boneAnimationQueue.setBlendWeight(blendWeight);
            if (boneAnimationQueue.rotationTimeline != null) {
                boneAnimationQueue.rotationQueue = getKeyFramePointAtTick(boneAnimationQueue.rotationTimeline, tick);
            }
            if (boneAnimationQueue.positionTimeline != null) {
                boneAnimationQueue.positionQueue = getKeyFramePointAtTick(boneAnimationQueue.positionTimeline, tick);
            }
            if (boneAnimationQueue.scaleTimeline != null) {
                boneAnimationQueue.scaleQueue = getKeyFramePointAtTick(boneAnimationQueue.scaleTimeline, tick);
            }
        }
    }

    private void processEndingTransition(ExpressionEvaluator<AnimationContext<?>> evaluator, float f) {
        float fMo1906xaffeef43 = this.currentAnimation.blendWeight != null ? this.currentAnimation.blendWeight.evalAsFloat(evaluator) : 1.0f;
        for (BoneAnimationQueue boneAnimationQueue : this.activeBoneAnimationQueues) {
            boneAnimationQueue.setBlendWeight(fMo1906xaffeef43);
            if (boneAnimationQueue.positionOutput != null) {
                boneAnimationQueue.rotationQueue = getConstantPointAtTick(f, boneAnimationQueue.positionOutput, boneAnimationQueue.overrideMode);
            }
            if (boneAnimationQueue.rotationOutput != null) {
                boneAnimationQueue.positionQueue = getConstantPointAtTick(f, boneAnimationQueue.rotationOutput, boneAnimationQueue.overrideMode);
            }
            if (boneAnimationQueue.scaleOutput != null) {
                boneAnimationQueue.scaleQueue = getConstantPointAtTick(f, boneAnimationQueue.scaleOutput, boneAnimationQueue.overrideMode);
            }
        }
    }

    public void resetAllQueues() {
        if (this.animationState != AnimationState.IDLE) {
            for (BoneAnimationQueue activeBoneAnimationQueue : this.activeBoneAnimationQueues) {
                activeBoneAnimationQueue.resetQueues();
            }
        }
    }

    /**
     * 当前关键帧播放进度
     **/
    private KeyFramePoint getKeyFramePointAtTick(InterpolationLookup<BoneKeyFrame> frames, float tick) {
        BoneKeyFrame frame = frames.getAtTime(tick);
        return new KeyFramePoint(tick - frame.getStartTick(), frame, this.context);
    }

    /**
     * 返过渡进度
     **/
    private TransitionPoint getTransitionPointAtTick(InterpolationLookup<BoneKeyFrame> frames, float tick, float lerpFactor, Vector3f offsetPoint) {
        return new TransitionPoint(tick, lerpFactor, this.transitionInterpolator.getProgress(), offsetPoint, (TransitionKeyFrame) frames.getAtTime(0.0f), this.context);
    }

    /**
     * 静态动画点
     */
    private ConstantPoint getConstantPointAtTick(float tick, Vector3f offsetPoint, boolean isInstant) {
        return new ConstantPoint(tick, isInstant ? 0.0f : defaultTransitionTick, offsetPoint, this.context);
    }

    private boolean applyPendingAnimation() {
        Pair<ILoopType, Animation> pair = this.pendingAnimation;
        if (pair == null) {
            return false;
        }
        this.pendingAnimation = null;
        this.currentAnimation = pair.getSecond();
        this.currentAnimationLoop = pair.getFirst();
        this.isAnimationFinished = false;
        for (BoneAnimation animation : this.currentAnimation.boneAnimations) {
            BoneAnimationQueue queue = this.boneAnimationQueues.get(animation.boneId);
            if (queue != null) {
                queue.applyAnimation(animation, !animation.scaleKeyFrames.isEmpty());
                this.activeBoneAnimationQueues.add(queue);
            }
        }
        this.instructionExecutor = new InstructionKeyFrameExecutor(this.currentAnimation.customInstructionKeyframes);
        this.soundExecutor = new SoundKeyFrameExecutor(this.currentAnimation.soundKeyFrames, this.context.getAudioPlayerManager());
        return true;
    }

    private void clearAnimation() {
        if (this.animationState != AnimationState.IDLE) {
            this.animationState = AnimationState.IDLE;
            if (this.soundExecutor != null) {
                this.soundExecutor.reset();
            }
            this.soundExecutor = null;
            this.instructionExecutor = null;
            for (BoneAnimationQueue activeBoneAnimationQueue : this.activeBoneAnimationQueues) {
                activeBoneAnimationQueue.clear();
            }
            this.activeBoneAnimationQueues.clear();
            this.currentAnimation = null;
            this.isAnimationFinished = true;
        }
    }

    public Animation getCurrentAnimation() {
        return this.currentAnimation;
    }

    public AnimationState getAnimationState() {
        return this.animationState;
    }

    public ReferenceArrayList<BoneAnimationQueue> getActiveBoneAnimationQueues() {
        return this.activeBoneAnimationQueues;
    }

    public boolean isAnimationFinished() {
        return this.isAnimationFinished;
    }

    public void setTransitionInterpolator(IInterpolable interpolable) {
        this.transitionInterpolator = interpolable;
    }

    public float getInterpolated() {
        return this.transitionInterpolator.getProgress() * 20.0f;
    }

    public float adjustTick(float tick) {
        return Math.max(tick - this.tickOffset, 0.0f);
    }

    public void stopSound() {
        if (this.soundExecutor != null) {
            this.soundExecutor.stop();
        }
    }

    public void cancelAnimation() {
        this.lastRequestedAnimation = null;
        this.pendingAnimation = null;
        clearAnimation();
    }

    public void fullReset() {
        cancelAnimation();
        this.boneAnimationQueues.clear();
    }

    public void resetRequestedAnimation() {
        this.lastRequestedAnimation = null;
    }

    public void beginEndingTransition(float tick) {
        startEndingTransition(tick);
    }
}