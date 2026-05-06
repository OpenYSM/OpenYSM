package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.ISpecialWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ConfigCheckBox extends StateSwitchingButton implements ISpecialWidget {

    private static final ResourceLocation location = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/roulette.png");

    private final Consumer<Boolean> consumer2;

    private final Component component2;

    public ConfigCheckBox(int i, int i2, int i3, Component component, Consumer<Boolean> consumer) {
        super(i, i2, i3, 12, false);
        this.component2 = component;
        this.consumer2 = consumer;
        initTextureValues(0, 0, 128, 12, location);
    }

    public ConfigCheckBox(int i, int i2, Component component, Consumer<Boolean> consumer) {
        this(i, i2, 115, component, consumer);
    }

    public void renderWidget(GuiGraphics guiGraphics, int i, int i2, float f) {
        super.renderWidget(guiGraphics, i, i2, f);
        guiGraphics.drawString(Minecraft.getInstance().font, this.component2, getX() + 14, getY() + 2, -1, false);
    }

    public void onClick(double d, double d2) {
        this.isStateTriggered = !this.isStateTriggered;
        this.consumer2.accept(Boolean.valueOf(this.isStateTriggered));
    }
}