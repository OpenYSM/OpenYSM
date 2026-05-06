package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.AuthorButton;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModelInfoScreen extends Screen {

    private static final ResourceLocation DEFAULT_AVATAR = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/default_avatar.png");

    private static final Map<String, Component> URL_LABELS = ImmutableMap.of("home", Component.translatable("gui.yes_steve_model.url.home"), "donate", Component.translatable("gui.yes_steve_model.url.donate"));

    private final List<IResourceLocatable> textureList;

    private final PlayerModelScreen parentScreen;

    private final ModelAssembly renderContext;

    private final ServerModelInfo modelData;

    private int selectedTextureIndex;

    private int guiLeft;

    private int guiTop;

    public ModelInfoScreen(PlayerModelScreen playerModelScreen, ModelAssembly modelAssembly) {
        super(Component.literal("Model Info GUI"));
        this.textureList = new ArrayList();
        this.selectedTextureIndex = 0;
        this.parentScreen = playerModelScreen;
        this.renderContext = modelAssembly;
        this.modelData = modelAssembly.getModelData();
        initWidgets();
    }

    private void initWidgets() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        this.textureList.clear();
        List<AuthorInfo> authorInfo = this.modelData.getExtraInfo().getAuthors();
        Map<String, OuterFileTexture> avatars = this.renderContext.getTextureRegistry().getAuthorAvatars();
        for (int i = 0; i < authorInfo.size(); i++) {
            OuterFileTexture avatar = avatars.get(authorInfo.get(i).getName());
            if (avatar != null) {
                textureManager.register(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "avatars/" + i), avatar);
                this.textureList.add(UploadManager.getOrCreateLocatable(avatar, true));
            } else {
                this.textureList.add(null);
            }
        }
    }

    public void init() {
        clearWidgets();
        this.guiLeft = (this.width - 420) / 2;
        this.guiTop = (this.height - 235) / 2;
        Metadata metadata = this.modelData.getExtraInfo();
        List<AuthorInfo> authorInfos = metadata.getAuthors();
        if (authorInfos.size() <= this.selectedTextureIndex) {
            this.selectedTextureIndex = 0;
        }
        int i = 0;
        while (i < 5) {
            int i2 = this.selectedTextureIndex + i;
            if (i2 >= authorInfos.size()) {
                while (i < 5) {
                    addRenderableWidget(AuthorButton.createAuthorButton(this.guiLeft + 25 + (75 * i), this.guiTop + 15, this));
                    i++;
                }
            } else {
                AuthorInfo authorInfo = authorInfos.get(i2);
                IResourceLocatable resourceLocatable = this.textureList.get(i2);
                addRenderableWidget(new AuthorButton(this.guiLeft + 25 + (75 * i), this.guiTop + 15, authorInfo, this.renderContext, resourceLocatable != null ? resourceLocatable.getResourceLocation().get() : DEFAULT_AVATAR, i2, this));
            }
            i++;
        }
        addRenderableWidget(new FlatColorButton(this.guiLeft + 2, this.guiTop + 25, 18, 100, Component.literal("<"), button -> {
            this.selectedTextureIndex = Math.max(0, this.selectedTextureIndex - 5);
            init();
        }).setTooltipText("gui.yes_steve_model.pre_page"));
        addRenderableWidget(new FlatColorButton(this.guiLeft + 25 + 375, this.guiTop + 25, 18, 100, Component.literal(">"), button2 -> {
            this.selectedTextureIndex += 5;
            init();
        }).setTooltipText("gui.yes_steve_model.next_page"));
        int i3 = this.guiTop + 150;
        for (int i4 = 0; i4 < Math.min(metadata.getLink().size(), 2); i4++) {
            String str = metadata.getLink().getKeyAt(i4);
            String str2 = metadata.getLink().getValueAt(i4);
            Component component = URL_LABELS.get(str);
            if (component == null) {
                component = Component.literal(str);
            }
            addRenderableWidget(new FlatColorButton(this.guiLeft + 310, i3, 85, 20, component, button3 -> {
                openUrl(str2);
            }));
            i3 += 25;
        }
        addRenderableWidget(new FlatColorButton(this.guiLeft + 310, i3, 85, 20, Component.translatable("gui.yes_steve_model.model.return"), button4 -> {
            getMinecraft().setScreen(this.parentScreen);
        }));
    }

    private void openUrl(@Nullable String str) {
        if (str != null && StringUtils.isNoneBlank(str)) {
            getMinecraft().setScreen(new ConfirmLinkScreen(z -> {
                if (z) {
                    Util.getPlatform().openUri(str);
                }
                getMinecraft().setScreen(this);
            }, str, true));
        }
    }

    public void render(GuiGraphics guiGraphics, int i, int i2, float f) {
        renderBackground(guiGraphics);
        guiGraphics.fillGradient(this.guiLeft + 25, this.guiTop + 150, this.guiLeft + 305, this.guiTop + 220, -1889838245, -1889838245);
        Metadata metadata2 = this.modelData.getExtraInfo();
        if (metadata2 != null) {
            int i3 = 0;
            Iterator it = this.font.split(Component.literal(ModelMetadataPresenter.getLocalizedModelString(this.renderContext, "metadata.tips", metadata2.getTips())), 270).iterator();
            while (it.hasNext()) {
                guiGraphics.drawString(this.font, (FormattedCharSequence) it.next(), this.guiLeft + 30, this.guiTop + 154 + i3, -1);
                Objects.requireNonNull(this.font);
                i3 += 9;
                Objects.requireNonNull(this.font);
                if (i3 > 9 * 7) {
                    break;
                }
            }
        }
        super.render(guiGraphics, i, i2, f);
        this.renderables.stream().filter(renderable -> {
            return renderable instanceof AuthorButton;
        }).forEach(renderable2 -> {
            ((AuthorButton) renderable2).refreshContactComponents(guiGraphics, this, i, i2);
        });
    }

    public boolean isPauseScreen() {
        return false;
    }
}