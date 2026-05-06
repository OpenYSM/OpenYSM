package com.elfmcys.yesstevemodel.client.gui.button;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigCheckBoxForge extends Checkbox {

    private final ForgeConfigSpec.BooleanValue forgeConfigSpec;

    public ConfigCheckBoxForge(int i, int i2, String str, ForgeConfigSpec.BooleanValue booleanValue) {
        super(i, i2, 400, 20, Component.translatable("gui.yes_steve_model.config." + str), booleanValue.get().booleanValue());
        this.forgeConfigSpec = booleanValue;
    }

    public void onPress() {
        super.onPress();
        this.forgeConfigSpec.set(Boolean.valueOf(!this.forgeConfigSpec.get().booleanValue()));
    }
}