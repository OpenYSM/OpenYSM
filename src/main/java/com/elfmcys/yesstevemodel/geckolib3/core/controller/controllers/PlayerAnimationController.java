package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers;

import com.elfmcys.yesstevemodel.client.animation.AnimationManager;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.animation.predicate.*;
import com.elfmcys.yesstevemodel.client.compat.gun.common.ItemUseAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.compat.carryon.CarryOnCompat;
import com.elfmcys.yesstevemodel.client.compat.parcool.ParcoolCompat;
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle;
import com.elfmcys.yesstevemodel.client.model.processor.*;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController;
import com.elfmcys.yesstevemodel.client.model.processor.ArmorSlotProcessor;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.minecraft.world.entity.EquipmentSlot;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PlayerAnimationController {

    private static final ProcessorPipeline<CustomPlayerEntity, PlayerModelBundle> REGISTRY = new ProcessorPipeline<>();

    private static final String PLAYER_PREFIX = "player";

    public static final String CAP_CONTROLLER_KEY = String.format("%s.%s", PLAYER_PREFIX, "cap");

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerControllers() {
        registerParallelController("pre_parallel", (animationEntryKey, entity, linkedAnimationName) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, linkedAnimationName != null ? new NamedAnimationPredicate(linkedAnimationName) : StopAnimationPredicate.INSTANCE));
        ParcoolCompat.getControllerFactory().ifPresent(controllerFactory -> {
            registerController("parcool", controllerFactory);
        });
        registerController("vehicle", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.1f, new LivingMovementAnimationPredicate()));
        registerSlotController("pre_main", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerController("main", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.1f, new AnimationManager()));
        registerSlotController("post_main", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerSlotController("pre_hold", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerController("hold_offhand", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.1f, new OffHandHoldPredicate()));
        registerController("hold_mainhand", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.1f, new MainHandHoldPredicate()));
        registerSlotController("post_hold", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        if (ItemUseAnimationPredicate.isLoaded()) {
            registerController("fire", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new ItemUseAnimationPredicate()));
        }
        registerSlotController("pre_swing", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerController("swing", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new ItemHoldAnimationPredicate()));
        registerSlotController("post_swing", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerSlotController("pre_use", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerController("use", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.1f, new InteractionHandAnimationPredicate()));
        registerSlotController("post_use", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerController("passenger", (animationEntryKey, entity) -> new CompositeAnimationController(entity, animationEntryKey, 0.1f, new OffhandAttackAnimationPredicate()));
        CarryOnCompat.getControllerFactory().ifPresent(controllerFactory -> registerController("carry_on", controllerFactory));
        registerController("cap", (animationEntryKey, entity) -> new PredicateBasedController(entity, animationEntryKey, 0.0f, new PlayerBaseAnimationPredicate()));
        registerController("gui_hover", true, (animationEntryKey, entity) -> new PredicateBasedController(entity, animationEntryKey, 0.0f, new PlayerCustomAnimationPredicate()));
        registerController("gui_focus", true, (animationEntryKey, entity) -> new PredicateBasedController(entity, animationEntryKey, 0.0f, new PlayerIdleAnimationPredicate()));
        registerParallelController("parallel", (animationEntryKey, entity, linkedAnimationName) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, linkedAnimationName != null ? new NamedAnimationPredicate(linkedAnimationName) : StopAnimationPredicate.INSTANCE, true));
        registerArmorController("armor", (animationEntryKey, entity, equipmentSlot) -> new CompositeAnimationController(entity, animationEntryKey, 0.0f, new ArmorPredicate(equipmentSlot)));
    }

    public static Consumer<CustomPlayerEntity> buildControllers(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
        if (REGISTRY.isEmpty()) {
            registerControllers();
        }
        return REGISTRY.buildAll(modelBundle, resourceBundle);
    }

    public static void registerController(String controllerName, BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>> controllerFactory) {
        registerController(controllerName, false, controllerFactory);
    }

    private static void registerController(String controllerName, boolean guiOnly, BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>> controllerFactory) {
        String controllerKey = String.format("%s.%s", PLAYER_PREFIX, controllerName);
        ModelProcessor<CustomPlayerEntity, PlayerModelBundle> processor = (modelData, resourceBundle) -> (entity, consumer) -> {
            consumer.accept(controllerFactory.apply(controllerKey, entity));
        };
        if (guiOnly) {
            processor = processor.withFilter(entity -> entity instanceof IPreviewAnimatable);
        }
        REGISTRY.register(processor);
    }

    private static void registerSlotController(String slotName, BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>> controllerFactory) {
        REGISTRY.register(new ControllerSlotBinder(PLAYER_PREFIX, slotName, PlayerAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static void registerNamedController(String slotName, String[] requiredAnimations, boolean checkAnimationEntries, BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>> controllerFactory) {
        REGISTRY.register(new NamedModelProcessor(PLAYER_PREFIX, slotName, requiredAnimations, checkAnimationEntries, PlayerAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static void registerParallelController(String slotName, TriFunction<String, CustomPlayerEntity, String, IAnimationController<CustomPlayerEntity>> controllerFactory) {
        REGISTRY.register(new ParallelProcessor(PLAYER_PREFIX, slotName, true, PlayerAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static void registerArmorController(String category, TriFunction<String, CustomPlayerEntity, EquipmentSlot, IAnimationController<CustomPlayerEntity>> controllerFactory) {
        REGISTRY.register(new ArmorSlotProcessor(PLAYER_PREFIX, category, PlayerAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static class PlayerAnimationDataProvider implements AnimationDataProvider<PlayerModelBundle> {

        public static final PlayerAnimationDataProvider INSTANCE = new PlayerAnimationDataProvider();

        private PlayerAnimationDataProvider() {
        }

        @Override
        public Object2ReferenceMap<String, AnimationController> getAnimationEntries(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getAnimationEntries();
        }

        @Override
        public Object2ReferenceMap<String, Animation> getAnimations(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getMainAnimations();
        }

        @Override
        public ConditionArmor getConditionArmor(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getConditionManager().getArmor();
        }
    }
}