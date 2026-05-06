package com.elfmcys.yesstevemodel.client.model.processor;

import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;

import java.util.function.BiFunction;

public class NamedModelProcessor<T extends GeoEntity<?>, TModel> implements ModelProcessor<T, TModel> {

    private final String animationEntryKey;

    private final String controllerKey;

    private final String[] requiredAnimations;

    private final boolean checkAnimationEntries;

    private final AnimationDataProvider<TModel> animationDataProvider;

    private final BiFunction<String, T, IAnimationController<T>> controllerFactory;

    public NamedModelProcessor(String prefix, String slotName, String[] requiredAnimations, boolean checkAnimationEntries, AnimationDataProvider<TModel> animationDataProvider, BiFunction<String, T, IAnimationController<T>> controllerFactory) {
        this.animationEntryKey = String.format("%s.%s", prefix, slotName);
        this.controllerKey = String.format("%s_ctrl_%s", prefix, slotName);
        this.requiredAnimations = requiredAnimations;
        this.checkAnimationEntries = checkAnimationEntries;
        this.animationDataProvider = animationDataProvider;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public ControllerFactory<T> process(TModel modelData, ModelResourceBundle resourceBundle) {
        boolean hasContent = false;
        if ((this.checkAnimationEntries && this.animationDataProvider.getAnimationEntries(modelData, resourceBundle).containsKey(this.animationEntryKey)) || resourceBundle.getEvents().containsKey(this.controllerKey)) {
            hasContent = true;
        } else if (this.requiredAnimations != null) {
            Object2ReferenceMap<String, Animation> animations = this.animationDataProvider.getAnimations(modelData, resourceBundle);
            for (String requiredAnimation : this.requiredAnimations) {
                Animation animation = animations.get(requiredAnimation);
                if (animation != null && !animation.isEmpty()) {
                    hasContent = true;
                    break;
                }
            }
        }
        if (hasContent) {
            return (entity, consumer) -> {
                consumer.accept(this.controllerFactory.apply(this.animationEntryKey, entity));
            };
        }
        return (entity, consumer) -> {
        };
    }
}