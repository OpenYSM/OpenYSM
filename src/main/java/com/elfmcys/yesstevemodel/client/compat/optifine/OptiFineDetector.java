package com.elfmcys.yesstevemodel.client.compat.optifine;

public class OptiFineDetector {

    private static boolean optifinePresent;

    public static void init() {
        try {
            Class.forName("net.optifine.Config");
            optifinePresent = true;
        } catch (ClassNotFoundException e) {
            optifinePresent = false;
        }
    }

    public static boolean isOptifinePresent() {
        return optifinePresent;
    }
}