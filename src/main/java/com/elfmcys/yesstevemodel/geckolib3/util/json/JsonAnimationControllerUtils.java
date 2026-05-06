package com.elfmcys.yesstevemodel.geckolib3.util.json;

import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.util.LinearKeyframeInterpolator;
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable;
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 解析动画控制器
 */
public class JsonAnimationControllerUtils {
    public static Set<Map.Entry<String, JsonElement>> getAnimationControllers(JsonObject json) {
        if (json.has("animation_controllers")) {
            return json.getAsJsonObject("animation_controllers").entrySet();
        }
        return ImmutableSet.of();
    }

    public static List<Map.Entry<String, JsonElement>> getStates(JsonObject json) {
        JsonObject states = json.getAsJsonObject("states");
        return states == null ? List.of() : new ArrayList<>(states.entrySet());
    }

    public static List<JsonElement> getAnimations(JsonObject json) {
        JsonArray animations = json.getAsJsonArray("animations");
        return animations == null ? List.of() : animations.asList();
    }

    @SuppressWarnings("unchecked")
    public static AnimationController deserializeJsonToAnimationController(Map.Entry<String, JsonElement> element, MolangParser parser, boolean mergeMultilineExpr)
            throws ClassCastException, IllegalStateException {
        JsonObject animCtrlJsonObject = element.getValue().getAsJsonObject();

        var initialState = "default";
        if (animCtrlJsonObject.has("initial_state"))
            initialState = animCtrlJsonObject.get("initial_state").getAsString();

        var states = new ReferenceArrayList<AnimationState>();

        for (Map.Entry<String, JsonElement> state : getStates(animCtrlJsonObject)) {
            String name = state.getKey();
            JsonObject stateJsonObject = state.getValue().getAsJsonObject();

            List<Pair<String, IValue>> animations = Lists.newArrayList();
            List<Pair<String, IValue>> transitions = Lists.newArrayList();
            List<String> soundEffects = Lists.newArrayList();

            getAnimations(animations, stateJsonObject.get("animations"), parser);
            getTransitions(transitions, stateJsonObject.get("transitions"), parser);
            getSoundEffects(soundEffects, stateJsonObject.get("sound_effects"));

            IValue[] onEntry = JsonMolangUtils.getExpressions(stateJsonObject.get("on_entry"), parser, mergeMultilineExpr);
            IValue[] onExit = JsonMolangUtils.getExpressions(stateJsonObject.get("on_exit"), parser, mergeMultilineExpr);

            IInterpolable blendTransition = getBlendTransition(stateJsonObject.get("blend_transition"));

            boolean blendViaShortestPath = false;
            if (stateJsonObject.has("blend_via_shortest_path"))
                blendViaShortestPath = stateJsonObject.get("blend_via_shortest_path").getAsBoolean();

            states.add(new AnimationState(name,
                    animations.toArray(new Pair[0]),
                    transitions.toArray(new Pair[0]),
                    soundEffects.toArray(new String[0]),
                    onEntry, onExit,
                    blendTransition,
                    blendViaShortestPath
            ));
        }

        return new AnimationController(initialState, states.toArray(new AnimationState[0]));
    }


    private static void getAnimations(List<Pair<String, IValue>> animations, @Nullable JsonElement element, MolangParser parser) {
        if (element == null) return;

        if (!element.isJsonArray()) return;

        for (JsonElement animation : element.getAsJsonArray()) {
            if (animation.isJsonPrimitive()) {
                animations.add(Pair.of(animation.getAsString(), null));
            } else if (animation.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : animation.getAsJsonObject().entrySet()) {
                    animations.add(Pair.of(entry.getKey(), parser.parseExpression(entry.getValue().getAsString(), false)));
                }
            }
        }
    }

    private static void getTransitions(List<Pair<String, IValue>> transitions, @Nullable JsonElement element, MolangParser parser) {
        if (element == null) return;

        if (!element.isJsonArray()) return;

        for (JsonElement transition : element.getAsJsonArray()) {
            for (Map.Entry<String, JsonElement> entry : transition.getAsJsonObject().entrySet()) {
                transitions.add(Pair.of(entry.getKey(), parser.parseExpression(entry.getValue().getAsString(), false)));
            }
        }
    }

    private static void getSoundEffects(List<String> soundEffects, @Nullable JsonElement element) {
        if (element == null) return;

        if (!element.isJsonArray()) return;

        for (JsonElement soundEffect : element.getAsJsonArray()) {
            if (soundEffect.isJsonObject()) {
                JsonObject soundEffectObj = soundEffect.getAsJsonObject();
                if (soundEffectObj.has("effect")) {
                    soundEffects.add(soundEffectObj.get("effect").getAsString());
                }
            } else if (soundEffect.isJsonPrimitive()) {
                soundEffects.add(soundEffect.getAsString());
            }
        }
    }

    private static IInterpolable getBlendTransition(@Nullable JsonElement element) {
        if (element == null) return new TicksInterpolator(0.0f);

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return new TicksInterpolator(element.getAsFloat());
        } else if (element.isJsonObject()) {
            JsonObject blendJsonObj = element.getAsJsonObject();
            float[] keys = new float[blendJsonObj.size()];
            float[] values = new float[blendJsonObj.size()];
            int i = 0;

            List<Map.Entry<String, JsonElement>> sortedBlend = new ArrayList<>(blendJsonObj.entrySet());
            sortedBlend.sort(Comparator.comparingDouble(e -> Double.parseDouble(e.getKey())));

            for (Map.Entry<String, JsonElement> blendEntry : sortedBlend) {
                keys[i] = Float.parseFloat(blendEntry.getKey());
                values[i] = blendEntry.getValue().getAsFloat();
                i++;
            }
            return new LinearKeyframeInterpolator(keys, values);
        }
        return new TicksInterpolator(0.0f);
    }
}
