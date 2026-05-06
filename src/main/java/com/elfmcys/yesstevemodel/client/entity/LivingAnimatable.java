package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.model.VehicleModelBundle;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.client.animation.molang.MolangEventDispatcher;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.model.ProjectileModelBundle;
import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public abstract class LivingAnimatable<T extends LivingEntity> extends GeoEntity<T> {

    public String currentTextureName;

    private int textureIndex;

    private final Vector2f armorBoneOffset;

    private boolean needsInit;

    private IValue playerUpdateIValue;

    private final BooleanList updateExpressionArgs;

    private boolean forceDisabled;

    private boolean extraRenderFlag;

    public LivingAnimatable(T t, boolean z) {
        super(t, z);
        this.armorBoneOffset = new Vector2f();
        this.needsInit = false;
        this.playerUpdateIValue = null;
        this.updateExpressionArgs = new BooleanArrayList(1);
        this.forceDisabled = false;
        this.extraRenderFlag = false;
        this.updateExpressionArgs.size(1);
    }

    @Override
    public void applyHeadTracking(AnimationEvent<? extends AnimatableEntity<T>> event, boolean z) {
        AnimatedGeoModel model = getCurrentModel();
        if (model != null && !model.headBones().isEmpty()) {
            IBone bone = model.headBones().get(model.headBones().size() - 1);
            if (z) {
                this.armorBoneOffset.set(bone.getRotationX(), bone.getRotationY());
            }
            EntityModelData data = event.getModelData();
            bone.setRotationX(this.armorBoneOffset.x + ((float) Math.toRadians(data.headPitch)));
            bone.setRotationY(this.armorBoneOffset.y + ((float) Math.toRadians(data.netHeadYaw)));
        }
    }

    @Override
    public void resetHeadTracking(boolean wasAnimEvaluated) {
        AnimatedGeoModel model = getCurrentModel();
        if (model != null && !model.headBones().isEmpty()) {
            IBone bone = model.headBones().get(model.headBones().size() - 1);
            bone.setRotationX(this.armorBoneOffset.x);
            bone.setRotationY(this.armorBoneOffset.y);
        }
    }

    @Override
    public LivingEntityFrameState<T> createPositionTracker(T t) {
        return new LivingEntityFrameState<>(t);
    }

    @Override
    public LivingEntityFrameState<T> getPositionTracker() {
        return (LivingEntityFrameState) super.getPositionTracker();
    }

    public void setCurrentTexture(String str) {
        this.currentTextureName = str;
        updateCurrentTexture();
    }

    public void initModelWithTexture(String str, String str2) {
        markModelInitialized();
        this.currentTextureName = str2;
        setModelId(str);
        updateCurrentTexture();
    }

    public void setForceDisabled(boolean z) {
        this.forceDisabled = z;
    }

    public boolean isForceDisabled() {
        return this.forceDisabled;
    }

    public boolean isModelActive() {
        return isModelInitialized() && !this.forceDisabled;
    }

    @Override
    public void onModelLoaded(ModelAssembly context) {
        super.onModelLoaded(context);
        updateCurrentTexture();
        List<IValue> values = context.getExpressionCache().getEvents().get(MolangEventDispatcher.PLAYER_UPDATE);
        if (values != null) {
            this.playerUpdateIValue = MolangEventDispatcher.createUpdateExpression(values, this.updateExpressionArgs);
        } else {
            this.playerUpdateIValue = null;
        }
    }

    @Override
    public void setCurrentModel(AnimatedGeoModel model) {
        super.setCurrentModel(model);
        if (model != null && !model.headBones().isEmpty()) {
            IBone bone = model.headBones().get(model.headBones().size() - 1);
            this.armorBoneOffset.set(bone.getRotationX(), bone.getRotationY());
        }
    }

    @Override
    public void resetModel() {
        super.resetModel();
        this.currentTextureName = null;
        this.textureIndex = 0;
        this.forceDisabled = false;
    }

    @Override
    public void reset() {
        super.reset();
        this.armorBoneOffset.set(0.0f);
        this.extraRenderFlag = false;
        this.needsInit = true;
    }

    @Override
    public void setupAnim(float seekTime, boolean z) {
        super.setupAnim(seekTime, z);
        if (this.needsInit) {
            this.needsInit = false;
            List<IValue> values = getAnimationExpressions(MolangEventDispatcher.PLAYER_INIT);
            if (values != null) {
                executeExpression(MolangEventDispatcher.createInitExpression(values), true, true, null);
            }
        }
        if (this.playerUpdateIValue != null) {
            this.updateExpressionArgs.set(0, z);
            executeExpression(this.playerUpdateIValue, true, true, null);
        }
    }

    public ConditionManager getModelConfig() {
        return getModelAssembly().getAnimationBundle().getConditionManager();
    }

    private void updateCurrentTexture() {
        if (isModelReady()) {
            OrderedStringMap<String, ? extends AbstractTexture> map = getModelAssembly().getAnimationBundle().getTextures();
            AbstractTexture abstractTexture = map.get(this.currentTextureName);
            if (abstractTexture != null) {
                ((TexturedModelWrapper) getRenderShape()).setTexture(abstractTexture);
                this.textureIndex = map.getValuesList().indexOf(abstractTexture);
            } else {
                this.currentTextureName = map.getKeyAt(0);
                ((TexturedModelWrapper) getRenderShape()).setTexture(map.getValueAt(0));
                this.textureIndex = 0;
            }
        }
    }

    @Override
    public GeoModel getAnimationProcessor() {
        return getModelAssembly().getAnimationBundle().getMainModel();
    }

    @Override
    @Nullable
    public Animation getAnimation(String str) {
        return getModelAssembly().getAnimationBundle().getMainAnimations().get(str);
    }

    @Override
    @Nullable
    public AnimationController getAnimationEntries(String str) {
        return getModelAssembly().getAnimationBundle().getAnimationEntries().get(str);
    }

    public String getCurrentTextureName() {
        return isModelReady() ? this.currentTextureName : getModelAssembly().getAnimationBundle().getTextures().getKeyAt(0);
    }

    @Override
    @NotNull
    public ResourceLocation getTextureLocation() {
        return isModelReady() ? ((TexturedModelWrapper) getRenderShape()).currentTexture.getResourceLocation().get() : ClientModelManager.getDefaultTexture();
    }

    @Override
    public int getTextureIndex() {
        if (isModelReady()) {
            return this.textureIndex;
        }
        return 0;
    }

    @Override
    public float getWidthScale() {
        return getModelAssembly().getModelData().getModelProperties().getWidthScale();
    }

    @Override
    public float getHeightScale() {
        return getModelAssembly().getModelData().getModelProperties().getHeightScale();
    }

    public boolean isRenderLayersFirst() {
        return getModelAssembly().getModelData().getModelProperties().isRenderLayersFirst();
    }

    public boolean isExtraRenderFlag() {
        return this.extraRenderFlag;
    }

    public void setExtraRenderFlag(boolean z) {
        this.extraRenderFlag = z;
    }

    public class TexturedModelWrapper extends ModelWrapper {

        public IResourceLocatable currentTexture;

        private final List<IResourceLocatable> allTextures;

        private final int textureResolution;

        public TexturedModelWrapper(ModelAssembly modelAssembly, boolean isActive, boolean collectAllTextures, boolean registerImmediately, int textureResolution) {
            super(modelAssembly, isActive);
            AbstractTexture abstractTexture = modelAssembly.getAnimationBundle().getTextures().get(LivingAnimatable.this.currentTextureName);
            this.currentTexture = UploadManager.getOrCreateLocatableWithSize(abstractTexture != null ? abstractTexture : modelAssembly.getAnimationBundle().getDefaultTexture(), registerImmediately, textureResolution);
            this.textureResolution = textureResolution;
            if (collectAllTextures) {
                this.allTextures = new ArrayList();
                for (AbstractTexture texture : modelAssembly.getAnimationBundle().getTextures().values()) {
                    this.allTextures.add(UploadManager.getOrCreateLocatable(texture, false));
                }
                for (ProjectileModelBundle projectileModelBundle : modelAssembly.getProjectileModels().values()) {
                    this.allTextures.add(UploadManager.getOrCreateLocatable(projectileModelBundle.getTexture(), false));
                }
                for (VehicleModelBundle vehicleModelBundle : modelAssembly.getVehicleModels().values()) {
                    this.allTextures.add(UploadManager.getOrCreateLocatable(vehicleModelBundle.getTexture(), false));
                }
                return;
            }
            this.allTextures = null;
        }

        private void setTexture(AbstractTexture abstractTexture) {
            this.currentTexture = UploadManager.getOrCreateLocatableWithSize(abstractTexture, true, this.textureResolution);
        }

        @Override
        public boolean isValid() {
            return this.currentTexture.getResourceLocation().isPresent();
        }
    }
}