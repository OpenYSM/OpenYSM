package com.elfmcys.yesstevemodel.client.compat.gun.swarfare;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.item.LungeMine;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.elfmcys.yesstevemodel.client.compat.gun.tacz.ConditionTAC;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SuperbWarfareAnimHandler {

    private static final TagKey<Item> PISTOL_TAG = TagKey.create(Registries.ITEM, ResourceLocation.parse("superbwarfare:animated/pistol"));

    private static final TagKey<Item> RPG_TAG = TagKey.create(Registries.ITEM, ResourceLocation.parse("superbwarfare:animated/rpg"));

    public static boolean isGunItem(ItemStack stack) {
        return stack.getItem() instanceof GunItem;
    }

    public static boolean isPlayerAiming(Player player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof VehicleEntity) {
            return ((VehicleEntity) vehicle).hidePassenger(player);
        }
        return false;
    }

    public static void applyGunTransform(ItemStack stack, AnimatedGeoModel model, LivingEntity entity, PoseStack poseStack, int packedLightIn, float partialTicks) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        if (stack.is(PISTOL_TAG) && !model.tacPistolBones().isEmpty()) {
            RenderUtils.prepMatrixForLocator(poseStack, model.tacPistolBones());
            poseStack.translate(0.0d, -0.125d, 0.0d);
            poseStack.scale(0.65f, 0.65f, 0.65f);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), entity.level(), entity.getId());
        }
        if (!stack.is(PISTOL_TAG) && !model.tacRifleBones().isEmpty()) {
            RenderUtils.prepMatrixForLocator(poseStack, model.tacRifleBones());
            poseStack.scale(0.65f, 0.65f, 0.65f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-180.0f));
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), entity.level(), entity.getId());
        }
    }

    @Nullable
    public static PlayState handleLungeMineAnim(AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event) {
        LivingEntity livingEntity = ((LivingAnimatable<?>) event.getAnimatable()).getEntity();
        if (!Objects.equals(Minecraft.getInstance().player, livingEntity) || !(livingEntity.getMainHandItem().getItem() instanceof LungeMine)) {
            return null;
        }
        if (ClientEventHandler.lungeSprint > 0) {
            return setAnimLoop(event, "superbwarfare:lunge_mine_sprint");
        }
        if (ClientEventHandler.lungeDraw > 0) {
            return setAnimLoop(event, "superbwarfare:lunge_mine_draw");
        }
        if (ClientEventHandler.lungeAttack > 0) {
            return setAnimLoop(event, "superbwarfare:lunge_mine_fire");
        }
        if (livingEntity.isSprinting() && livingEntity.onGround() && ClientEventHandler.lungeDraw == 0) {
            return setAnimLoop(event, "superbwarfare:lunge_mine_run");
        }
        return setAnimLoop(event, "superbwarfare:lunge_mine_idle");
    }

    public static PlayState handleTaczAnim(AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event, String animation, ILoopType loopType) {
        String str2 = "tac:" + animation;
        if (event.getAnimatable().getAnimation(str2) != null) {
            return setAnimWithMode(event, str2, loopType);
        }
        return setAnimWithMode(event, animation, loopType);
    }

    @NotNull
    private static PlayState playGunAnimLoop(AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event, ItemStack stack, String animation) {
        return playGunAnimByType(event, stack, animation, ILoopType.EDefaultLoopTypes.LOOP);
    }

    public static PlayState handleGunHoldAnim(AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event, ItemStack stack) {
        PlayState playState = handleLungeMineAnim(event);
        if (playState != null) {
            return playState;
        }
        if (!(stack.getItem() instanceof GunItem)) {
            return null;
        }
        LivingEntity entity = ((LivingAnimatable<?>) event.getAnimatable()).getEntity();
        if (!entity.isSwimming() && entity.getPose() == Pose.SWIMMING) {
            if (Math.abs(event.getLimbSwingAmount()) > 0.05d) {
                return playGunAnimLoop(event, stack, "tac:climb:");
            }
            return playGunAnimLoop(event, stack, "tac:climbing:");
        }
        double zoomTime = ClientEventHandler.zoomTime;
        if (!Objects.equals(Minecraft.getInstance().player, entity)) {
            zoomTime = 0.0d;
        }
        if (zoomTime > 0.3d) {
            return playGunAnimLoop(event, stack, "tac:aim:");
        }
        if (entity.onGround() && entity.isSprinting()) {
            return playGunAnimLoop(event, stack, "tac:run:");
        }
        return playGunAnimLoop(event, stack, "tac:hold:");
    }

    public static PlayState handleGunActionAnim(AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event, ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem)) {
            return PlayState.STOP;
        }
        LivingEntity livingEntity = ((LivingAnimatable<?>) event.getAnimatable()).getEntity();
        GunData gunDataFrom = GunData.from(stack);
        if (!Objects.equals(Minecraft.getInstance().player, livingEntity)) {
            return PlayState.STOP;
        }
        if (event.getController() != null && event.getController().isPlaying()) {
            event.getController().stopTransition();
        }
        if (gunDataFrom.reloading() && gunDataFrom.reload.time() > 40) {
            return playGunAnimByType(event, stack, "tac:reload:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        if (ClientEventHandler.gunMelee > 5) {
            return playGunAnimByType(event, stack, "tac:melee:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        double fireRotTimer = ClientEventHandler.fireRotTimer;
        if (0.0d < fireRotTimer && fireRotTimer < 1.0d) {
            double zoomTime = ClientEventHandler.zoomTime;
            if (!livingEntity.isSwimming() && livingEntity.getPose() == Pose.SWIMMING && ((double) Math.abs(event.getLimbSwingAmount())) <= 0.05d) {
                return playGunAnimByType(event, stack, "tac:climbing:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            if (zoomTime > 0.3d) {
                return playGunAnimByType(event, stack, "tac:aim:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            return playGunAnimByType(event, stack, "tac:hold:fire:", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        return PlayState.CONTINUE;
    }

    @NotNull
    private static PlayState playGunAnimByType(AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event, ItemStack itemStack, String animation, ILoopType loopType) {
        ConditionTAC tac = event.getAnimatable().getModelConfig().getTAC();
        if (tac != null) {
            String str = tac.doTest(((LivingAnimatable<?>) event.getAnimatable()).getEntity().getMainHandItem(), animation);
            if (StringUtils.isNoneBlank(str)) {
                return setAnimWithMode(event, str, loopType);
            }
        }
        if (itemStack.is(PISTOL_TAG)) {
            return setAnimWithMode(event, animation + "pistol", loopType);
        }
        if (itemStack.is(RPG_TAG)) {
            return setAnimWithMode(event, animation + "rpg", loopType);
        }
        return setAnimWithMode(event, animation + "rifle", loopType);
    }

    @NotNull
    private static PlayState setAnimWithMode(AnimationEvent<?> event, String animation, ILoopType loopType) {
        event.getController().setAnimation(animation, loopType);
        return PlayState.CONTINUE;
    }

    @NotNull
    private static PlayState setAnimLoop(AnimationEvent<?> event, String animation) {
        event.getController().setAnimation(animation);
        return PlayState.CONTINUE;
    }
}