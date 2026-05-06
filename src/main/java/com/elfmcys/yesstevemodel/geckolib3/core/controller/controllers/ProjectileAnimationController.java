package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers;

import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.animation.predicate.NamedAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.predicate.ProjectileAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity;
import com.elfmcys.yesstevemodel.client.model.ProjectileModelBundle;
import com.elfmcys.yesstevemodel.client.model.processor.ModelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.NamedModelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.ParallelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.ProcessorPipeline;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ProjectileAnimationController {

    private static final String PROJECTILE_PREFIX = "projectile";

    private static final ProcessorPipeline<GeckoProjectileEntity, ProjectileModelBundle> REGISTRY = new ProcessorPipeline<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerControllers() {
        registerNamedController("pre_main", null, true, (animationEntryKey, entity) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerNamedController("main", ProjectileAnimationPredicate.ENVIRONMENT_STATES, true, (animationEntryKey, entity) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.1f, new ProjectileAnimationPredicate()));
        registerNamedController("post_main", null, true, (animationEntryKey, entity) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerParallelController("parallel", (animationEntryKey, entity, linkedAnimationName) ->
            new CompositeAnimationController(entity, animationEntryKey, 0.0f, linkedAnimationName != null ? new NamedAnimationPredicate(linkedAnimationName) : StopAnimationPredicate.INSTANCE, true));
    }

    public static Consumer<GeckoProjectileEntity> buildControllers(ProjectileModelBundle modelBundle, ModelResourceBundle resourceBundle) {
        if (REGISTRY.isEmpty()) {
            registerControllers();
        }
        return REGISTRY.buildAll(modelBundle, resourceBundle);
    }

    private static ModelProcessor<GeckoProjectileEntity, ProjectileModelBundle> registerController(String controllerName, BiFunction<String, GeckoProjectileEntity, IAnimationController<GeckoProjectileEntity>> controllerFactory) {
        String controllerKey = String.format("%s.%s", PROJECTILE_PREFIX, controllerName);
        return REGISTRY.register((modelData, resourceBundle) -> {
            return (entity, consumer) -> {
                consumer.accept(controllerFactory.apply(controllerKey, entity));
            };
        });
    }

    private static ModelProcessor<GeckoProjectileEntity, ProjectileModelBundle> registerNamedController(String slotName, String[] requiredAnimations, boolean checkAnimationEntries, BiFunction<String, GeckoProjectileEntity, IAnimationController<GeckoProjectileEntity>> controllerFactory) {
        return REGISTRY.register(new NamedModelProcessor<>(PROJECTILE_PREFIX, slotName, requiredAnimations, checkAnimationEntries, ProjectileAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static ModelProcessor<GeckoProjectileEntity, ProjectileModelBundle> registerParallelController(String slotName, TriFunction<String, GeckoProjectileEntity, String, IAnimationController<GeckoProjectileEntity>> controllerFactory) {
        return REGISTRY.register(new ParallelProcessor<>(PROJECTILE_PREFIX, slotName, false, ProjectileAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static class ProjectileAnimationDataProvider implements AnimationDataProvider<ProjectileModelBundle> {

        public static final ProjectileAnimationDataProvider INSTANCE = new ProjectileAnimationDataProvider();

        private ProjectileAnimationDataProvider() {
        }

        @Override
        public Object2ReferenceMap<String, AnimationController> getAnimationEntries(ProjectileModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getAnimationControllers();
        }

        @Override
        public Object2ReferenceMap<String, Animation> getAnimations(ProjectileModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getAnimations();
        }

        @Override
        public ConditionArmor getConditionArmor(ProjectileModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return null;
        }
    }
}