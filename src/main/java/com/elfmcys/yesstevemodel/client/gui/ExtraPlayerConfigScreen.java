package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.ConfigCheckBoxForge;
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.client.gui.button.LoadingStateButton;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.config.ExtraPlayerRenderConfig;
import com.elfmcys.yesstevemodel.config.LoadingStateConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.Nullable;

public class ExtraPlayerConfigScreen extends Screen {

    @Nullable
    private final PlayerModelScreen parentScreen;

    public ExtraPlayerConfigScreen(@Nullable PlayerModelScreen modelScreen) {
        super(Component.literal("YSM Config GUI"));
        this.parentScreen = modelScreen;
    }

    public void init() {
        int guiLeft = (this.width - 420) / 2;
        int guiTop = (this.height - 265) / 2;
        addRenderableWidget(new FlatColorButton(guiLeft + 5, guiTop + 2, 80, 18, Component.translatable("gui.yes_steve_model.model.return"), button -> {
            getMinecraft().setScreen(this.parentScreen);
        }));
        addRenderableWidget(new ForgeSlider(guiLeft + 5, guiTop + 24, 320, 18, Component.translatable("gui.yes_steve_model.config.sound_volume"), Component.literal("%"), 0.0d, 100.0d, GeneralConfig.SOUND_VOLUME.get().doubleValue(), true) {
            public void applyValue() {
                GeneralConfig.SOUND_VOLUME.set(Double.valueOf(getValue()));
            }
        });
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 45, "disable_self_model", GeneralConfig.DISABLE_SELF_MODEL));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 67, "disable_other_model", GeneralConfig.DISABLE_OTHER_MODEL));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 89, "print_animation_roulette_msg", GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 111, "disable_self_hands", GeneralConfig.DISABLE_SELF_HANDS));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 133, "disable_player_render", ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 155, "disable_projectile_model", GeneralConfig.DISABLE_PROJECTILE_MODEL));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 177, "disable_vehicle_model", GeneralConfig.DISABLE_VEHICLE_MODEL));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 199, "disable_external_first_person_anim", GeneralConfig.DISABLE_EXTERNAL_FP_ANIM));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 221, "disable_loading_state_screen", LoadingStateConfig.DISABLE_LOADING_STATE_SCREEN));
        addRenderableWidget(new ConfigCheckBoxForge(guiLeft + 5, guiTop + 243, "use_compatibility_renderer", GeneralConfig.USE_COMPATIBILITY_RENDERER));
        addRenderableWidget(new LoadingStateButton(guiLeft + 5, guiTop + 264));
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}