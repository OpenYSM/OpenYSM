package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PauseScreenButtonBuilder {
    public static boolean isServerConnected() {
        return YesSteveModel.isOnAndroid();
    }

    @Nullable
    public static List<Button> createButtons(PauseScreen pauseScreen) {
        if (isServerConnected()) {
            Minecraft minecraft = Minecraft.getInstance();
            Button buttonBuild = Button.builder(Component.translatable("gui.yes_steve_model.skin"), button -> {
                if (GeneralConfig.DISCLAIMER_SHOW.get()) {
                    minecraft.setScreen(new DisclaimerScreen());
                } else {
                    minecraft.setScreen(new PlayerModelScreen());
                }
            }).bounds((pauseScreen.width / 2) - 69, pauseScreen.height - 35, 138, 30).build();
            buttonBuild.setTooltip(Tooltip.create(Component.translatable("key.yes_steve_model.player_model.desc")));
            Button buttonBuild2 = Button.builder(Component.literal("🔧"), button2 -> {
                minecraft.setScreen(new ExtraPlayerRenderScreen());
            }).bounds((pauseScreen.width / 2) - 120, pauseScreen.height - 35, 50, 30).build();
            buttonBuild2.setTooltip(Tooltip.create(Component.translatable("key.yes_steve_model.open_extra_player_render.desc")));
            Button buttonBuild3 = Button.builder(Component.literal("😄"), button3 -> {
                if (minecraft.player != null) {
                    minecraft.player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                        String str = cap.getModelId();
                        ModelAssembly modelAssembly = cap.getModelAssembly();
                        if (modelAssembly != null && !modelAssembly.getModelData().getModelProperties().getExtraAnimation().isEmpty()) {
                            minecraft.setScreen(new AnimationRouletteScreen(str, modelAssembly, cap));
                        }
                    });
                }
            }).bounds((pauseScreen.width / 2) + 69, pauseScreen.height - 35, 50, 30).build();
            buttonBuild3.setTooltip(Tooltip.create(Component.translatable("key.yes_steve_model.animation_roulette.desc")));
            return List.of(buttonBuild, buttonBuild2, buttonBuild3);
        }
        return null;
    }
}