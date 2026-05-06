package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers;

import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.predicate.RideStateAnimationPredicate;
import com.elfmcys.yesstevemodel.client.model.VehicleModelBundle;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.animation.predicate.EntityMovementPredicate;
import com.elfmcys.yesstevemodel.client.animation.predicate.MovementAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.predicate.NamedAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.model.processor.ModelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.ParallelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.ProcessorPipeline;
import com.elfmcys.yesstevemodel.client.model.processor.NamedModelProcessor;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.client.entity.VehicleRotationController;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class VehicleAnimationController {

    private static final String VEHICLE_PREFIX = "vehicle";

    public static final String ORIGIN_CONTROLLER_KEY = "vehicle.origin";

    private static final ProcessorPipeline<GeckoVehicleEntity, VehicleModelBundle> REGISTRY = new ProcessorPipeline<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerControllers() {
        registerParallelController("pre_parallel", (animationEntryKey, entity, linkedAnimationName) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.0f, linkedAnimationName != null ? new NamedAnimationPredicate(linkedAnimationName) : StopAnimationPredicate.INSTANCE));
        registerNamedController("pre_main", null, true, (animationEntryKey, entity) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerNamedController("main", EntityMovementPredicate.MOVEMENT_STATES, true, (animationEntryKey, entity) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.1f, new EntityMovementPredicate()));
        registerNamedController("move", MovementAnimationPredicate.ANIMATION_NAMES, true, (animationEntryKey, entity) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.1f, new MovementAnimationPredicate()));
        registerOriginController("origin", (animationEntryKey, entity) ->
            new VehicleRotationController(entity, animationEntryKey));
        registerNamedController("ride", RideStateAnimationPredicate.ANIMATION_NAMES, true, (animationEntryKey, entity) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.1f, new RideStateAnimationPredicate()));
        registerNamedController("post_main", null, true, (animationEntryKey, entity) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerParallelController("parallel", (animationEntryKey, entity, linkedAnimationName) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.0f, linkedAnimationName != null ? new NamedAnimationPredicate(linkedAnimationName) : StopAnimationPredicate.INSTANCE, true));
    }

    public static Consumer<GeckoVehicleEntity> buildControllers(VehicleModelBundle modelBundle, ModelResourceBundle resourceBundle) {
        if (REGISTRY.isEmpty()) {
            registerControllers();
        }
        return REGISTRY.buildAll(modelBundle, resourceBundle);
    }

    private static ModelProcessor<GeckoVehicleEntity, VehicleModelBundle> registerOriginController(String controllerName, BiFunction<String, GeckoVehicleEntity, IAnimationController<GeckoVehicleEntity>> controllerFactory) {
        String controllerKey = String.format("%s.%s", VEHICLE_PREFIX, controllerName);
        return REGISTRY.register((modelData, resourceBundle) -> (entity, consumer) -> consumer.accept(controllerFactory.apply(controllerKey, entity)));
    }

    private static ModelProcessor<GeckoVehicleEntity, VehicleModelBundle> registerNamedController(String slotName, String[] requiredAnimations, boolean checkAnimationEntries, BiFunction<String, GeckoVehicleEntity, IAnimationController<GeckoVehicleEntity>> controllerFactory) {
        return REGISTRY.register(new NamedModelProcessor<>(VEHICLE_PREFIX, slotName, requiredAnimations, checkAnimationEntries, VehicleAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static ModelProcessor<GeckoVehicleEntity, VehicleModelBundle> registerParallelController(String slotName, TriFunction<String, GeckoVehicleEntity, String, IAnimationController<GeckoVehicleEntity>> controllerFactory) {
        return REGISTRY.register(new ParallelProcessor<>(VEHICLE_PREFIX, slotName, false, VehicleAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static class VehicleAnimationDataProvider implements AnimationDataProvider<VehicleModelBundle> {

        public static final VehicleAnimationDataProvider INSTANCE = new VehicleAnimationDataProvider();

        private VehicleAnimationDataProvider() {
        }

        @Override
        public Object2ReferenceMap<String, AnimationController> getAnimationEntries(VehicleModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getAnimationControllers();
        }

        @Override
        public Object2ReferenceMap<String, Animation> getAnimations(VehicleModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getAnimations();
        }

        @Override
        public ConditionArmor getConditionArmor(VehicleModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return null;
        }
    }
}