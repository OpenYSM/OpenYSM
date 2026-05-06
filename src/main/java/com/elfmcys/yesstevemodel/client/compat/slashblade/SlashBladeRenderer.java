package com.elfmcys.yesstevemodel.client.compat.slashblade;

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SlashBladeRenderer {

    private static final ResourceLocation BLADE_OBJ = ResourceLocation.fromNamespaceAndPath("slashblade", "model/blade.obj");

    private static final ResourceLocation BLADE_TEXTURE = ResourceLocation.fromNamespaceAndPath("slashblade", "model/blade.png");

    public static void renderBladeOnly(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        stack.getCapability(CapabilitySlashBlade.BLADESTATE).ifPresent(bladeState -> {
            String str;
            ResourceLocation resourceLocation = bladeState.getTexture().orElse(BLADE_TEXTURE);
            WavefrontObject model = BladeModelManager.getInstance().getModel(bladeState.getModel().orElse(BLADE_OBJ));

            if (bladeState.isBroken()) {
                str = "blade_damaged";
            } else {
                str = "blade";
            }

            BladeRenderState.renderOverrided(stack, model, str, resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverridedLuminous(stack, model, str + "_luminous", resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverrided(stack, model, "sheath", resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverridedLuminous(stack, model, "sheath_luminous", resourceLocation, poseStack, bufferSource, packedLight);
        });
    }

    public static void renderOnEntity(LivingEntity entity, AnimatedGeoModel model, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack, float partialTick) {
        if (SlashBladeCompat.isSlashBladeItem(stack)) {
            List<IBone> leftWaistBones = model.leftWaistBones();
            List<IBone> bladeBones = model.bladeBones();
            List<IBone> sheathBones = model.sheathBones();

            if (bladeBones.isEmpty() || sheathBones.isEmpty() || leftWaistBones.isEmpty()) {
                renderBladeOnWaist(entity, model, poseStack, bufferSource, packedLight, stack, partialTick, leftWaistBones);
            } else {
                stack.getCapability(CapabilitySlashBlade.BLADESTATE).ifPresent(bladeState -> renderBladeWithBones(bladeState, poseStack, bufferSource, packedLight, stack, leftWaistBones, bladeBones, sheathBones));
            }
        }
    }

    public static void renderBladeWithBones(ISlashBladeState bladeState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack, List<IBone> leftWaistBones, List<IBone> bladeBones, List<IBone> sheathBones) {
        String partName;
        ResourceLocation resourceLocation = bladeState.getTexture().orElse(BLADE_TEXTURE);
        WavefrontObject model = BladeModelManager.getInstance().getModel(bladeState.getModel().orElse(BLADE_OBJ));

        if (bladeState.isBroken()) {
            partName = "blade_damaged";
        } else {
            partName = "blade";
        }

        IBone lastLeftWaistBone = leftWaistBones.get(leftWaistBones.size() - 1);
        if (lastLeftWaistBone.getScaleX() != 0.0f || lastLeftWaistBone.getScaleY() != 0.0f || lastLeftWaistBone.getScaleZ() != 0.0f) {
            poseStack.pushPose();
            RenderUtils.prepMatrixForLocator(poseStack, leftWaistBones);
            poseStack.translate(0.0d, 0.025d, -0.6d);
            poseStack.scale(0.01f, 0.01f, 0.01f);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0f));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0f));
            BladeRenderState.renderOverrided(stack, model, partName, resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverridedLuminous(stack, model, partName + "_luminous", resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverrided(stack, model, "sheath", resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverridedLuminous(stack, model, "sheath_luminous", resourceLocation, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }

        IBone lastBladeBone = bladeBones.get(bladeBones.size() - 1);
        if (lastBladeBone.getScaleX() != 0.0f || lastBladeBone.getScaleY() != 0.0f || lastBladeBone.getScaleZ() != 0.0f) {
            poseStack.pushPose();
            RenderUtils.prepMatrixForLocator(poseStack, bladeBones);
            poseStack.translate(0.0d, 0.035d, 0.0d);
            poseStack.scale(0.01f, 0.01f, 0.01f);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0f));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0f));
            BladeRenderState.renderOverrided(stack, model, partName, resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverridedLuminous(stack, model, partName + "_luminous", resourceLocation, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }

        IBone lastSheathBone = sheathBones.get(sheathBones.size() - 1);
        if (lastSheathBone.getScaleX() != 0.0f || lastSheathBone.getScaleY() != 0.0f || lastSheathBone.getScaleZ() != 0.0f) {
            poseStack.pushPose();
            RenderUtils.prepMatrixForLocator(poseStack, sheathBones);
            poseStack.translate(0.0d, 0.025d, -0.6d);
            poseStack.scale(0.01f, 0.01f, 0.01f);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0f));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0f));
            BladeRenderState.renderOverrided(stack, model, "sheath", resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverridedLuminous(stack, model, "sheath_luminous", resourceLocation, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }
    }

    private static void renderBladeOnWaist(LivingEntity entity, AnimatedGeoModel model1, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack, float partialTick, List<IBone> leftWaistBones) {
        poseStack.pushPose();
        if (!leftWaistBones.isEmpty()) {
            applyWaistBoneTransform(HumanoidArm.LEFT, poseStack, model1);
        } else {
            poseStack.translate(-0.25d, 1.25d, 0.0d);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(20.0f));
        }

        poseStack.translate(0.0d, 0.0d, -0.7d);
        poseStack.scale(0.01f, 0.01f, 0.01f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0f));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0f));

        if (stack.isEmpty()) {
            return;
        }

        stack.getCapability(CapabilitySlashBlade.BLADESTATE).ifPresent(iSlashBladeState -> {
            String partName;
            ResourceLocation resourceLocation = iSlashBladeState.getTexture().orElse(BLADE_TEXTURE);
            WavefrontObject model = BladeModelManager.getInstance().getModel(iSlashBladeState.getModel().orElse(BLADE_OBJ));

            if (iSlashBladeState.isBroken()) {
                partName = "blade_damaged";
            } else {
                partName = "blade";
            }

            BladeRenderState.renderOverrided(stack, model, "sheath", resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverridedLuminous(stack, model, "sheath_luminous", resourceLocation, poseStack, bufferSource, packedLight);

            long timeSinceLastAction = entity.level().getGameTime() - iSlashBladeState.getLastActionTime();
            if (timeSinceLastAction < 5) {
                poseStack.translate(0.0d, 0.0d, -71.42857142857143d);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(60.0f + ((timeSinceLastAction + partialTick) * 48.0f)));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
            }

            BladeRenderState.renderOverrided(stack, model, partName, resourceLocation, poseStack, bufferSource, packedLight);
            BladeRenderState.renderOverridedLuminous(stack, model, partName + "_luminous", resourceLocation, poseStack, bufferSource, packedLight);
        });
        poseStack.popPose();
    }

    public static void renderRightWaist(AnimatedGeoModel model, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ItemStack stack) {
        if (SlashBladeCompat.isSlashBladeItem(stack)) {
            poseStack.pushPose();
            if (!model.rightWaistBones().isEmpty()) {
                applyWaistBoneTransform(HumanoidArm.RIGHT, poseStack, model);
            } else {
                poseStack.translate(0.25d, 1.25d, 0.0d);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(5.0f));
            }
            poseStack.translate(0.0d, 0.0d, -0.7d);
            poseStack.scale(0.01f, 0.01f, 0.01f);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0f));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0f));
            renderBladeOnly(poseStack, bufferSource, packedLight, stack);
            poseStack.popPose();
        }
    }

    private static void applyWaistBoneTransform(HumanoidArm arm, PoseStack poseStack, AnimatedGeoModel model) {
        if (arm == HumanoidArm.LEFT) {
            RenderUtils.prepMatrixForLocator(poseStack, model.leftWaistBones());
        } else {
            RenderUtils.prepMatrixForLocator(poseStack, model.rightWaistBones());
        }
    }
}