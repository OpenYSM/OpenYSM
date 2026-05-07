package com.elfmcys.yesstevemodel.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FlatColorButton extends Button {

    private boolean selected;

    private List<Component> tooltip;

    public FlatColorButton(int x, int y, int width, int height, Component component, Button.OnPress onPress) {
        super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
        this.selected = false;
    }

    public FlatColorButton setTooltipText(String str) {
        this.tooltip = Collections.singletonList(Component.translatable(str));
        return this;
    }

    public FlatColorButton setTooltipLines(List<Component> list) {
        this.tooltip = list;
        return this;
    }

    public void renderTooltip(GuiGraphics guiGraphics, Screen screen, int mouseX, int mouseY) {
        if (this.isHovered && this.tooltip != null) {
            guiGraphics.renderComponentTooltip(screen.getMinecraft().font, this.tooltip, mouseX, mouseY);
        }
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        if (this.selected) {
            guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + this.height, -14774017, -14774017);
        } else {
            guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + this.height, -12369342, -12369342);
        }
        if (isHoveredOrFocused()) {
            guiGraphics.fillGradient(getX(), getY() + 1, getX() + 1, (getY() + this.height) - 1, -790560, -790560);
            guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + 1, -790560, -790560);
            guiGraphics.fillGradient((getX() + this.width) - 1, getY() + 1, getX() + this.width, (getY() + this.height) - 1, -790560, -790560);
            guiGraphics.fillGradient(getX(), (getY() + this.height) - 1, getX() + this.width, getY() + this.height, -790560, -790560);
        }
        renderScrollingString(guiGraphics, font, 2, 15986656);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}