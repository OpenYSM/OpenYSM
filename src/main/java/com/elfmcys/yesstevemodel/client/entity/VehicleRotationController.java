package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.compat.immersiveaircraft.ImmersiveAirCraftCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.BoneTransformProvider;
import com.elfmcys.yesstevemodel.geckolib3.core.util.TransitionVector3f;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.client.compat.simpleplanes.SimplePlanesCompat;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class VehicleRotationController implements IAnimationController<GeckoVehicleEntity> {

    private final String modelId;

    private final GeckoVehicleEntity entity;

    private final ExpressionTransformProvider transformProvider = new ExpressionTransformProvider();

    private BoneTopLevelSnapshot boneTarget;

    private TransitionVector3f vehicleRotation;

    public VehicleRotationController(GeckoVehicleEntity entity, String str) {
        this.modelId = str;
        this.entity = entity;
    }

    @Override
    public String getName() {
        return this.modelId;
    }

    @Override
    public String getCurrentAnimation() {
        return "[Coded]";
    }

    @Nullable
    public Vector3f getVehicleRotation() {
        return this.vehicleRotation;
    }

    @Override
    public void init(List<BoneTopLevelSnapshot> list, Object2ReferenceMap<String, List<IValue>> object2ReferenceMap) {
        this.boneTarget = list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void process(AnimationEvent<GeckoVehicleEntity> event, ExpressionEvaluator<AnimationContext<?>> evaluator, boolean isFirstPerson) {
        ImmersiveAirCraftCompat.getAircraftRotation(event).or(() -> {
            return SimplePlanesCompat.getSimplePlanesRotation(event);
        }).ifPresent(vector3f -> {
            this.vehicleRotation = new TransitionVector3f(vector3f);
            this.vehicleRotation.setPercentCompleted(0.0f);
        });
    }

    @Override
    public void forEachTransform(Consumer<BoneTransformProvider> consumer) {
        if (this.boneTarget != null && this.vehicleRotation != null) {
            consumer.accept(this.transformProvider);
        }
    }

    @Override
    public void reset() {
        this.boneTarget = null;
        this.vehicleRotation = null;
    }

    private class ExpressionTransformProvider implements BoneTransformProvider {
        private ExpressionTransformProvider() {
        }

        @Override
        public BoneTopLevelSnapshot getBoneTarget() {
            return VehicleRotationController.this.boneTarget;
        }

        @Override
        public Optional<TransitionVector3f> getRotation(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            return Optional.ofNullable(VehicleRotationController.this.vehicleRotation);
        }

        @Override
        public Optional<TransitionVector3f> getPosition(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            return Optional.empty();
        }

        @Override
        public Optional<TransitionVector3f> getScale(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            return Optional.empty();
        }
    }
}