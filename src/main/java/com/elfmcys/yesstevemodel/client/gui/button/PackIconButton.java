package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.resource.models.ModelPackData;
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter;
import com.elfmcys.yesstevemodel.util.FileTypeUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class PackIconButton extends Button {

    private static final ResourceLocation location = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/default_pack_icon.png");

    private final ModelPackData packData;

    public PackIconButton(int i, int i2, int i3, int i4, ModelPackData packData, Button.OnPress onPress) {
        super(i, i2, i3, i4, Component.literal(ModelMetadataPresenter.getLocalizedString(packData, "name", packData.getName())), onPress, DEFAULT_NARRATION);
        this.packData = packData;
    }

    public void renderWidget(GuiGraphics guiGraphics, int i, int i2, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + this.height, -6598176, -6598176);
        ResourceLocation location = FileTypeUtil.getPackIconLocation(this.packData.getPath());
        AbstractTexture texture = minecraft.textureManager.getTexture(location, MissingTextureAtlasSprite.getTexture());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (texture == MissingTextureAtlasSprite.getTexture()) {
            guiGraphics.blit(location, getX(), getY(), 0.0f, 0.0f, this.width, this.height, this.width, this.height);
        } else {
            guiGraphics.blit(location, getX(), getY(), 0.0f, 0.0f, this.width, this.height, this.width, this.height);
        }
        RenderSystem.disableBlend();
        List listSplit = font.split(getMessage(), 45);
        if (listSplit.size() > 1) {
            drawCenteredString(guiGraphics, font, (FormattedCharSequence) listSplit.get(0), getX() + (this.width / 2), (getY() + this.height) - 19, 5592405);
            drawCenteredString(guiGraphics, font, (FormattedCharSequence) listSplit.get(1), getX() + (this.width / 2), (getY() + this.height) - 10, 5592405);
        } else {
            drawCenteredString(guiGraphics, font, getMessage(), getX() + (this.width / 2), (getY() + this.height) - 15, 5592405);
        }
        if (isHoveredOrFocused()) {
            guiGraphics.fillGradient(getX(), getY() + 1, getX() + 1, (getY() + this.height) - 1, -1982745, -1982745);
            guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + 1, -1982745, -1982745);
            guiGraphics.fillGradient((getX() + this.width) - 1, getY() + 1, getX() + this.width, (getY() + this.height) - 1, -1982745, -1982745);
            guiGraphics.fillGradient(getX(), (getY() + this.height) - 1, getX() + this.width, getY() + this.height, -1982745, -1982745);
        }
    }

    public void renderDescription(GuiGraphics guiGraphics, Screen screen, int i, int i2) {
        String str = ModelMetadataPresenter.getLocalizedString(this.packData, "description", this.packData.getDescription());
        if (StringUtils.isBlank(str)) {
            return;
        }
        List<Component> listSingletonList = Collections.singletonList(Component.literal(str));
        if (isHovered()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0f, 0.0f, 4000.0f);
            guiGraphics.renderComponentTooltip(screen.getMinecraft().font, listSingletonList, i, i2);
            guiGraphics.pose().popPose();
        }
    }

    private static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component component, int i, int i2, int i3) {
        guiGraphics.drawString(font, component, i - (font.width(component) / 2), i2, i3, false);
    }

    private static void drawCenteredString(GuiGraphics guiGraphics, Font font, FormattedCharSequence formattedCharSequence, int i, int i2, int i3) {
        guiGraphics.drawString(font, formattedCharSequence, i - (font.width(formattedCharSequence) / 2), i2, i3, false);
    }
}