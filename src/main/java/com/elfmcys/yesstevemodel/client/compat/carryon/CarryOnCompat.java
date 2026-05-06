package com.elfmcys.yesstevemodel.client.compat.carryon;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.animation.predicate.PlayerAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.function.BiFunction;

public class CarryOnCompat {

    private static final String MOD_ID = "carryon";

    private static boolean IS_LOADED = false;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> getControllerFactory() {
        if (IS_LOADED) {
            return Optional.of((str, animatable) -> new CompositeAnimationController(animatable, str, 0.1f, new PlayerAnimationPredicate()));
        }
        return Optional.empty();
    }

    public static boolean isPlayerCarrying(Player player) {
        if (IS_LOADED) {
            return CarryOnDataHelper.isPlayerCarrying(player);
        }
        return false;
    }

    public static void registerBindings(CtrlBinding binding) {
        if (isLoaded()) {
            CarryOnBinding.registerBindings(binding);
        } else {
            registerDummyBindings(binding);
        }
    }

    private static void registerDummyBindings(CtrlBinding binding) {
        binding.livingEntityVar("carryon_type", ctx -> StringPool.EMPTY);
        binding.livingEntityVar("carryon_is_princess", ctx -> false);
    }
}