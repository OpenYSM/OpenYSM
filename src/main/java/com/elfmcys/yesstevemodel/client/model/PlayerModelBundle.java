package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.FirstPersonArmAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.minecraft.client.renderer.texture.AbstractTexture;

import java.util.function.Consumer;

public class PlayerModelBundle {

    private final GeoModel mainModel;

    private final GeoModel armModel;

    private final Object2ReferenceMap<String, Animation> mainAnimations;

    private final Object2ReferenceMap<String, Animation> armAnimations;

    private final ConditionManager conditionManager;

    private final ArmorConditions modelProcessor;

    private final Object2ReferenceMap<String, AnimationController> animationEntries;

    private final OrderedStringMap<String, ? extends AbstractTexture> textures;

    private final String defaultTextureName;

    private final AbstractTexture defaultTexture;

    private final Consumer<CustomPlayerEntity> playerControllerInstaller;

    private final Consumer<PlayerGeoEntity> armControllerInstaller;

    private final Object maidControllerInstaller;

    public PlayerModelBundle(GeoModel mainModel,
                             GeoModel armModel,
                             Object2ReferenceMap<String, Animation> mainAnimations,
                             Object2ReferenceMap<String, Animation> armAnimations,
                             ConditionManager conditionManager,
                             ArmorConditions modelProcessor,
                             Object2ReferenceMap<String, AnimationController> animationEntries,
                             OrderedStringMap<String, ? extends AbstractTexture> textures,
                             String defaultTextureName, AbstractTexture defaultTexture, ModelResourceBundle modelResourceBundle) {
        this.mainModel = mainModel;
        this.armModel = armModel;
        this.mainAnimations = mainAnimations;
        this.armAnimations = armAnimations;
        this.conditionManager = conditionManager;
        this.animationEntries = animationEntries;
        this.modelProcessor = modelProcessor;
        this.textures = textures;
        this.defaultTextureName = defaultTextureName;
        this.defaultTexture = defaultTexture;
        this.playerControllerInstaller = PlayerAnimationController.buildControllers(this, modelResourceBundle);
        this.armControllerInstaller = FirstPersonArmAnimationController.buildControllers(this, modelResourceBundle);
        this.maidControllerInstaller = TouhouLittleMaidCompat.buildControllers(this, modelResourceBundle);
    }

    public GeoModel getMainModel() {
        return this.mainModel;
    }

    public GeoModel getArmModel() {
        return this.armModel;
    }

    public Object2ReferenceMap<String, Animation> getMainAnimations() {
        return this.mainAnimations;
    }

    public Object2ReferenceMap<String, Animation> getArmAnimations() {
        return this.armAnimations;
    }

    public ConditionManager getConditionManager() {
        return this.conditionManager;
    }

    public ArmorConditions getModelProcessor() {
        return this.modelProcessor;
    }

    public Object2ReferenceMap<String, AnimationController> getAnimationEntries() { // 动画控制器
        return this.animationEntries;
    }

    public OrderedStringMap<String, ? extends AbstractTexture> getTextures() {
        return this.textures;
    }

    public String getDefaultTextureName() {
        return this.defaultTextureName;
    }

    public AbstractTexture getDefaultTexture() {
        return this.defaultTexture;
    }

    public Consumer<CustomPlayerEntity> getPlayerControllerInstaller() {
        return this.playerControllerInstaller;
    }

    public Consumer<PlayerGeoEntity> getArmControllerInstaller() {
        return this.armControllerInstaller;
    }

    public Object getMaidControllerInstaller() {
        return this.maidControllerInstaller;
    }
}