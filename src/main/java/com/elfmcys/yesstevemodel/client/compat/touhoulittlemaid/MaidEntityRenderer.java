package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.capability.VehicleCapabilityProvider;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapability;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapabilityProvider;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.GeoLayerRenderer;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntityRenderer;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class MaidEntityRenderer extends GeoReplacedEntityRenderer<EntityMaid, MaidCapability> implements IGeoEntityRenderer<EntityMaid> {
    private static final Set<EntityType> CUSTOM_RIDERS = Set.of(EntityType.MINECART, EntityType.BOAT, InitEntities.BROOM.get());
    public final List<GeoLayerRenderer<EntityMaid, MaidEntityRenderer>> maidLayerRenderer;

    public MaidEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.maidLayerRenderer = new ObjectArrayList<>();
    }

    public MaidCapability getMaidCapability(EntityMaid maid) {
        return maid.getCapability(MaidCapabilityProvider.MAID_CAP).map(cap -> cap).orElseGet(() -> new MaidCapability(maid, true));
    }

    public IGeoEntity getGeoEntity(EntityMaid maid) {
        return getMaidCapability(maid);
    }

    @Override
    public void addGeoLayerRenderer(GeoLayerRenderer<?, ?> geoLayerRenderer) {
        this.maidLayerRenderer.add((GeoLayerRenderer<EntityMaid, MaidEntityRenderer>) geoLayerRenderer);
    }

    public void geoRender(EntityMaid entityMaid, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        entityMaid.getCapability(MaidCapabilityProvider.MAID_CAP).ifPresent(cap -> renderEntity(cap, entityYaw, partialTick, poseStack, bufferSource, packedLight));
    }

    @NotNull
    public ResourceLocation getTextureLocation(EntityMaid maid) {
        return maid.getCapability(MaidCapabilityProvider.MAID_CAP).map((cap) -> cap.getTextureLocation()).orElse(MissingTextureAtlasSprite.getLocation());
    }

    public void render(MaidCapability entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn, AnimationEvent<?> event, EntityModelData modelData) {
        for (GeoLayerRenderer<EntityMaid, MaidEntityRenderer> entityMaidMaidEntityRendererGeoLayerRenderer : this.maidLayerRenderer) {
            entityMaidMaidEntityRendererGeoLayerRenderer.render(poseStack, bufferSource, packedLightIn, entity.getEntity(), event.getLimbSwing(), event.getLimbSwingAmount(), partialTick, modelData.lerpedAge, modelData.netHeadYaw, modelData.headPitch);
        }
    }

    @Override
    public void setupRotations(EntityMaid maid, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(maid, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (maid.isMaidInSittingPose()) {
            poseStack.translate(0.0d, -0.5d, 0.0d);
            return;
        }

        Entity entity = maid.getVehicle();
        if (entity instanceof Player) {
            poseStack.translate(-0.05d, 0.19d, 0.24d);
        } else if (entity != null && CUSTOM_RIDERS.contains(entity.getType()) && !entity.getCapability(VehicleCapabilityProvider.VEHICLE_CAP).isPresent()) {
            poseStack.translate(0.0d, -0.5d, 0.0d);
        }
    }
}