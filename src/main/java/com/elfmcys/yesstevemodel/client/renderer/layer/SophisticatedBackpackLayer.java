package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.compat.sbackpack.SBackpackCompat;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackLayerRenderer;

public class SophisticatedBackpackLayer extends GeoLayerRenderer<CustomPlayerEntity> {

    private final EntityModel<Player> backpackModel = createBackpackModel();

    public SophisticatedBackpackLayer() {
    }

    private static EntityModel<Player> createBackpackModel() {
        return new EntityModel<>() {
            public void setupAnim(Player player, float f, float f2, float f3, float f4, float f5) {
            }

            public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i2, float f, float f2, float f3, float f4) {
            }
        };
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn, CustomPlayerEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        Player player;
        ItemStack stack;
        AnimatedGeoModel model = entityLivingBaseIn.getCurrentModel();
        if (model != null && !model.backpackBones().isEmpty() && (stack = SBackpackCompat.getBackpackItem((player = entityLivingBaseIn.getEntity()))) != null) {
            poseStack.pushPose();
            renderBackpack(poseStack, model);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0f));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0f));
            poseStack.translate(0.0d, -0.1d, 0.0d);
            BackpackLayerRenderer.renderBackpack(this.backpackModel, player, poseStack, bufferSource, packedLightIn, stack, false);
            poseStack.popPose();
        }
    }

    public void renderBackpack(PoseStack poseStack, AnimatedGeoModel model) {
        RenderUtils.prepMatrixForLocator(poseStack, model.backpackBones());
    }
}