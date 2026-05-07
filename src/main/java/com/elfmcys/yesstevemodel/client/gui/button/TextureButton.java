package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SRequestSwitchModelPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class TextureButton extends Button {

    public final PlayerPreviewEntity previewEntity;

    public final ModelAssembly modelAssembly;

    public TextureButton(int x, int y, PlayerPreviewEntity previewEntity, ModelAssembly modelAssembly) {
        super(x, y, 54, 102, Component.empty(), button -> {
        }, DEFAULT_NARRATION);
        this.previewEntity = previewEntity;
        this.modelAssembly = modelAssembly;
    }

    public void onPress() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            localPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                cap.setCurrentTexture(this.previewEntity.getCurrentTextureName());
                NetworkHandler.sendToServer(new C2SRequestSwitchModelPacket(this.previewEntity.getModelId(), this.previewEntity.getCurrentTextureName()));
            });
        }
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + this.height, -12369342, -12369342);
        renderPlayerPreview(guiGraphics, minecraft.getFrameTime());
        String str = this.previewEntity.getCurrentTextureName();
        MutableComponent mutableComponentLiteral = Component.literal(ModelMetadataPresenter.getLocalizedModelString(this.modelAssembly, "files.player.texture.%s".formatted(str), str));
        List listSplit = font.split(mutableComponentLiteral, 50);
        if (listSplit.size() > 1) {
            guiGraphics.drawCenteredString(font, (FormattedCharSequence) listSplit.get(0), getX() + (this.width / 2), (getY() + this.height) - 19, 15986656);
            guiGraphics.drawCenteredString(font, (FormattedCharSequence) listSplit.get(1), getX() + (this.width / 2), (getY() + this.height) - 10, 15986656);
        } else {
            guiGraphics.drawCenteredString(font, mutableComponentLiteral, getX() + (this.width / 2), (getY() + this.height) - 15, 15986656);
        }
        if (isHoveredOrFocused()) {
            guiGraphics.fillGradient(getX(), getY() + 1, getX() + 1, (getY() + this.height) - 1, -790560, -790560);
            guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + 1, -790560, -790560);
            guiGraphics.fillGradient((getX() + this.width) - 1, getY() + 1, getX() + this.width, (getY() + this.height) - 1, -790560, -790560);
            guiGraphics.fillGradient(getX(), (getY() + this.height) - 1, getX() + this.width, getY() + this.height, -790560, -790560);
        }
    }

    public void renderPlayerPreview(GuiGraphics guiGraphics, float partialTick) {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        RenderSystem.enableScissor((int) (getX() * guiScale), (int) (Minecraft.getInstance().getWindow().getHeight() - (((getY() + this.height) - 20) * guiScale)), (int) (this.width * guiScale), (int) ((this.height - 20) * guiScale));
        ModelPreviewRenderer.renderLivingEntityPreview(getX() + (this.width / 2.0f), getY() + (this.height / 2.0f) + 24.0f, 35.0f, partialTick, this.previewEntity, RendererManager.getPlayerRenderer(), false, true);
        RenderSystem.disableScissor();
    }
}