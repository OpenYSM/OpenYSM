package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationControllerFile;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

public class MainModelData {

    private final List<GeoModel> models;

    private final Map<String, AnimationFile> animations;

    private final List<AnimationControllerFile> animationControllers;

    private final OrderedStringMap<String, OuterFileTexture> textureMap;

    public MainModelData(GeoModel[] models, Map<String, AnimationFile> animations, AnimationControllerFile[] animationControllerFiles, OrderedStringMap<String, OuterFileTexture> textureMap) {
        this.models = ObjectArrayList.wrap(models);
        this.animations = animations;
        this.animationControllers = ObjectArrayList.wrap(animationControllerFiles);
        this.textureMap = textureMap;
    }

    public Map<String, AnimationFile> getAnimations() {
        return this.animations;
    }

    public List<GeoModel> getModels() {
        return this.models;
    }

    public List<AnimationControllerFile> getAnimationControllers() {
        return this.animationControllers;
    }

    public OrderedStringMap<String, OuterFileTexture> getTextureMap() {
        return this.textureMap;
    }
}