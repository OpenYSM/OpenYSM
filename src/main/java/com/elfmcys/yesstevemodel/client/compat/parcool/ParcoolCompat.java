package com.elfmcys.yesstevemodel.client.compat.parcool;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.animation.predicate.PlayerSpecialAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

public class ParcoolCompat {
    private static final ArtifactVersion MIN_VERSION = new DefaultArtifactVersion("3.4.1");
    private static final String MOD_ID = "parcool";

    private static boolean IS_LOADED;

    private static boolean INCOMPATIBLE_VERSION;

    public static void init() {
        if (GeneralConfig.PARCOOL != null && !GeneralConfig.PARCOOL.get()) {
            IS_LOADED = false;
            return;
        }
        ModFileInfo modFileById = LoadingModList.get().getModFileById(MOD_ID);
        if (modFileById != null) {
            if (modFileById.getMods().get(0).getVersion().compareTo(MIN_VERSION) >= 0) {
                IS_LOADED = true;
            } else {
                INCOMPATIBLE_VERSION = true;
            }
        }
    }

    public static Optional<Pair<String, String>> getInCompatibleInfo() {
        if (INCOMPATIBLE_VERSION) {
            return Optional.of(Pair.of(MOD_ID, MIN_VERSION.toString()));
        }
        return Optional.empty();
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> getControllerFactory() {
        if (IS_LOADED) {
            return Optional.of((name, entity) -> new CompositeAnimationController(entity, name, 0.1f, new PlayerSpecialAnimationPredicate()));
        }
        return Optional.empty();
    }

    public static boolean isPlayerParcooling(Player player) {
        if (isLoaded()) {
            return ParcoolAnimationHandler.hasParcoolAnimation(player);
        }
        return false;
    }

    @Nullable
    public static String getActionName(Player player) {
        if (isLoaded()) {
            return ParcoolAnimationHandler.getParcoolAnimationName(player);
        }
        return null;
    }

    public static void registerBindings(CtrlBinding binding) {
        if (isLoaded()) {
            ParcoolBinding.registerBindings(binding);
        } else {
            registerDummyBindings(binding);
        }
    }

    private static void registerDummyBindings(CtrlBinding binding) {
        binding.livingEntityVar("parcool_state", interfaceC0807x6b368640 -> StringPool.EMPTY);
    }
}