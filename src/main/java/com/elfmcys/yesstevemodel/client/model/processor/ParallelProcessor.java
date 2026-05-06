package com.elfmcys.yesstevemodel.client.model.processor;

import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceRBTreeMap;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ParallelProcessor<T extends GeoEntity<?>, TModel> implements ModelProcessor<T, TModel> {

    private final String prefix;

    private final String slotName;

    private final Predicate<String> animationEntryMatcher;

    private final Predicate<String> controllerEntryMatcher;

    private final Predicate<String> animationNameMatcher;

    private final AnimationDataProvider<TModel> animationDataProvider;

    private final TriFunction<String, T, String, IAnimationController<T>> controllerFactory;

    public ParallelProcessor(String prefix, String slotName, boolean allowExtraSlots, AnimationDataProvider<TModel> animationDataProvider, TriFunction<String, T, String, IAnimationController<T>> controllerFactory) {
        this.prefix = prefix;
        this.slotName = slotName;
        if (allowExtraSlots) {
            this.animationEntryMatcher = Pattern.compile(String.format("^%s\\.%s_.+", prefix, slotName)).asMatchPredicate();
            this.controllerEntryMatcher = Pattern.compile(String.format("^%s_ctrl_%s_.+", prefix, slotName)).asMatchPredicate();
        } else {
            this.animationEntryMatcher = Pattern.compile(String.format("^%s\\.%s_[0-7]$", prefix, slotName)).asMatchPredicate();
            this.controllerEntryMatcher = Pattern.compile(String.format("^%s_ctrl_%s_[0-7]$", prefix, slotName)).asMatchPredicate();
        }
        this.animationNameMatcher = Pattern.compile(String.format("^%s[0-7]$", slotName)).asMatchPredicate();
        this.animationDataProvider = animationDataProvider;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public ControllerFactory<T> process(TModel modelData, ModelResourceBundle resourceBundle) {
        Object2ReferenceRBTreeMap matchedSlots = new Object2ReferenceRBTreeMap();
        Object2ReferenceMaps.fastForEach(this.animationDataProvider.getAnimationEntries(modelData, resourceBundle), entry -> {
            if (this.animationEntryMatcher.test(entry.getKey())) {
                matchedSlots.put(entry.getKey(), null);
            }
        });
        Object2ReferenceMap<String, Animation> animations = this.animationDataProvider.getAnimations(modelData, resourceBundle);
        Object2ReferenceMaps.fastForEach(resourceBundle.getEvents(), event -> {
            if (this.controllerEntryMatcher.test(event.getKey())) {
                String controllerName = event.getKey().replace("_ctrl_", ".");
                try {
                    String suffix = controllerName.substring(this.prefix.length() + this.slotName.length() + 2);
                    int slotIndex = Integer.parseInt(suffix);
                    if (slotIndex >= 0 && slotIndex <= 7) {
                        String animationName = this.slotName + suffix;
                        if (animations.containsKey(animationName)) {
                            matchedSlots.put(controllerName, animationName);
                            return;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
                matchedSlots.put(controllerName, null);
            }
        });
        Object2ReferenceMaps.fastForEach(animations, animEntry -> {
            if (!animEntry.getValue().isEmpty() && this.animationNameMatcher.test(animEntry.getKey())) {
                matchedSlots.put(String.format("%s.%s_%s", this.prefix, this.slotName, animEntry.getKey().substring(this.slotName.length())), animEntry.getKey());
            }
        });
        return (entity, consumer) -> {
            Object2ReferenceMaps.fastForEach(matchedSlots, slot -> {
                consumer.accept(this.controllerFactory.apply((String) slot.getKey(), entity, (String) slot.getValue()));
            });
        };
    }
}