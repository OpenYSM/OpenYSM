package com.elfmcys.yesstevemodel.geckolib3.file;

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationControllerFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;

public class VehicleModelFiles {

    private final String[] textureNames;

    private final GeoModel model;

    private final AnimationFile animations;

    private final AnimationControllerFile animationController;

    private final OuterFileTexture texture;

    public VehicleModelFiles(String[] textureNames, GeoModel model, AnimationFile animations, AnimationControllerFile animationController, OuterFileTexture texture) {
        this.textureNames = textureNames;
        this.model = model;
        this.animations = animations;
        this.animationController = animationController;
        this.texture = texture;
    }

    public String[] getTextureNames() {
        return this.textureNames;
    }

    public GeoModel getModel() {
        return this.model;
    }

    public AnimationFile getAnimations() {
        return this.animations;
    }

    public AnimationControllerFile getAnimationController() {
        return this.animationController;
    }

    public OuterFileTexture getTexture() {
        return this.texture;
    }
}
