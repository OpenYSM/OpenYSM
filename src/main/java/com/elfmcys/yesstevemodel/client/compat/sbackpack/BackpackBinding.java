package com.elfmcys.yesstevemodel.client.compat.sbackpack;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;

public class BackpackBinding {
    public static void registerFunctions(CtrlBinding binding) {
        binding.livingEntityVar("has_sophisticated_backpack", context -> {
            return SBackpackCompat.getBackpackItem(context.entity()) != null;
        });
    }
}