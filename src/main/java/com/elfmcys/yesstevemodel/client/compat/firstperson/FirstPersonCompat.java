package com.elfmcys.yesstevemodel.client.compat.firstperson;

import dev.tr7zw.firstperson.api.FirstPersonAPI;
import dev.tr7zw.firstperson.api.PlayerOffsetHandler;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.LoadingModList;

public class FirstPersonCompat {

    private static final String MOD_ID_OLD = "firstpersonmod";

    private static final String MOD_ID_NEW = "firstperson";

    private static volatile float cameraDistance;

    private static boolean IS_LOADED;

    public static void init() {
        IS_LOADED = LoadingModList.get().getModFileById(MOD_ID_NEW) != null || LoadingModList.get().getModFileById(MOD_ID_OLD) != null;
        if (IS_LOADED) {
            detectVersion();
            setCameraDistance(24.0f);
        }
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    private static void detectVersion() {
        PlayerOffsetHandler handler = (abstractClientPlayer, f, vec3, vec32) -> new Vec3(vec32.x(), 1.5f - cameraDistance, vec32.z());

        FirstPersonAPI.registerPlayerHandler(handler);
    }

    public static boolean isFirstPersonActive() {
        return IS_LOADED && FirstPersonAPI.isRenderingPlayer();
    }

    public static boolean shouldHideHead() {
        return isFirstPersonActive();
    }

    public static void setCameraDistance(float f) {
        cameraDistance = f / 16.0f;
    }

}