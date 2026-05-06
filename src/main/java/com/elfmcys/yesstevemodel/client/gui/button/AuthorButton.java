package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.resource.models.AuthorInfo;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class AuthorButton extends Button {

    private final AuthorInfo authorInfo;

    private final ModelAssembly modelAssembly;

    private final ResourceLocation resourceLocation;

    private final int i5;

    private final List<Component> componentList;

    private int i6;

    private final Screen screen2;

    public AuthorButton(int x, int y, AuthorInfo authorInfo, ModelAssembly modelAssembly, ResourceLocation resourceLocation, int i3, Screen screen) {
        super(x, y, 70, 130, Component.empty(), button -> {
        }, DEFAULT_NARRATION);
        this.i6 = -1;
        this.authorInfo = authorInfo;
        this.modelAssembly = modelAssembly;
        this.resourceLocation = resourceLocation;
        this.i5 = i3;
        this.componentList = Lists.newArrayList();
        if (this.authorInfo != null) {
            renderTooltip(false);
        }
        this.screen2 = screen;
    }

    public static AuthorButton createAuthorButton(int i, int i2, Screen screen) {
        return new AuthorButton(i, i2, null, null, null, -1, screen);
    }

    public void renderWidget(GuiGraphics guiGraphics, int i, int i2, float f) {
        Font font = Minecraft.getInstance().font;
        if (this.authorInfo == null || this.modelAssembly == null || this.resourceLocation == null) {
            guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + this.height, -1891417534, -1891417534);
            guiGraphics.drawCenteredString(font, Component.literal("......"), getX() + (this.width / 2), getY() + (this.height / 2), ChatFormatting.GRAY.getColor().intValue());
            return;
        }
        if (isHoveredOrFocused()) {
            guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + this.height, -1892652116, -1892652116);
        } else {
            guiGraphics.fillGradient(getX(), getY(), getX() + this.width, getY() + this.height, -1891417534, -1891417534);
        }
        guiGraphics.blit(this.resourceLocation, getX() + 3, getY() + 3, 64, 64, 0.0f, 0.0f, 64, 64, 64, 64);
        String str = ModelMetadataPresenter.getLocalizedModelString(this.modelAssembly, "metadata.authors.%d.name".formatted(this.i5), this.authorInfo.getName());
        String str2 = ModelMetadataPresenter.getLocalizedModelString(this.modelAssembly, "metadata.authors.%d.role".formatted(this.i5), this.authorInfo.getRole());
        String str3 = ModelMetadataPresenter.getLocalizedModelString(this.modelAssembly, "metadata.authors.%d.comment".formatted(this.i5), this.authorInfo.getComment());
        renderScrollingString(guiGraphics, font, Component.literal(str), getX() + 2, getY() + 72, (getX() + this.width) - 2, getY() + 82, ChatFormatting.GOLD.getColor().intValue());
        guiGraphics.drawCenteredString(font, str2, getX() + 35, getY() + 82, ChatFormatting.GREEN.getColor().intValue());
        drawWrappedText(guiGraphics, Component.literal(str3), getX() + 3, getY() + 95, 64, -1);
    }

    public void drawWrappedText(GuiGraphics guiGraphics, FormattedText formattedText, int i, int i2, int i3, int i4) {
        Font font = Minecraft.getInstance().font;
        for (FormattedCharSequence formattedCharSequence : font.split(formattedText, i3)) {
            guiGraphics.drawString(font, formattedCharSequence, i, i2, i4, false);
            i2 += 9;
            if (i2 > getY() + this.height) {
                return;
            }
        }
    }

    public void refreshContactComponents(GuiGraphics guiGraphics, Screen screen, int i, int i2) {
        if (this.isHovered && !this.componentList.isEmpty()) {
            guiGraphics.renderComponentTooltip(screen.getMinecraft().font, this.componentList, i, i2);
        } else if (this.i6 != -1) {
            this.i6 = -1;
            renderTooltip(false);
        }
    }

    public boolean mouseScrolled(double d, double d2, double d3) {
        if (d3 > 0.0d) {
            if (this.i6 > 0) {
                this.i6--;
                renderTooltip(false);
                return true;
            }
            return true;
        }
        if (d3 < 0.0d) {
            if (this.i6 < this.componentList.size() - 2) {
                this.i6++;
                renderTooltip(false);
                return true;
            }
            return true;
        }
        return super.mouseScrolled(d, d2, d3);
    }

    private void renderTooltip(boolean z) {
        if (this.authorInfo == null) {
            return;
        }
        this.componentList.clear();
        for (int i = 0; i < this.authorInfo.getContact().size(); i++) {
            MutableComponent componentLiteral = Component.literal(this.authorInfo.getContact().getKeyAt(i) + ": " + this.authorInfo.getContact().getValueAt(i));
            if (i == this.i6) {
                componentLiteral.append(Component.literal(z ? " ✓" : " ◀").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
            }
            this.componentList.add(componentLiteral);
        }
        if (!this.componentList.isEmpty()) {
            this.componentList.add(Component.translatable("gui.yes_steve_model.model.info.contact.click_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public void onPress() {
        String link;
        if (this.authorInfo == null) {
            return;
        }
        int i = this.i6;
        if (i == -1) {
            i = 0;
        }
        if (i < 0 || i >= this.authorInfo.getContact().size() || (link = this.authorInfo.getContact().getValueAt(i)) == null) {
            return;
        }
        if (link.startsWith("http://") || link.startsWith("https://")) {
            Minecraft.getInstance().setScreen(new ConfirmLinkScreen(z -> {
                if (z) {
                    Util.getPlatform().openUri(link);
                }
                Minecraft.getInstance().setScreen(this.screen2);
            }, link, true));
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(link);
        if (this.i6 == -1) {
            this.i6 = 0;
        }
        renderTooltip(true);
    }
}