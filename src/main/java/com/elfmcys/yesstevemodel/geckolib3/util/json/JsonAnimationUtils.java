package com.elfmcys.yesstevemodel.geckolib3.util.json;

import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrameProcessor;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.RawBoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.event.ParticleEventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.util.AnimationUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.server.ChainedJsonException;

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JsonAnimationUtils {
    public static Set<Map.Entry<String, JsonElement>> getAnimations(JsonObject json) {
        if (json.has("animations")) {
            return json.getAsJsonObject("animations").entrySet();
        }
        return ImmutableSet.of();
    }

    public static List<Map.Entry<String, JsonElement>> getBones(JsonObject json) {
        JsonObject bones = json.getAsJsonObject("bones");
        return bones == null ? List.of() : new ArrayList<>(bones.entrySet());
    }

    public static List<Map.Entry<String, JsonElement>> getSoundEffects(JsonObject json) {
        JsonObject bones = json.getAsJsonObject("sound_effects");
        return bones == null ? List.of() : new ArrayList<>(bones.entrySet());
    }

    public static List<Map.Entry<String, JsonElement>> getCustomInstructionKeyFrames(JsonObject json) {
        JsonObject customInstructions = json.getAsJsonObject("timeline");
        return customInstructions == null ? List.of() : new ArrayList<>(customInstructions.entrySet());
    }

    private static JsonElement getObjectByKey(Set<Map.Entry<String, JsonElement>> json, String key)
            throws ChainedJsonException {
        for (Map.Entry<String, JsonElement> entry : json) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        throw new ChainedJsonException("Could not find key: " + key);
    }

    public static Map.Entry<String, JsonElement> getAnimation(JsonObject animationFile, String animationName)
            throws ChainedJsonException {
        return new AbstractMap.SimpleEntry(animationName, getObjectByKey(getAnimations(animationFile), animationName));
    }

    public static Animation deserializeJsonToAnimation(Map.Entry<String, JsonElement> element, MolangParser parser, boolean mergeMultilineExpr)
            throws ClassCastException, IllegalStateException {
        JsonObject animationJsonObject = element.getValue().getAsJsonObject();

        var animationName = element.getKey();
        JsonElement animationLength = animationJsonObject.get("animation_length");
        var animationLengthTicks = animationLength == null ? -1 : AnimationUtils.convertSecondsToTicks(animationLength.getAsFloat());

        var loop = ILoopType.fromJson(animationJsonObject.get("loop"));

        IValue blendWeight = null;
        if (animationJsonObject.has("blend_weight"))
            blendWeight = parser.parseExpression(animationJsonObject.get("blend_weight").getAsString(), false);

        Boolean overridePrevAnim = null;
        if (animationJsonObject.has("override_previous_animation")) {
            overridePrevAnim = animationJsonObject.get("override_previous_animation").getAsBoolean();
        }

        var boneAnimations = new ReferenceArrayList<BoneAnimation>();
        var customInstructionKeyframes = new ReferenceArrayList<EventKeyFrame<IValue[]>>();
        var soundKeyFrames = new ReferenceArrayList<EventKeyFrame<String>>();

        for (Map.Entry<String, JsonElement> keyFrame : getSoundEffects(animationJsonObject)) {
            double startTick = Double.parseDouble(keyFrame.getKey()) * 20;
            JsonElement value = keyFrame.getValue();

            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                soundKeyFrames.add(new EventKeyFrame<>(startTick, value.getAsString()));
            }

            if (value.isJsonObject()) {
                JsonObject effectObj = value.getAsJsonObject();

                if (effectObj.has("effect")) soundKeyFrames.add(new EventKeyFrame<>(startTick, effectObj.get("effect").getAsString()));
            }
        }

        for (Map.Entry<String, JsonElement> keyFrame : getCustomInstructionKeyFrames(animationJsonObject)) {
            double startTick = Double.parseDouble(keyFrame.getKey()) * 20;
            JsonElement value = keyFrame.getValue();
            if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                IValue[] values = JsonMolangUtils.getExpressions(array, parser, mergeMultilineExpr);

                customInstructionKeyframes.add(new EventKeyFrame<>(startTick, values));
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                IValue[] values = new IValue[]{parser.parseExpression(value.getAsString(), false)};
                customInstructionKeyframes.add(new EventKeyFrame<>(startTick, values));
            }
        }

        customInstructionKeyframes.sort(Comparator.comparingDouble(EventKeyFrame::getStartTick));

        for (Map.Entry<String, JsonElement> bone : getBones(animationJsonObject)) {
            List<RawBoneKeyFrame> rotationKeyFrames = Lists.newArrayList();
            List<RawBoneKeyFrame> positionKeyFrames = Lists.newArrayList();
            List<RawBoneKeyFrame> scaleKeyFrames = Lists.newArrayList();
            JsonObject boneJsonObj = bone.getValue().getAsJsonObject();

            JsonKeyFrameUtils.getKeyFrames(scaleKeyFrames, boneJsonObj.get("scale"), parser);
            JsonKeyFrameUtils.getKeyFrames(positionKeyFrames, boneJsonObj.get("position"), parser);
            JsonKeyFrameUtils.getKeyFrames(rotationKeyFrames, boneJsonObj.get("rotation"), parser);

            boneAnimations.add(new BoneAnimation(bone.getKey(), BoneKeyFrameProcessor.process(rotationKeyFrames, true), BoneKeyFrameProcessor.process(positionKeyFrames, false), BoneKeyFrameProcessor.process(scaleKeyFrames, false)));
        }

        if (animationLengthTicks == -1) {
            animationLengthTicks = calculateLength(boneAnimations);
        }
        return new Animation(animationName, animationLengthTicks, loop, null, null, blendWeight, overridePrevAnim, boneAnimations.toArray(new BoneAnimation[0]), soundKeyFrames.toArray(new EventKeyFrame[0]), new ParticleEventKeyFrame[0], customInstructionKeyframes.toArray(new EventKeyFrame[0]));
    }

    private static float calculateLength(List<BoneAnimation> boneAnimations) {
        float longestLength = 0;
        for (BoneAnimation animation : boneAnimations) {
            float xKeyframeTime = calculateKeyFrameListLength(animation.rotationKeyFrames);
            float yKeyframeTime = calculateKeyFrameListLength(animation.positionKeyFrames);
            float zKeyframeTime = calculateKeyFrameListLength(animation.scaleKeyFrames);
            longestLength = maxAll(longestLength, xKeyframeTime, yKeyframeTime, zKeyframeTime);
        }
        return longestLength == 0 ? Float.MAX_VALUE : longestLength;
    }

    private static float calculateKeyFrameListLength(List<BoneKeyFrame> boneKeyFrames) {
        if (boneKeyFrames.isEmpty()) {
            return 0;
        }
        return boneKeyFrames.get(boneKeyFrames.size() - 1).getStartTick();
    }

    public static float maxAll(float... values) {
        float max = 0;
        for (float value : values) {
            max = Math.max(value, max);
        }
        return max;
    }
}
