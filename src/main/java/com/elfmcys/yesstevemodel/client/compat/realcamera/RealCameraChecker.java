package com.elfmcys.yesstevemodel.client.compat.realcamera;

import com.xtracr.realcamera.RealCameraCore;

public class RealCameraChecker {
    public static boolean isRealCameraActive() {
        return RealCameraCore.isActive();
    }
}