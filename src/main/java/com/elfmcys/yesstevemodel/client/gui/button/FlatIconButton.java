package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.client.gui.ISpecialWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlatIconButton extends AbstractWidget implements ISpecialWidget {

    private final int iconIndex;

    public FlatIconButton(int i, int i2, int i3, Component component) {
        super(i, i2, 115, 15, component);
        this.iconIndex = i3;
    }

    public void renderWidget(GuiGraphics guiGraphics, int i, int i2, float f) {
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + this.iconIndex, -280804798);
        renderScrollingString(guiGraphics, Minecraft.getInstance().font, 2, 16777215);
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}