package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.VehicleAnimationController;
import com.elfmcys.yesstevemodel.client.model.VehicleModelBundle;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.client.entity.VehicleRotationController;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class GeckoVehicleEntity extends GeoEntity<Entity> {

    private VehicleModelBundle vehicleModel;

    private VehicleRotationController expressionBuilder;

    public GeckoVehicleEntity(Entity entity) {
        super(entity, true);
    }

    @Override
    public void registerAnimationControllers() {
        if (this.vehicleModel != null) {
            this.vehicleModel.getAnimatableConsumer().accept(this);
            this.expressionBuilder = (VehicleRotationController) getAnimationData().getAnimationControllerByName(VehicleAnimationController.ORIGIN_CONTROLLER_KEY);
        }
    }

    @Nullable
    public Vector3f getExpressionOffset() {
        if (this.expressionBuilder != null) {
            return this.expressionBuilder.getVehicleRotation();
        }
        return null;
    }

    @Override
    @Nullable
    public GeoEntity.ModelWrapper buildRenderShape(ModelAssembly modelAssembly, boolean isDefault) {
        VehicleModelBundle modelBundle = modelAssembly.getVehicleModels().get(this.entity.getType().builtInRegistryHolder().key().location());
        if (modelBundle != null) {
            return new EntityModelWrapper(modelAssembly, isDefault, modelBundle);
        }
        return null;
    }

    @Override
    public void onModelLoaded(ModelAssembly modelAssembly) {
        super.onModelLoaded(modelAssembly);
        this.vehicleModel = modelAssembly.getVehicleModels().get(this.entity.getType().builtInRegistryHolder().key().location());
    }

    @Override
    public void clearModel() {
        super.clearModel();
        this.vehicleModel = null;
        this.expressionBuilder = null;
    }

    @Override
    public GeoModel getAnimationProcessor() {
        return this.vehicleModel.getModel();
    }

    @Override
    @NotNull
    public ResourceLocation getTextureLocation() {
        return ((EntityModelWrapper) getRenderShape()).textureLocatable.getResourceLocation().orElseGet(MissingTextureAtlasSprite::getLocation);
    }

    @Override
    public Animation getAnimation(String str) {
        return this.vehicleModel.getAnimations().get(str);
    }

    @Override
    @Nullable
    public AnimationController getAnimationEntries(String str) {
        return this.vehicleModel.getAnimationControllers().get(str);
    }

    @Override
    public boolean isModelReady() {
        return super.isModelReady() && this.vehicleModel != null && getRenderShape().isValid();
    }

    @Override
    public float getHeightScale() {
        return 0.7f;
    }

    @Override
    public float getWidthScale() {
        return 0.7f;
    }

    private static class EntityModelWrapper extends ModelWrapper {

        private final IResourceLocatable textureLocatable;

        public EntityModelWrapper(ModelAssembly modelAssembly, boolean isDefault, VehicleModelBundle modelBundle) {
            super(modelAssembly, isDefault);
            this.textureLocatable = UploadManager.getOrCreateLocatable(modelBundle.getTexture(), true);
        }

        @Override
        public boolean isValid() {
            return this.textureLocatable.getResourceLocation().isPresent();
        }
    }
}