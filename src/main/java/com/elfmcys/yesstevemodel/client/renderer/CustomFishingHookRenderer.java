package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.ProjectileCapabilityProvider;
import com.elfmcys.yesstevemodel.client.compat.oculus.OculusCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Unique;

public class CustomFishingHookRenderer {
    public static boolean tryRenderCustomHook(FishingHook fishingHook, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        return fishingHook.getCapability(ProjectileCapabilityProvider.PROJECTILE_CAP).map(cap -> {
            if (cap.isModelInitialized() && cap.isModelReady()) {
                fishingHook.setXRot(0.0f);
                fishingHook.xRotO = 0.0f;
                RendererManager.getProjectileRenderer().render(cap, entityYaw, partialTick, poseStack, bufferSource, packedLight);
                Player playerOwner = fishingHook.getPlayerOwner();
                if (playerOwner != null) {
                    poseStack.pushPose();
                    renderFishingLine(fishingHook, partialTick, poseStack, bufferSource, playerOwner);
                    poseStack.popPose();
                }
                return false;
            }
            return true;
        }).orElse(true);
    }

    private static void renderFishingLine(FishingHook fishingHook, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, Player player) {
        int hand = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        if (!player.getMainHandItem().canPerformAction(ToolActions.FISHING_ROD_CAST)) {
            hand = -hand;
        }
        float swingProgressSqrt = Mth.sin(Mth.sqrt(player.getAttackAnim(partialTick)) * 3.1415927f);
        float yawOffset = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * 0.017453292f;
        double dSin = Mth.sin(yawOffset);
        double dCos = Mth.cos(yawOffset);
        double handOffset = hand * 0.35d;
        double anglerX;
        double anglerY;
        double anglerZ;
        float anglerEye;
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Options options = entityRenderDispatcher.options;
        if (options == null || !options.getCameraType().isFirstPerson() || player != Minecraft.getInstance().player) {
            anglerX = (Mth.lerp(partialTick, player.xo, player.getX()) - (dCos * handOffset)) - (dSin * 0.8d);
            anglerY = ((player.yo + player.getEyeHeight()) + ((player.getY() - player.yo) * partialTick)) - 0.45d;
            anglerZ = (Mth.lerp(partialTick, player.zo, player.getZ()) - (dSin * handOffset)) + (dCos * 0.8d);
            anglerEye = player.isCrouching() ? -0.1875f : 0.0f;
        } else {
            Vec3 vec3XRot = entityRenderDispatcher.camera.getNearPlane().getPointOnPlane(hand * 0.525f, -0.1f).scale(960.0d / options.fov().get().intValue()).yRot(swingProgressSqrt * 0.5f).xRot((-swingProgressSqrt) * 0.7f);
            anglerX = Mth.lerp(partialTick, player.xo, player.getX()) + vec3XRot.x;
            anglerY = Mth.lerp(partialTick, player.yo, player.getY()) + vec3XRot.y;
            anglerZ = Mth.lerp(partialTick, player.zo, player.getZ()) + vec3XRot.z;
            anglerEye = player.getEyeHeight();
        }
        float startX = (float) (anglerX - Mth.lerp(partialTick, fishingHook.xo, fishingHook.getX()));
        float startY = ((float) (anglerY - (Mth.lerp(partialTick, fishingHook.yo, fishingHook.getY()) + 0.25d))) + anglerEye;
        float startZ = (float) (anglerZ - Mth.lerp(partialTick, fishingHook.zo, fishingHook.getZ()));
        float[] color = lineColor(fishingHook);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lineStrip());
        PoseStack.Pose poseLast = poseStack.last();
        for (int size = 0; size <= 16; size++) {
            stringVertex(startX, startY, startZ, buffer, poseLast, fraction(size), fraction(size + 1), color[0], color[1], color[2]);
        }
        if (OculusCompat.isLoaded()) {
            buffer.vertex(0.0d, 0.0d, 0.0d).color(0, 0, 0, 255).normal(0.0f, 0.0f, 0.0f).endVertex();
        }
    }

    @Unique
    private static float[] lineColor(FishingHook fishingHook) {
        return new float[]{0.0f, 0.0f, 0.0f};
    }

    @Unique
    private static float fraction(int i) {
        return i / 16.0f;
    }

    @Unique
    private static void stringVertex(float x, float y, float z, VertexConsumer vertexConsumer, PoseStack.Pose pose, float f4, float f5, float f6, float f7, float f8) {
        float f9 = x * f4;
        float f10 = (y * ((f4 * f4) + f4) * 0.5f) + 0.25f;
        float f11 = z * f4;
        float f12 = (x * f5) - f9;
        float f13 = (((y * ((f5 * f5) + f5)) * 0.5f) + 0.25f) - f10;
        float f14 = (z * f5) - f11;
        float fSqrt = Mth.sqrt((f12 * f12) + (f13 * f13) + (f14 * f14));
        vertexConsumer.vertex(pose.pose(), f9, f10, f11).color(f6, f7, f8, 1.0f).normal(pose.normal(), f12 / fSqrt, f13 / fSqrt, f14 / fSqrt).endVertex();
    }
}