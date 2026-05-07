package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.geckolib3.geo.NativeModelRenderer;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.renderer.CustomEntityTranslucentRenderType;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class RenderFirstPlayerBackground {
    // 因为RenderHandEvent可有几率会渲染多次，所以为了避免多次渲染，这样设计
    private static boolean currentFrameRendered = false;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (YesSteveModel.isAvailable() && event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            currentFrameRendered = false;
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        if (GeneralConfig.DISABLE_SELF_MODEL.get()) {
            return;
        }
        if (GeneralConfig.DISABLE_SELF_HANDS.get()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || currentFrameRendered) {
            return;
        }
        currentFrameRendered = true;
        player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
            if (!cap.isModelActive()) {
                return;
            }
            String modelId = cap.getModelId();
            ModelAssembly modelAssembly = cap.getModelAssembly();
            if (modelAssembly == null || !modelAssembly.getAnimationBundle().getArmModel().hasCustomLimbs) {
                return;
            }
            CustomPlayerRenderer instance = RendererManager.getPlayerRenderer();
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource multiBufferSource = event.getMultiBufferSource();
            if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(player, cap,  modelId))) {
                return;
            }
            ResourceLocation resourceLocationB_ = cap.getTextureLocation();
            int textureIndex = cap.getTextureIndex();
            VertexConsumer buffer = multiBufferSource.getBuffer(CustomEntityTranslucentRenderType.get(resourceLocationB_));
            if (instance != null) {
                poseStack.pushPose();
                if (Minecraft.getInstance().options.bobView().get()) {
                    applyHandTransform(poseStack, event.getPartialTick(), player);
                }
                poseStack.translate(0.0d, -1.5d, 0.0d);
                NativeModelRenderer.renderMesh(buffer, poseStack.last(), modelAssembly.getAnimationBundle().getArmModel(), modelAssembly.getAnimationBundle().getArmModel().getBoneTransformData(), null, textureIndex, 3, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
                poseStack.popPose();
            }
        });
    }

    private static void applyHandTransform(PoseStack poseStack, float partialTick, Player player) {
        float walkPhase = -(player.walkDist + ((player.walkDist - player.walkDistO) * partialTick));
        float fLerp = Mth.lerp(partialTick, player.oBob, player.bob);
        poseStack.translate((-Mth.sin(walkPhase * 3.1415927f)) * fLerp * 0.5f, Math.abs(Mth.cos(walkPhase * 3.1415927f) * fLerp), 0.0d);
        poseStack.mulPose(com.mojang.math.Axis.ZN.rotationDegrees(Mth.sin(walkPhase * 3.1415927f) * fLerp * 3.0f));
        poseStack.mulPose(com.mojang.math.Axis.XN.rotationDegrees(Math.abs(Mth.cos((walkPhase * 3.1415927f) - 0.2f) * fLerp) * 5.0f));
    }
}