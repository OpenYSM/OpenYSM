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
    public TextureButton createTextureButton(int i, int i2, PlayerPreviewEntity previewEntity, int i3) {
        return new TouhouMaidTextureButton(i, i2, previewEntity, this.maid, i3, this.renderContext);
    }

    @Override
    public void renderTexturePreview(GuiGraphics guiGraphics, int i, int i2, int i3, int i4, float f) {
        RenderSystem.enableScissor(i, i2, i3, i4);
        this.maid.getCapability(MaidCapabilityProvider.MAID_CAP).ifPresent(cap -> {
            this.modelHolder.initModelWithTexture(cap.getModelId(), cap.getCurrentTextureName());
            ModelPreviewRenderer.renderEntityPreview(this.guiLeft + 149.5f + 40.0f + this.offsetX, this.guiTop + 117.5f + 80.0f + this.offsetY, this.zoom, this.pitch, this.yaw, f, this.modelHolder, RendererManager.getPlayerRenderer(), this.showGround);
        });
        RenderSystem.disableScissor();
    }
}