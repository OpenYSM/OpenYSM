package com.elfmcys.yesstevemodel.client.compat.acceleratedrendering;

import net.minecraftforge.fml.ModList;

public class AcceleratedRenderingCompat {

    private static final String MOD_ID = "acceleratedrendering";

    private static boolean IS_LOADED;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }
}