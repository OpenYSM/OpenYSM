package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapabilityProvider;
import com.elfmcys.yesstevemodel.client.gui.button.TextureButton;
import com.elfmcys.yesstevemodel.client.gui.button.TouhouMaidTextureButton;
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

public class TouhouMaidTextureScreen extends PlayerTextureScreen {

    private final EntityMaid maid;

    public TouhouMaidTextureScreen(PlayerModelScreen modelScreen, String str, ModelAssembly modelAssembly, EntityMaid entityMaid) {
        super(modelScreen, str, modelAssembly);
        this.maid = entityMaid;
    }

    @Override
    public TextureButton createTextureButton(int x, int y, PlayerPreviewEntity previewEntity, int textureIndex) {
        return new TouhouMaidTextureButton(x, y, previewEntity, this.maid, textureIndex, this.renderContext);
    }

    @Override
    public void renderTexturePreview(GuiGraphics guiGraphics, int scissorX, int scissorY, int scissorWidth, int scissorHeight, float partialTick) {
        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        this.maid.getCapability(MaidCapabilityProvider.MAID_CAP).ifPresent(cap -> {
            this.modelHolder.initModelWithTexture(cap.getModelId(), cap.getCurrentTextureName());
            ModelPreviewRenderer.renderEntityPreview(this.guiLeft + 149.5f + 40.0f + this.offsetX, this.guiTop + 117.5f + 80.0f + this.offsetY, this.zoom, this.pitch, this.yaw, partialTick, this.modelHolder, RendererManager.getPlayerRenderer(), this.showGround);
        });
        RenderSystem.disableScissor();
    }
}