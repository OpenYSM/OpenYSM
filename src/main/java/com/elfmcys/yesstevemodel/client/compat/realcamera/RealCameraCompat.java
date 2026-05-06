package com.elfmcys.yesstevemodel.client.compat.realcamera;

import net.minecraftforge.fml.ModList;

public class RealCameraCompat {

    private static final String MOD_ID = "realcamera";

    private static boolean IS_LOADED = false;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static boolean isActive() {
        if (IS_LOADED) {
            return RealCameraChecker.isRealCameraActive();
        }
        return false;
    }
}