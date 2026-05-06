package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapabilityProvider;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.client.gui.button.ModelButton;
import com.elfmcys.yesstevemodel.client.gui.button.TouhouMaidModelButton;
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.util.FileTypeUtil;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

public class TouhouMaidModelScreen extends PlayerModelScreen {

    private final EntityMaid maid;

    public TouhouMaidModelScreen(EntityMaid entityMaid) {
        this.maid = entityMaid;
    }

    @Override
    public ModelButton createModelButton(int i, int i2, boolean z, PlayerPreviewEntity previewEntity, ModelAssembly modelAssembly) {
        return new TouhouMaidModelButton(i, i2, z, previewEntity, modelAssembly, this.maid);
    }

    @Override
    public PlayerTextureScreen createTextureScreen(PlayerModelScreen modelScreen, String str, ModelAssembly modelAssembly) {
        return new TouhouMaidTextureScreen(modelScreen, str, Objects.requireNonNullElse(this.maid.getCapability(MaidCapabilityProvider.MAID_CAP).map((v0) -> {
            return v0.getModelAssembly();
        }).orElse(null), modelAssembly), this.maid);
    }

    @Override
    public ModelInfoScreen createModelInfoScreen(PlayerModelScreen modelScreen, ModelAssembly modelAssembly) {
        return new ModelInfoScreen(modelScreen, Objects.requireNonNullElse(this.maid.getCapability(MaidCapabilityProvider.MAID_CAP).map((v0) -> {
            return v0.getModelAssembly();
        }).orElse(null), modelAssembly));
    }

    @Override
    public void renderModelPreview(GuiGraphics guiGraphics, int i, int i2, float f) {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        RenderSystem.enableScissor((int) ((this.guiLeft + 5) * guiScale), (int) (Minecraft.getInstance().getWindow().getHeight() - ((this.guiTop + 200) * guiScale)), (int) (125.0d * guiScale), (int) (171.0d * guiScale));
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.guiLeft + 67, this.guiTop + 190, 70, (this.guiLeft + 67) - i, ((this.guiTop + 180) - 95) - i2, this.maid);
        RenderSystem.disableScissor();
        this.maid.getCapability(MaidCapabilityProvider.MAID_CAP).ifPresent(cap -> {
            List<FormattedCharSequence> listSplit = this.font.split(FormattedText.of(ClientModelManager.getModelContext(cap.getModelId()).map(it -> {
                Metadata metadata2 = it.getModelData().getExtraInfo();
                if (metadata2 != null) {
                    return ModelMetadataPresenter.getLocalizedModelString(it, "metadata.name", metadata2.getName());
                }
                return StringPool.EMPTY;
            }).filter(charSequence -> {
                return StringUtils.isNoneBlank(charSequence);
            }).orElse(FileTypeUtil.getNameWithoutArchiveExtension(cap.getModelId()))), 125);
            int i3 = this.guiTop + 205;
            for (FormattedCharSequence formattedCharSequence : listSplit) {
                guiGraphics.drawString(this.font, formattedCharSequence, this.guiLeft + ((135 - this.font.width(formattedCharSequence)) / 2), i3, 15986656);
                i3 += 10;
            }
        });
    }
}