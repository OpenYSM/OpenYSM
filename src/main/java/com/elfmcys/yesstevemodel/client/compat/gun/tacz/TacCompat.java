package com.elfmcys.yesstevemodel.client.compat.gun.tacz;

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class TacCompat {

    private static final String MOD_ID = "tacz";

    private static boolean IS_LOADED = false;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
        if (IS_LOADED) {
            TacAnimHandler.clearGunState();
        }
    }

    public static void registerControllerFunctions(CtrlBinding binding) {
        if (isLoaded()) {
            TacBinding.registerFunctions(binding);
        } else {
            registerTaczFunctions(binding);
        }
    }

    private static void registerTaczFunctions(CtrlBinding binding) {
        binding.livingEntityVar("tac_hold_gun", ctx -> false);
        binding.livingEntityVar("tac_gun_type", ctx -> StringPool.EMPTY);
        binding.livingEntityVar("tac_gun_id", ctx -> StringPool.EMPTY);
        binding.livingEntityVar("tac_is_fire", ctx -> false);
        binding.livingEntityVar("tac_is_aim", ctx -> false);
        binding.livingEntityVar("tac_is_reload", ctx -> false);
        binding.livingEntityVar("tac_is_melee", ctx -> false);
        binding.livingEntityVar("tac_is_draw", ctx -> false);
        binding.livingEntityVar("tac_fire_mode", ctx -> StringPool.EMPTY);
    }

    public static void applyItemTransform(ItemStack stack, AnimatedGeoModel model, LivingEntity entity, PoseStack poseStack, int packedLightIn, float partialTicks) {
        if (isLoaded() && TacAnimHandler.isTaczGunItem(stack)) {
            poseStack.pushPose();
            TacAnimHandler.applyTaczGunTransform(stack, model, entity, poseStack, packedLightIn, partialTicks);
            poseStack.popPose();
        }
    }

    @Nullable
    public static PlayState handleTaczAnimState(LivingEntity entity, AnimationEvent<? extends LivingAnimatable<?>> event, String animation, ILoopType loopType) {
        if (isLoaded() && TacAnimHandler.isTaczGunItem(entity.getMainHandItem())) {
            return TacAnimHandler.handleTaczAnimState(event, animation, loopType);
        }
        return null;
    }

    @Nullable
    public static PlayState handleGunHoldAnimState(ItemStack stack, AnimationEvent<? extends LivingAnimatable<?>> event) {
        if (isLoaded() && TacAnimHandler.isTaczGunItem(stack)) {
            return TacAnimHandler.handleTaczGunHold(event, stack);
        }
        return null;
    }

    @Nullable
    public static PlayState handleGunActionAnimState(ItemStack stack, AnimationEvent<? extends LivingAnimatable<?>> event) {
        if (isLoaded() && TacAnimHandler.isTaczGunItem(stack)) {
            return TacAnimHandler.handleTaczGunAction(event, stack);
        }
        return null;
    }

    public static void handleGunSound(LivingEntity entity, ItemStack stack) {
        if (isLoaded() && TacAnimHandler.isTaczGunItem(stack)) {
            TacAnimHandler.handleTaczSound(entity);
        }
    }

    public static void handleItemSound(ItemStack stack) {
        if (isLoaded() && TacAnimHandler.isTaczGunItem(stack)) {
            TacAnimHandler.clearTaczSound();
        }
    }

    @Nullable
    public static ResourceLocation getGunTexture(ItemStack stack) {
        if (isLoaded()) {
            return TacAnimHandler.getTaczGunTexture(stack);
        }
        return null;
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }
}