package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;

public class DisclaimerScreen extends Screen {

    private Checkbox checkbox;

    private int textY;

    private int textHeight;

    public DisclaimerScreen() {
        super(Component.literal("Disclaimer GUI"));
    }

    public void init() {
        clearWidgets();
        int size = this.font.split(Component.translatable("gui.yes_steve_model.disclaimer.text"), 400).size();
        Objects.requireNonNull(this.font);
        int i = (size * 9) + 20 + 20 + 10 + 20;
        this.textY = (this.width - 400) / 2;
        this.textHeight = (this.height - i) / 2;
        MutableComponent mutableComponentTranslatable = Component.translatable("gui.yes_steve_model.disclaimer.read");
        int iWidth = this.font.width(mutableComponentTranslatable);
        this.checkbox = new Checkbox((this.width - iWidth) / 2, (this.textHeight + i) - 50, iWidth, 20, mutableComponentTranslatable, !GeneralConfig.DISCLAIMER_SHOW.get().booleanValue());
        addRenderableWidget(this.checkbox);
        addRenderableWidget(new Button.Builder(Component.translatable("gui.yes_steve_model.disclaimer.close"), button -> {
            if (this.checkbox.selected()) {
                GeneralConfig.DISCLAIMER_SHOW.set(false);
                Minecraft.getInstance().setScreen(new PlayerModelScreen());
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        }).size(300, 20).pos((this.width - 300) / 2, (this.textHeight + i) - 20).build());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawWordWrap(this.font, Component.translatable("gui.yes_steve_model.disclaimer.text"), this.textY, this.textHeight, 400, -1);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}