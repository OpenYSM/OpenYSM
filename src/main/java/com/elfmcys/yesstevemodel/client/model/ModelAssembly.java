package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.client.gui.metadata.ModelDisplayAssets;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public class ModelAssembly {

    private final PlayerModelBundle animationBundle;

    private final Map<ResourceLocation, ProjectileModelBundle> projectileModels;

    private final Map<ResourceLocation, VehicleModelBundle> vehicleModels;

    private final ModelResourceBundle expressionCache;

    private final ServerModelInfo modelData;

    private final ModelDisplayAssets textureRegistry;

    private final List<AbstractTexture> textures;

    public ModelAssembly(PlayerModelBundle animationBundle, Map<ResourceLocation, ProjectileModelBundle> projectileModels, Map<ResourceLocation, VehicleModelBundle> vehicleModels, ModelResourceBundle expressionCache, ServerModelInfo modelData, ModelDisplayAssets textureRegistry, List<AbstractTexture> list) {
        this.animationBundle = animationBundle;
        this.projectileModels = projectileModels;
        this.vehicleModels = vehicleModels;
        this.expressionCache = expressionCache;
        this.modelData = modelData;
        this.textureRegistry = textureRegistry;
        this.textures = list;
    }

    public PlayerModelBundle getAnimationBundle() {
        return this.animationBundle;
    }

    public List<AbstractTexture> getTextures() {
        return this.textures;
    }

    public ModelResourceBundle getExpressionCache() {
        return this.expressionCache;
    }

    public Map<ResourceLocation, ProjectileModelBundle> getProjectileModels() {
        return this.projectileModels;
    }

    public Map<ResourceLocation, VehicleModelBundle> getVehicleModels() {
        return this.vehicleModels;
    }

    public ServerModelInfo getModelData() {
        return this.modelData;
    }

    public ModelDisplayAssets getTextureRegistry() {
        return this.textureRegistry;
    }

    public String getDisplayName(String str) {
        Metadata name = getModelData().getExtraInfo();
        if (name != null) {
            return ModelMetadataPresenter.getLocalizedModelString(this, "metadata.name", name.getName());
        }
        return str;
    }
}