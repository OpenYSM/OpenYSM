package com.elfmcys.yesstevemodel.client.compat.create;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class CreateCompat {

    private static final String MOD_ID = "create";

    private static boolean IS_LOADED = false;

    public static void init() {
        ModFileInfo modFileById = LoadingModList.get().getModFileById(MOD_ID);
        if (modFileById != null) {
            IS_LOADED = new DefaultArtifactVersion(modFileById.versionString()).compareTo(new DefaultArtifactVersion("6.0.0")) >= 0;
        }
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static boolean isPlayerOnCreateContraption(Player player) {
        if (IS_LOADED) {
            return SkyHookHelper.isPlayerOnSkyHook(player);
        }
        return false;
    }

    public static void registerCreateFunctions(CtrlBinding binding) {
        if (IS_LOADED) {
            binding.playerEntityVar("create_hanging_skyhook", interfaceC0807x6b368640 -> isPlayerOnCreateContraption(interfaceC0807x6b368640.entity()));
        } else {
            binding.playerEntityVar("create_hanging_skyhook", interfaceC0807x6b3686402 -> false);
        }
    }
}