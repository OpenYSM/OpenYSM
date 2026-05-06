package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OpenModelFolderScreen extends Screen {

    private final PlayerModelScreen parentScreen;

    public OpenModelFolderScreen(PlayerModelScreen modelScreen) {
        super(Component.literal("Open Model Folder"));
        this.parentScreen = modelScreen;
    }

    public void init() {
        int i = (this.width - 310) / 2;
        int i2 = (this.height / 2) + 60;
        clearWidgets();
        addRenderableWidget(Button.builder(Component.translatable("gui.yes_steve_model.open_model_folder.open"), button -> {
            Util.getPlatform().openFile(ServerModelManager.CUSTOM.toFile());
        }).bounds(i, i2, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.yes_steve_model.model.return"), button2 -> {
            getMinecraft().setScreen(this.parentScreen);
        }).bounds(i + 160, i2, 150, 20).build());
    }

    public void render(GuiGraphics guiGraphics, int i, int i2, float f) {
        renderBackground(guiGraphics);
        guiGraphics.drawWordWrap(this.font, Component.translatable("gui.yes_steve_model.open_model_folder.tips"), (this.width - 400) / 2, (this.height / 2) - 80, 400, 16777215);
        super.render(guiGraphics, i, i2, f);
    }
}