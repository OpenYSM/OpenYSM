package com.elfmcys.yesstevemodel.client.compat.gun.swarfare;

import com.elfmcys.yesstevemodel.client.compat.gun.swarfare.event.GunArmRenderEvent;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

public class SWarfareCompat {

    private static final String MOD_ID = "superbwarfare";

    private static boolean IS_LOADED = false;

    public static void init() {
        ModFileInfo modFileById = LoadingModList.get().getModFileById(MOD_ID);
        if (modFileById != null) {
            IS_LOADED = new DefaultArtifactVersion(modFileById.versionString()).compareTo(new DefaultArtifactVersion("0.8.7.1")) >= 0;
        }
        if (IS_LOADED) {
            MinecraftForge.EVENT_BUS.register(new GunArmRenderEvent());
        }
    }

    public static boolean isGunItem(ItemStack itemStack) {
        if (IS_LOADED) {
            return SuperbWarfareAnimHandler.isGunItem(itemStack);
        }
        return false;
    }

    public static boolean isPlayerAiming(Player player) {
        if (IS_LOADED) {
            return SuperbWarfareAnimHandler.isPlayerAiming(player);
        }
        return false;
    }

    public static void applyGunTransform(ItemStack stack, AnimatedGeoModel model, LivingEntity entity, PoseStack poseStack, int packedLightIn, float partialTicks) {
        if (IS_LOADED && SuperbWarfareAnimHandler.isGunItem(stack)) {
            poseStack.pushPose();
            SuperbWarfareAnimHandler.applyGunTransform(stack, model, entity, poseStack, packedLightIn, partialTicks);
            poseStack.popPose();
        }
    }

    @Nullable
    public static PlayState handleTaczAnim(LivingEntity entity, AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event, String str, ILoopType loopType) {
        if (IS_LOADED && SuperbWarfareAnimHandler.isGunItem(entity.getMainHandItem())) {
            return SuperbWarfareAnimHandler.handleTaczAnim(event, str, loopType);
        }
        return null;
    }

    @Nullable
    public static PlayState handleGunHoldAnim(ItemStack stack, AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event) {
        if (IS_LOADED) {
            return SuperbWarfareAnimHandler.handleGunHoldAnim(event, stack);
        }
        return null;
    }

    @Nullable
    public static PlayState handleGunActionAnim(ItemStack stack, AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event) {
        if (IS_LOADED && SuperbWarfareAnimHandler.isGunItem(stack)) {
            return SuperbWarfareAnimHandler.handleGunActionAnim(event, stack);
        }
        return null;
    }

    @Nullable
    public static ResourceLocation getGunTexture(ItemStack stack) {
        if (IS_LOADED) {
            return ForgeRegistries.ITEMS.getKey(stack.getItem());
        }
        return null;
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }
}