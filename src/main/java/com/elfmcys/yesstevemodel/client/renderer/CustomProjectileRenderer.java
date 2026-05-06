package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.ProjectileCapabilityProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.projectile.Projectile;

public class CustomProjectileRenderer {
    public static boolean renderProjectile(Projectile projectile, float entityYaw, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        return projectile.getCapability(ProjectileCapabilityProvider.PROJECTILE_CAP).map(cap -> {
            if (cap.isModelInitialized() && cap.isModelReady()) {
                RendererManager.getProjectileRenderer().render(cap, entityYaw, f2, poseStack, multiBufferSource, i);
                return false;
            }
            return true;
        }).orElse(true);
    }
}