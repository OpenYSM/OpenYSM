package com.elfmcys.yesstevemodel.client.compat.slashblade;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;

public class SlashBladeBinding {
    public static void registerFunctions(CtrlBinding binding) {
        binding.livingEntityVar("slashblade_animation", SlashBladeStateHelper::getSlashBladeAnimationFromContext);
    }
}