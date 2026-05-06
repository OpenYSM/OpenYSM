package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends FlatColorButton {

    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");

    private final int iconU;

    private final int iconV;

    public IconButton(int i, int i2, int i3, int i4, int i5, int i6, Button.OnPress onPress) {
        super(i, i2, i3, i4, Component.empty(), onPress);
        this.iconU = i5;
        this.iconV = i6;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int i2, float f) {
        super.renderWidget(guiGraphics, i, i2, f);
        guiGraphics.blit(ICON_TEXTURE, getX() + ((this.width - 16) / 2), getY() + ((this.height - 16) / 2), 16, 16, this.iconU, this.iconV, 16, 16, 256, 256);
    }
}