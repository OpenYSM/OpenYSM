package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.VehicleCapabilityProvider;
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class VehicleRenderer extends GeoEntityRenderer<Entity, GeckoVehicleEntity> {
    public VehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (Minecraft.getInstance().player == null || entity.isInvisibleTo(Minecraft.getInstance().player)) {
            return;
        }
        entity.getCapability(VehicleCapabilityProvider.VEHICLE_CAP).ifPresent(cap -> {
            cap.tickModel();
            renderEntity(cap, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        });
    }

    @NotNull
    public ResourceLocation getTextureLocation(Entity entity) {
        return entity.getCapability(VehicleCapabilityProvider.VEHICLE_CAP).map((cap) -> cap.getTextureLocation()).orElse(MissingTextureAtlasSprite.getLocation());
    }
}