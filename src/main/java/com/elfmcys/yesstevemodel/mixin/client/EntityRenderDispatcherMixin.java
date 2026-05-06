package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.renderer.CustomFishingHookRenderer;
import com.elfmcys.yesstevemodel.client.renderer.CustomVehicleRenderer;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.client.renderer.CustomProjectileRenderer;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({EntityRenderDispatcher.class})
public class EntityRenderDispatcherMixin {
    @WrapWithCondition(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")})
    private boolean render(EntityRenderer<?> renderer, Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (!YesSteveModel.isAvailable()) {
            return true;
        }
        if (entity instanceof Projectile projectile) {
            if (!GeneralConfig.DISABLE_PROJECTILE_MODEL.get()) {
                if (projectile instanceof FishingHook fishingHook) {
                    return CustomFishingHookRenderer.tryRenderCustomHook(fishingHook, entityYaw, partialTicks, poseStack, multiBufferSource, packedLight);
                }
                return CustomProjectileRenderer.renderProjectile(projectile, entityYaw, partialTicks, poseStack, multiBufferSource, packedLight);
            }
        }
        if (!GeneralConfig.DISABLE_VEHICLE_MODEL.get().booleanValue()) {
            ModelPreviewRenderer.renderVehicleModel(entity, poseStack, partialTicks);
            return CustomVehicleRenderer.renderVehicle(entity, entityYaw, partialTicks, poseStack, multiBufferSource, packedLight);
        }
        return true;
    }
}