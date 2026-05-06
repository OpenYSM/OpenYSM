package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.VehicleCapabilityProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

public class CustomVehicleRenderer {
    public static boolean renderVehicle(Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        return entity.getCapability(VehicleCapabilityProvider.VEHICLE_CAP).map(cap -> {
            if (cap.isModelInitialized() && cap.isModelReady()) {
                RendererManager.getVehicleRenderer().renderEntity(cap, getBodyRotation(entity, entityYaw, partialTick), partialTick, poseStack, bufferSource, packedLight);
                return false;
            }
            return true;
        }).orElse(true);
    }

    public static float getBodyRotation(Entity entity, float entityYaw, float partialTick) {
        float bodyRotation = entityYaw;
        if (entity instanceof LivingEntity) {
            bodyRotation = getLivingBodyRotation((LivingEntity) entity, partialTick);
        } else if (entity instanceof AbstractMinecart) {
            bodyRotation = getMinecartBodyRotation((AbstractMinecart) entity, partialTick, bodyRotation);
        }
        return bodyRotation;
    }

    private static float getLivingBodyRotation(LivingEntity entity, float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);

        if (entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit()) {
            Entity vehicle = entity.getVehicle();
            if (vehicle instanceof LivingEntity livingVehicle) {
                float vehicleBodyYaw = Mth.rotLerp(partialTick, livingVehicle.yBodyRotO, livingVehicle.yBodyRot);
                float yawDiff = Mth.clamp(Mth.wrapDegrees(headYaw - vehicleBodyYaw), -85.0f, 85.0f);
                bodyYaw = headYaw - yawDiff;

                if (yawDiff * yawDiff > 2500.0f) {
                    bodyYaw += yawDiff * 0.2f;
                }
            }
        }
        return bodyYaw;
    }

    private static float getMinecartBodyRotation(AbstractMinecart minecart, float partialTick, float defaultYaw) {
        double interpX = Mth.lerp(partialTick, minecart.xOld, minecart.getX());
        double interpY = Mth.lerp(partialTick, minecart.yOld, minecart.getY());
        double interpZ = Mth.lerp(partialTick, minecart.zOld, minecart.getZ());
        Vec3 interpPos = minecart.getPos(interpX, interpY, interpZ);

        float calculatedYaw = defaultYaw;

        if (interpPos != null) {
            Vec3 frontOffsetPos = minecart.getPosOffs(interpX, interpY, interpZ, 0.30000001192092896d);
            Vec3 backOffsetPos = minecart.getPosOffs(interpX, interpY, interpZ, -0.30000001192092896d);

            if (frontOffsetPos == null) {
                frontOffsetPos = interpPos;
            }
            if (backOffsetPos == null) {
                backOffsetPos = interpPos;
            }

            Vec3 directionVec = backOffsetPos.add(-frontOffsetPos.x, -frontOffsetPos.y, -frontOffsetPos.z);
            if (directionVec.length() != 0.0d) {
                calculatedYaw = (float) ((Math.atan2(directionVec.z, directionVec.x) * 180.0d) / Math.PI);
            }
        }
        return calculatedYaw;
    }
}