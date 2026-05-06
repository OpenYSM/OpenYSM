package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability;

import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.MaidFrameState;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.molang.runtime.Struct;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.ILocationModel;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class MaidCapability extends LivingAnimatable<EntityMaid> implements IGeoEntity {

    private MaidModelInfo maidModelInfo;

    public MaidCapability(EntityMaid entityMaid, boolean z) {
        super(entityMaid, z);
        this.maidModelInfo = new MaidModelInfo();
    }

    @Override
    public void registerAnimationControllers() {
        ((Consumer) getModelAssembly().getAnimationBundle().getMaidControllerInstaller()).accept(this);
    }

    @Override
    @NotNull
    public GeoEntity.ModelWrapper buildRenderShape(ModelAssembly modelAssembly, boolean z) {
        return new TexturedModelWrapper(modelAssembly, z, true, true, 600);
    }

    @Override
    public MaidFrameState createPositionTracker(EntityMaid entityMaid) {
        return new MaidFrameState(entityMaid);
    }

    @Override
    public MaidFrameState getPositionTracker() {
        return (MaidFrameState) super.getPositionTracker();
    }

    public boolean hasModel() {
        return this.entity.rouletteAnimDirty;
    }

    public void refreshModel() {
        this.entity.rouletteAnimDirty = false;
    }

    public boolean isModelAvailable() {
        return this.entity.rouletteAnimPlaying;
    }

    public String getModelTextureId() {
        return this.entity.rouletteAnim;
    }

    public void setMolangVars(Object2FloatOpenHashMap<String> object2FloatOpenHashMap) {
    }

    public void updateRoamingVars(Object2FloatOpenHashMap<String> object2FloatOpenHashMap) {
    }

    public Struct getPropertyContainer() {
        return null;
    }

    @Override
    public void setupAnim(float seekTime, boolean z) {
        super.setupAnim(seekTime, z);
        getEvaluationContext().setRoamingProperties(getPropertyContainer());
    }

    public IMaid getMaid() {
        return this.entity;
    }

    public MaidModelInfo getMaidInfo() {
        return this.maidModelInfo;
    }

    public void setMaidInfo(MaidModelInfo maidModelInfo) {
        if (this.maidModelInfo != maidModelInfo) {
            this.maidModelInfo = maidModelInfo;
        }
    }

    public ILocationModel getGeoModel() {
        return getCurrentModel().getTouhouMaidData();
    }

    public void setYsmModel(String str, String str2) {
        initModelWithTexture(str, str2);
    }
}