package com.elfmcys.yesstevemodel.geckolib3.core.builder;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation;
import com.elfmcys.yesstevemodel.geckolib3.core.event.ParticleEventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Animation {

    public final String animationName;

    public final float animationLength;

    public final ILoopType loop;

    // TODO https://github.com/bernie-g/geckolib/blob/4d62f40ab2d33db3c9d15d6867608ebc74242413/common/src/main/java/com/geckolib/loading/definition/animation/ActorAnimation.java#L54
    // 没调用
    @Nullable
    public final IValue unKnowData1;

    @Nullable
    public final IValue unKnowData2;

    @Nullable
    public final IValue blendWeight;

    @Nullable
    public final Boolean override;

    public final List<BoneAnimation> boneAnimations;

    public final List<EventKeyFrame<String>> soundKeyFrames;

    public final List<ParticleEventKeyFrame> particleKeyFrames;

    public final List<EventKeyFrame<IValue[]>> customInstructionKeyframes;

    public boolean isFromPrimaryAssembly = false;

    public Animation(String animationName, double animationLength, ILoopType loop,
                     @Nullable IValue unKnowData1, @Nullable IValue unKnowData2,
                     @Nullable IValue blendWeight, @Nullable Boolean overridePrevAnim,
                     BoneAnimation[] boneAnimations,
                     EventKeyFrame<String>[] soundKeyFrames,
                     ParticleEventKeyFrame[] particleKeyFrames,
                     EventKeyFrame<IValue[]>[] customInstructionKeyframes) {
        this.animationName = animationName;
        this.animationLength = (float) animationLength;
        this.loop = loop;
        this.unKnowData1 = unKnowData1;
        this.unKnowData2 = unKnowData2;
        this.blendWeight = blendWeight;
        this.override = overridePrevAnim;
        this.boneAnimations = ReferenceArrayList.wrap(boneAnimations);
        this.soundKeyFrames = ReferenceArrayList.wrap(soundKeyFrames);
        this.particleKeyFrames = ReferenceArrayList.wrap(particleKeyFrames);
        this.customInstructionKeyframes = ReferenceArrayList.wrap(customInstructionKeyframes);
    }

    public boolean isEmpty() {
        return this.boneAnimations.isEmpty() && this.soundKeyFrames.isEmpty() && this.particleKeyFrames.isEmpty() && this.customInstructionKeyframes.isEmpty();
    }
}