package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouMaidCompat;

public final class ClientCommonInit {
    private ClientCommonInit() {
    }

    public static void run() {
        TouhouMaidCompat.init();
        ClientModelManager.loadDefaultModel();
    }
}
