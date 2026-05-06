package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.animation.predicate.*;
import com.elfmcys.yesstevemodel.client.compat.gun.common.ItemUseAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.animation.MaidGameStateAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.animation.MaidIdleAnimPredicate;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.animation.MaidStatusAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapability;
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle;
import com.elfmcys.yesstevemodel.client.model.processor.ModelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.NamedModelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.ParallelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.ProcessorPipeline;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate;
import com.elfmcys.yesstevemodel.client.model.processor.ControllerSlotBinder;
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

public class MaidAnimationController {

    private static final String PLAYER_PREFIX = "player";

    private static final String MAID_PREFIX = "maid";

    private static final ProcessorPipeline<MaidCapability, PlayerModelBundle> REGISTRY = new ProcessorPipeline<>();

    @SuppressWarnings("unchecked")
    private static void registerControllers() {
        registerParallelController("pre_parallel", (name, cap, animationName) ->
                new CompositeAnimationController(cap, name, 0.0f, animationName != null ? new NamedAnimationPredicate(animationName) : StopAnimationPredicate.INSTANCE));

        registerController("vehicle", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.1f, new LivingMovementAnimationPredicate()));

        registerSlotController("pre_main", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new StopAnimationPredicate()));

        registerController("main", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.1f, new TouhouMaidAnimationPredicate()));

        registerSlotController("post_main", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new StopAnimationPredicate()));

        registerSlotController("pre_hold", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new StopAnimationPredicate()));

        registerController("hold_offhand", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.1f, new OffHandHoldPredicate()));

        registerController("hold_mainhand", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.1f, new MainHandHoldPredicate()));

        registerSlotController("post_hold", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new StopAnimationPredicate()));

        if (ItemUseAnimationPredicate.isLoaded()) {
            registerController("fire", (name, cap) ->
                    new CompositeAnimationController(cap, name, 0.0f, new ItemUseAnimationPredicate()));
        }

        registerSlotController("pre_swing", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new StopAnimationPredicate()));

        registerController("swing", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new ItemHoldAnimationPredicate()));

        registerSlotController("post_swing", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new StopAnimationPredicate()));

        registerSlotController("pre_use", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new StopAnimationPredicate()));

        registerController("use", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.1f, new InteractionHandAnimationPredicate()));

        registerSlotController("post_use", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new StopAnimationPredicate()));

        registerNamedController("misc", MaidGameStateAnimationPredicate.GAME_STATE_ANIMATIONS, true, (name, cap) ->
                new CompositeAnimationController(cap, name, 0.1f, new MaidGameStateAnimationPredicate()));

        registerController("passenger", (name, cap) ->
                new CompositeAnimationController(cap, name, 0.1f, new OffhandAttackAnimationPredicate()));

        registerController("cap", (name, cap) ->
                new PredicateBasedController(cap, name, 0.0f, new MaidIdleAnimPredicate()));

        registerParallelController("parallel", (name, cap, animationName) ->
                new CompositeAnimationController(cap, name, 0.0f, animationName != null ? new NamedAnimationPredicate(animationName) : StopAnimationPredicate.INSTANCE, true));

        registerArmorController("armor", (name, cap, equipmentSlot) ->
                new CompositeAnimationController(cap, name, 0.0f, new ArmorPredicate(equipmentSlot)));

        registerNamedController("statue", MaidStatusAnimationPredicate.RENDER_STATES, true, (name, cap) ->
                new CompositeAnimationController(cap, name, 0.0f, new MaidStatusAnimationPredicate()));
    }

    public static Consumer<MaidCapability> buildControllers(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
        if (REGISTRY.isEmpty()) {
            registerControllers();
        }
        return REGISTRY.buildAll(modelBundle, resourceBundle);
    }

    private static ModelProcessor<MaidCapability, PlayerModelBundle> registerController(String controllerName, BiFunction<String, MaidCapability, IAnimationController<MaidCapability>> controllerFactory) {
        String controllerKey = String.format("%s.%s", PLAYER_PREFIX, controllerName);
        return REGISTRY.register((modelBundle, resourceBundle) -> (capability, consumer) -> {
            consumer.accept(controllerFactory.apply(controllerKey, capability));
        });
    }

    private static ModelProcessor<MaidCapability, PlayerModelBundle> registerSlotController(String slotName, BiFunction<String, MaidCapability, IAnimationController<MaidCapability>> controllerFactory) {
        return REGISTRY.register(new ControllerSlotBinder<>(PLAYER_PREFIX, slotName, MaidAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static ModelProcessor<MaidCapability, PlayerModelBundle> registerNamedController(String slotName, String[] requiredAnimations, boolean checkAnimationEntries, BiFunction<String, MaidCapability, IAnimationController<MaidCapability>> controllerFactory) {
        return REGISTRY.register(new NamedModelProcessor<>(MAID_PREFIX, slotName, requiredAnimations, checkAnimationEntries, MaidAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static ModelProcessor<MaidCapability, PlayerModelBundle> registerParallelController(String slotName, TriFunction<String, MaidCapability, String, IAnimationController<MaidCapability>> controllerFactory) {
        return REGISTRY.register(new ParallelProcessor<>(PLAYER_PREFIX, slotName, true, MaidAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static ModelProcessor<MaidCapability, PlayerModelBundle> registerArmorController(String category, TriFunction<String, MaidCapability, EquipmentSlot, IAnimationController<MaidCapability>> controllerFactory) {
        return REGISTRY.register(new ArmorSlotProcessor<>(PLAYER_PREFIX, category, MaidAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static class MaidAnimationDataProvider implements AnimationDataProvider<PlayerModelBundle> {

        public static final MaidAnimationDataProvider INSTANCE = new MaidAnimationDataProvider();

        private MaidAnimationDataProvider() {
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