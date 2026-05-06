package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.config.LoadingStateConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class LoadingStateButton extends Button {
    public LoadingStateButton(int i, int i2) {
        super(i, i2, 100, 20, Component.empty(), button -> {
        }, DEFAULT_NARRATION);
    }

    public void renderWidget(GuiGraphics guiGraphics, int i, int i2, float f) {
        super.renderWidget(guiGraphics, i, i2, f);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.yes_steve_model.config.loading_state_position"), getX() + 105, getY() + 6, 16777215, false);
    }

    public Component getMessage() {
        return Component.literal(LoadingStateConfig.LOADING_STATE_POSITION.get().name());
    }

    public void onPress() {
        LoadingStateConfig.Position stateConfig;
        switch (LoadingStateConfig.LOADING_STATE_POSITION.get()) {
            case TOP_LEFT:
                stateConfig = LoadingStateConfig.Position.TOP_CENTER;
                break;
            case TOP_CENTER:
                stateConfig = LoadingStateConfig.Position.TOP_RIGHT;
                break;
            case TOP_RIGHT:
                stateConfig = LoadingStateConfig.Position.BOTTOM_RIGHT;
                break;
            case BOTTOM_RIGHT:
                stateConfig = LoadingStateConfig.Position.BOTTOM_CENTER;
                break;
            case BOTTOM_CENTER:
                stateConfig = LoadingStateConfig.Position.BOTTOM_LEFT;
                break;
            case BOTTOM_LEFT:
                stateConfig = LoadingStateConfig.Position.TOP_LEFT;
                break;
            default:
                throw new IncompatibleClassChangeError();
        }
        LoadingStateConfig.LOADING_STATE_POSITION.set(stateConfig);
    }
}