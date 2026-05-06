package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DebugAnimationScreen extends Screen {

    private final PlayerModelScreen parentScreen;

    private int guiLeft;

    private int guiTop;

    public DebugAnimationScreen(PlayerModelScreen modelScreen) {
        super(Component.literal("YSM Config GUI"));
        this.parentScreen = modelScreen;
    }

    public void init() {
        this.guiLeft = (this.width - 420) / 2;
        this.guiTop = (this.height - 235) / 2;
        addRenderableWidget(new FlatColorButton(this.guiLeft + 5, this.guiTop, 80, 18, Component.translatable("gui.yes_steve_model.model.return"), button -> {
            getMinecraft().setScreen(this.parentScreen);
        }));
    }

    public void render(GuiGraphics guiGraphics, int i, int i2, float f) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, "Coming Soooooooooooooooooooooooooon™", this.width / 2, (this.height / 2) - 5, ChatFormatting.DARK_RED.getColor().intValue());
        super.render(guiGraphics, i, i2, f);
    }
}