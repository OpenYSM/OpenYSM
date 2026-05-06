package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.geckolib3.geo.NativeModelRenderer;
import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.LayerTypeConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.common.MinecraftForge;

public class HandItemRenderer {

    private PlayerGeoEntity geoModel = null;

    public void renderHandItem(LocalPlayer localPlayer, ModelAssembly modelAssembly, PlayerCapability capability, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        AnimatedGeoModel model;
        if (this.geoModel == null || this.geoModel.getEntity() != localPlayer) {
            this.geoModel = new PlayerGeoEntity(localPlayer, capability);
        }
        this.geoModel.tickModel();
        if (this.geoModel.processAnimation(partialTick) == null || (model = this.geoModel.getCurrentModel()) == null) {
            return;
        }
        SpecialPlayerRenderEvent event = new SpecialPlayerRenderEvent(localPlayer, capability, capability.getModelId());
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        ResourceLocation resourceLocation = event.getTextureLocation() == null ? capability.getTextureLocation() : event.getTextureLocation();
        int textureIndex = event.getTextureLocation() == null ? capability.getTextureIndex() : 0;
        VertexConsumer buffer = bufferSource.getBuffer(CustomEntityTranslucentRenderType.get(resourceLocation));
        int renderPartMask = arm == HumanoidArm.LEFT ? LayerTypeConstants.TYPE_LEFT : LayerTypeConstants.TYPE_RIGHT;
        poseStack.pushPose();
        if (arm == HumanoidArm.LEFT) {
            poseStack.translate(0.25d, 1.8d, 0.0d);
        } else {
            poseStack.translate(-0.25d, 1.8d, 0.0d);
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        NativeModelRenderer.renderMesh(buffer, poseStack.last(), model.getGeoModel(), model.getMatrixData(), model.getAbsPivotData(), textureIndex, renderPartMask, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}