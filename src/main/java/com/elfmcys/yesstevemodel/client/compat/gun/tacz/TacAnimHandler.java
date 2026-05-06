package com.elfmcys.yesstevemodel.client.compat.gun.tacz;

import com.elfmcys.yesstevemodel.client.compat.gun.tacz.event.GunFireReloadEvent;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public class TacAnimHandler {
    TacAnimHandler() {
    }

    public static boolean isTaczGunItem(ItemStack stack) {
        return stack.getItem() instanceof IGun;
    }

    public static void clearGunState() {
        MinecraftForge.EVENT_BUS.register(new GunFireReloadEvent());
    }

    public static boolean isTaczGunInHand(ItemStack stack) {
        return false;
    }

    public static void applyTaczGunTransform(ItemStack stack, AnimatedGeoModel model, LivingEntity entity, PoseStack poseStack, int packedLightIn, float partialTicks) {
        IGun iGunOrNull = IGun.getIGunOrNull(stack);
        if (iGunOrNull == null) {
            return;
        }
        TimelessAPI.getCommonGunIndex(iGunOrNull.getGunId(stack)).ifPresent(commonGunIndex -> {
            String type = commonGunIndex.getType();
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            if (isGunType(type, GunTabType.PISTOL) && !model.tacPistolBones().isEmpty()) {
                RenderUtils.prepMatrixForLocator(poseStack, model.tacPistolBones());
                poseStack.translate(0.0d, -0.125d, 0.0d);
                poseStack.scale(0.65f, 0.65f, 0.65f);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0f));
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0f));
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), entity.level(), entity.getId());
            }
            if (!isGunType(type, GunTabType.PISTOL) && !model.tacRifleBones().isEmpty()) {
                RenderUtils.prepMatrixForLocator(poseStack, model.tacRifleBones());
                poseStack.scale(0.65f, 0.65f, 0.65f);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-180.0f));
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), entity.level(), entity.getId());
            }
        });
    }

    public static PlayState handleTaczHandAnim(AnimationEvent<? extends AnimatableEntity<? extends LivingEntity>> event, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return playLoopAnimation(event, "tac:mainhand:grenade");
        }
        return playLoopAnimation(event, "tac:offhand:grenade");
    }

    public static PlayState handleTaczAnimState(AnimationEvent<? extends AnimatableEntity<? extends LivingEntity>> event, String animation, ILoopType loopType) {
        String str2 = "tac:" + animation;
        if (event.getAnimatable().getAnimation(str2) != null) {
            return setAnimationAndContinue(event, str2, loopType);
        }
        return setAnimationAndContinue(event, animation, loopType);
    }

    public static PlayState handleTaczGunHold(AnimationEvent<? extends LivingAnimatable<?>> event, ItemStack stack) {
        IGun iGunOrNull = IGun.getIGunOrNull(stack);
        if (iGunOrNull == null) {
            return PlayState.STOP;
        }
        Optional<CommonGunIndex> commonGunIndex = TimelessAPI.getCommonGunIndex(iGunOrNull.getGunId(stack));
        if (commonGunIndex.isEmpty()) {
            return PlayState.STOP;
        }
        String type = commonGunIndex.get().getType();
        LivingEntity livingEntity = ((LivingAnimatable<?>) event.getAnimatable()).getEntity();
        IGunOperator iGunOperatorFromLivingEntity = IGunOperator.fromLivingEntity(livingEntity);
        if (!livingEntity.isSwimming() && livingEntity.getPose() == Pose.SWIMMING) {
            if (Math.abs(event.getLimbSwingAmount()) > 0.05d) {
                return playGunAnimation(event, type, "tac:climb:");
            }
            return playGunAnimation(event, type, "tac:climbing:");
        }
        if (iGunOperatorFromLivingEntity.getSynAimingProgress() > 0.0f) {
            return playGunAnimation(event, type, "tac:aim:");
        }
        if (livingEntity.onGround() && livingEntity.isSprinting()) {
            return playGunAnimation(event, type, "tac:run:");
        }
        return playGunAnimation(event, type, "tac:hold:");
    }

    public static PlayState handleTaczGunAction(AnimationEvent<? extends LivingAnimatable<?>> event, ItemStack stack) {
        IGun iGunOrNull = IGun.getIGunOrNull(stack);
        if (iGunOrNull == null) {
            return PlayState.STOP;
        }
        Optional<CommonGunIndex> commonGunIndex = TimelessAPI.getCommonGunIndex(iGunOrNull.getGunId(stack));
        if (commonGunIndex.isEmpty()) {
            return PlayState.STOP;
        }
        String type = commonGunIndex.get().getType();
        LivingEntity livingEntity = ((LivingAnimatable<?>) event.getAnimatable()).getEntity();
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(livingEntity);
        long synShootCoolDown = gunOperator.getSynShootCoolDown();
        if (livingEntity instanceof IClientPlayerGunOperator) {
            synShootCoolDown = Math.max(synShootCoolDown, ((IClientPlayerGunOperator) livingEntity).getClientShootCoolDown());
        }
        if (event.getAnimatable().isExtraRenderFlag()) {
            playLoopAnimation(event, "empty");
        }
        event.getAnimatable().setExtraRenderFlag(false);
        if (gunOperator.getSynReloadState().getCountDown() > 0.0f) {
            return playGunAnimationWithMode(event, type, "tac:reload:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        if (gunOperator.getSynMeleeCoolDown() > 0) {
            return playGunAnimationWithMode(event, type, "tac:melee:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        if (synShootCoolDown > 0) {
            float synAimingProgress = gunOperator.getSynAimingProgress();
            if (!livingEntity.isSwimming() && livingEntity.getPose() == Pose.SWIMMING && ((double) Math.abs(event.getLimbSwingAmount())) <= 0.05d) {
                return playGunAnimationWithMode(event, type, "tac:climbing:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            if (synAimingProgress > 0.0f) {
                return playGunAnimationWithMode(event, type, "tac:aim:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            return playGunAnimationWithMode(event, type, "tac:hold:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        return PlayState.CONTINUE;
    }

    public static void handleTaczSound(LivingEntity entity) {
        if (entity.equals(Minecraft.getInstance().player)) {
            MuzzleFlashRender.isSelf = true;
            ShellRender.isSelf = true;
        }
    }

    public static void clearTaczSound() {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
    }

    @Nullable
    public static ResourceLocation getTaczGunTexture(ItemStack stack) {
        IGun iGunOrNull = IGun.getIGunOrNull(stack);
        if (iGunOrNull == null) {
            return null;
        }
        return iGunOrNull.getGunId(stack);
    }

    @NotNull
    private static PlayState playGunAnimation(AnimationEvent<? extends LivingAnimatable<?>> event, String str, String str2) {
        return playGunAnimationWithMode(event, str, str2, ILoopType.EDefaultLoopTypes.LOOP);
    }

    @NotNull
    private static PlayState playGunAnimationWithMode(AnimationEvent<? extends LivingAnimatable<?>> event, String str, String str2, ILoopType loopType) {
        ConditionTAC tac = event.getAnimatable().getModelConfig().getTAC();
        if (tac != null) {
            String str3 = tac.doTest(((LivingEntity) ((LivingAnimatable) event.getAnimatable()).getEntity()).getMainHandItem(), str2);
            if (StringUtils.isNoneBlank(str3)) {
                return setAnimationAndContinue(event, str3, loopType);
            }
        }
        if (isGunType(str, GunTabType.PISTOL)) {
            return setAnimationAndContinue(event, str2 + "pistol", loopType);
        }
        if (isGunType(str, GunTabType.RPG)) {
            return setAnimationAndContinue(event, str2 + "rpg", loopType);
        }
        return setAnimationAndContinue(event, str2 + "rifle", loopType);
    }

    @NotNull
    private static PlayState playLoopAnimation(AnimationEvent<?> event, String animation) {
        return setAnimationAndContinue(event, animation, ILoopType.EDefaultLoopTypes.LOOP);
    }

    @NotNull
    private static PlayState setAnimationAndContinue(AnimationEvent<?> event, String animation, ILoopType loopType) {
        event.getController().setAnimation(animation, loopType);
        return PlayState.CONTINUE;
    }

    private static boolean isGunType(String str, GunTabType gunTabType) {
        return str.equals(gunTabType.name().toLowerCase(Locale.ENGLISH));
    }
}