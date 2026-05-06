package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers;

import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.animation.predicate.EquipmentSlotAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.predicate.NamedAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity;
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle;
import com.elfmcys.yesstevemodel.client.model.processor.*;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController;
import com.elfmcys.yesstevemodel.client.model.processor.ArmorSlotProcessor;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.minecraft.world.entity.EquipmentSlot;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class FirstPersonArmAnimationController {

    private static final String FP_ARM_PREFIX = "fp.arm";

    private static final ProcessorPipeline<PlayerGeoEntity, PlayerModelBundle> processorRegistry = new ProcessorPipeline<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerDefaultProcessors() {
        registerNamedProcessor("misc", null, true, (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerParallelProcessor("parallel", (animationEntryKey, entity, linkedAnimationName) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, linkedAnimationName != null ? new NamedAnimationPredicate(linkedAnimationName) : StopAnimationPredicate.INSTANCE, true));
        registerArmorProcessor("armor", (animationEntryKey, entity, equipmentSlot) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new EquipmentSlotAnimationPredicate(equipmentSlot)));
    }

    public static Consumer<PlayerGeoEntity> buildControllers(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
        if (processorRegistry.isEmpty()) {
            registerDefaultProcessors();
        }
        return processorRegistry.buildAll(modelBundle, resourceBundle);
    }

    private static void registerSimpleProcessor(String slotName, BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        registerProcessorWithFilter(slotName, false, controllerFactory);
    }

    private static void registerProcessorWithFilter(String slotName, boolean skipOnPreview, BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        String animationEntryKey = String.format("%s.%s", FP_ARM_PREFIX, slotName);
        ModelProcessor<PlayerGeoEntity, PlayerModelBundle> processor = (modelData, resourceBundle) -> {
            return (entity, consumer) -> {
                consumer.accept(controllerFactory.apply(animationEntryKey, entity));
            };
        };
        if (skipOnPreview) {
            processor = processor.withFilter(entity -> entity instanceof IPreviewAnimatable);
        }
        processorRegistry.register(processor);
    }

    private static void registerMolangProcessor(String slotName, BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        processorRegistry.register(new ControllerSlotBinder(FP_ARM_PREFIX, slotName, DefaultBoneExpressionProvider.INSTANCE, controllerFactory));
    }

    private static void registerNamedProcessor(String slotName, String[] requiredAnimations, boolean checkAnimationEntries, BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        processorRegistry.register(new NamedModelProcessor(FP_ARM_PREFIX, slotName, requiredAnimations, checkAnimationEntries, DefaultBoneExpressionProvider.INSTANCE, controllerFactory));
    }

    private static void registerParallelProcessor(String slotName, TriFunction<String, PlayerGeoEntity, String, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        processorRegistry.register(new ParallelProcessor(FP_ARM_PREFIX, slotName, true, DefaultBoneExpressionProvider.INSTANCE, controllerFactory));
    }

    private static void registerArmorProcessor(String category, TriFunction<String, PlayerGeoEntity, EquipmentSlot, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        processorRegistry.register(new ArmorSlotProcessor(FP_ARM_PREFIX, category, DefaultBoneExpressionProvider.INSTANCE, controllerFactory));
    }

    private static class DefaultBoneExpressionProvider implements AnimationDataProvider<PlayerModelBundle> {

        public static final DefaultBoneExpressionProvider INSTANCE = new DefaultBoneExpressionProvider();

        private DefaultBoneExpressionProvider() {
        }

        @Override
        public Object2ReferenceMap<String, AnimationController> getAnimationEntries(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getAnimationEntries();
        }

        @Override
        public Object2ReferenceMap<String, Animation> getAnimations(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getArmAnimations();
        }

        @Override
        public ConditionArmor getConditionArmor(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getModelProcessor().getConditionArmor();
        }
    }
}