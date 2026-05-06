package com.elfmcys.yesstevemodel.client.model.processor;

import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ControllerSlotBinder<T extends GeoEntity<?>, TModel> implements ModelProcessor<T, TModel> {

    private final Predicate<String> controllerNameMatcher;

    private final Predicate<String> molangEventMatcher;

    private final AnimationDataProvider<TModel> animationDataProvider;

    private final BiFunction<String, T, IAnimationController<T>> controllerFactory;

    public ControllerSlotBinder(String prefix, String slotName, AnimationDataProvider<TModel> animationDataProvider, BiFunction<String, T, IAnimationController<T>> controllerFactory) {
        this.controllerNameMatcher = Pattern.compile(String.format("^%s\\.%s(_.+){0,1}$", prefix, slotName)).asMatchPredicate();
        this.molangEventMatcher = Pattern.compile(String.format("^%s_ctrl_%s(_.+){0,1}$", prefix, slotName)).asMatchPredicate();
        this.animationDataProvider = animationDataProvider;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public ControllerFactory<T> process(TModel modelData, ModelResourceBundle resourceBundle) {
        ObjectRBTreeSet<String> controllerNames = new ObjectRBTreeSet<>();
        Object2ReferenceMaps.fastForEach(this.animationDataProvider.getAnimationEntries(modelData, resourceBundle), entry -> {
            if (this.controllerNameMatcher.test(entry.getKey())) {
                controllerNames.add(entry.getKey());
            }
        });
        Object2ReferenceMaps.fastForEach(resourceBundle.getEvents(), entry -> {
            if (this.molangEventMatcher.test(entry.getKey())) {
                controllerNames.add(entry.getKey().replace("_ctrl_", "."));
            }
        });
        return (entity, consumer) -> {
            for (String controllerName : controllerNames) {
                consumer.accept(this.controllerFactory.apply(controllerName, entity));
            }
        };
    }
}