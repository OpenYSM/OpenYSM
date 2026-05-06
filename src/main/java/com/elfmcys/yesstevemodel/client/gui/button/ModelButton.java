package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.client.animation.AnimationTracker;
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter;
import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SRequestSwitchModelPacket;
import com.elfmcys.yesstevemodel.util.FileTypeUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ModelButton extends Button {

    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");

    public final boolean isStarred;

    private final int backgroundColor;

    public final ModelAssembly renderContext;

    public final PlayerPreviewEntity modelIdHolder;

    private final String modelId;

    private final String modelName;

    private final String authorName;

    private final double animationDuration;

    private final boolean disablePreviewRotation;

    private final Component displayName;

    @Nullable
    private IResourceLocatable backgroundTexture;

    @Nullable
    private IResourceLocatable foregroundTexture;

    @Nullable
    private String cachedLanguage;

    @Nullable
    private List<Component> tooltipLines;

    @Nullable
    private List<Component> detailedTooltipLines;

    private long lastHoverTime;

    public ModelButton(int i, int i2, boolean z, PlayerPreviewEntity playerPreviewEntity, ModelAssembly textureRegistry) {
        super(i, i2, 52, 90, createDisplayName(playerPreviewEntity, textureRegistry), button -> {
        }, DEFAULT_NARRATION);
        this.backgroundTexture = null;
        this.foregroundTexture = null;
        this.tooltipLines = null;
        this.detailedTooltipLines = null;
        this.lastHoverTime = -1L;
        this.isStarred = z;
        this.backgroundColor = z ? 2130706432 : -12369342;
        this.renderContext = textureRegistry;
        this.modelIdHolder = playerPreviewEntity;
        this.disablePreviewRotation = textureRegistry.getModelData().getModelProperties().isDisablePreviewRotation();
        this.displayName = Component.literal(FileTypeUtil.getNameWithoutArchiveExtension(playerPreviewEntity.getModelId()));
        this.backgroundTexture = textureRegistry.getTextureRegistry().getGuiBackground() == null ? null : UploadManager.getOrCreateLocatableWithSize(textureRegistry.getTextureRegistry().getGuiBackground(), true, 200);
        this.foregroundTexture = textureRegistry.getTextureRegistry().getGuiForeground() == null ? null : UploadManager.getOrCreateLocatableWithSize(textureRegistry.getTextureRegistry().getGuiForeground(), true, 200);
        Object2ReferenceMap<String, Animation> object2ReferenceMapM3399xe6e508ff = textureRegistry.getAnimationBundle().getMainAnimations();
        if (object2ReferenceMapM3399xe6e508ff.containsKey("hover")) {
            this.modelId = "hover";
        } else {
            this.modelId = "empty";
        }
        if (object2ReferenceMapM3399xe6e508ff.containsKey("hover_fadeout")) {
            this.modelName = "hover_fadeout";
            this.animationDuration = object2ReferenceMapM3399xe6e508ff.get("hover_fadeout").animationLength * 50.0f;
        } else {
            this.modelName = "empty";
            this.animationDuration = 0.0d;
        }
        if (object2ReferenceMapM3399xe6e508ff.containsKey("focus")) {
            this.authorName = "focus";
        } else {
            this.authorName = "empty";
        }
    }

    private static MutableComponent createDisplayName(PlayerPreviewEntity previewEntity, ModelAssembly modelAssembly) {
        Metadata metadata2 = modelAssembly.getModelData().getExtraInfo();
        if (metadata2 == null || StringUtils.isBlank(metadata2.getName())) {
            return Component.literal(FileTypeUtil.getNameWithoutArchiveExtension(previewEntity.getModelId()));
        }
        return Component.literal(ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "metadata.name", metadata2.getName()));
    }

    public Component getMessage() {
        if (GeneralConfig.SHOW_MODEL_ID_FIRST.get().booleanValue()) {
            return this.displayName;
        }
        return super.getMessage();
    }

    public void onPress() {
        LocalPlayer localPlayer;
        if (!this.isStarred && (localPlayer = Minecraft.getInstance().player) != null) {
            localPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                if (NetworkHandler.isClientConnected()) {
                    if (cap.hasMolangVars(this.modelIdHolder.getModelAssembly().getModelData().getHashId())) {
                        cap.initModelWithTexture(this.modelIdHolder.getModelId(), this.modelIdHolder.getCurrentTextureName());
                        NetworkHandler.sendToServer(new C2SRequestSwitchModelPacket(cap.getModelId(), cap.getCurrentTextureName()));
                        return;
                    } else {
                        NetworkHandler.sendToServer(new C2SRequestSwitchModelPacket(this.modelIdHolder.getModelId(), this.modelIdHolder.getCurrentTextureName()));
                        return;
                    }
                }
                cap.initModelWithTexture(this.modelIdHolder.getModelId(), this.modelIdHolder.getCurrentTextureName());
            });
        }
    }

    public void renderWidget(GuiGraphics guiGraphics, int i, int i2, float f) {
        AnimationTracker c0117x8455a741Mo1262xaffeef43 = this.modelIdHolder.getAnimationStateMachine();
        if (isHovered()) {
            this.lastHoverTime = Util.getMillis();
            c0117x8455a741Mo1262xaffeef43.setPreviousAnimation(this.modelId);
        } else if (Util.getMillis() - this.lastHoverTime < this.animationDuration) {
            c0117x8455a741Mo1262xaffeef43.setPreviousAnimation(this.modelName);
        } else {
            c0117x8455a741Mo1262xaffeef43.setPreviousAnimation("empty");
        }
        if (isFocused()) {
            c0117x8455a741Mo1262xaffeef43.setQueuedAnimation(this.authorName);
        } else {
            c0117x8455a741Mo1262xaffeef43.setQueuedAnimation("empty");
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int x = getX();
        int y = getY();
        guiGraphics.fillGradient(x, y, x + this.width, y + this.height, this.backgroundColor, this.backgroundColor);
        if (this.backgroundTexture != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(this.backgroundTexture.getResourceLocation().get(), x, y, 0.0f, 0.0f, this.width, this.height, this.width, this.height);
            RenderSystem.disableBlend();
        }
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        RenderSystem.enableScissor((int) (x * guiScale), (int) (Minecraft.getInstance().getWindow().getHeight() - (((y + this.height) - 20) * guiScale)), (int) (this.width * guiScale), (int) ((this.height - 20) * guiScale));
        ModelPreviewRenderer.renderLivingEntityPreview(x + (this.width / 2.0f), y + (this.height / 2.0f) + 20.0f, 30.0f, minecraft.getFrameTime(), this.modelIdHolder, RendererManager.getPlayerRenderer(), this.disablePreviewRotation, true);
        RenderSystem.disableScissor();
        int i3 = 3500;
        if (this.foregroundTexture != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(this.foregroundTexture.getResourceLocation().get(), x, y, 3500, 0.0f, 0.0f, this.width, this.height, this.width, this.height);
            RenderSystem.disableBlend();
        }
        List listSplit = font.split(getMessage(), 45);
        if (listSplit.size() > 1) {
            guiGraphics.drawCenteredString(font, (FormattedCharSequence) listSplit.get(0), x + (this.width / 2), (y + this.height) - 19, 15986656);
            guiGraphics.drawCenteredString(font, (FormattedCharSequence) listSplit.get(1), x + (this.width / 2), (y + this.height) - 10, 15986656);
        } else {
            guiGraphics.drawCenteredString(font, getMessage(), x + (this.width / 2), (y + this.height) - 15, 15986656);
        }
        if (!this.isStarred && isHoveredOrFocused()) {
            guiGraphics.fillGradient(x, y + 1, x + 1, (y + this.height) - 1, 3500, -790560, -790560);
            guiGraphics.fillGradient(x, y, x + this.width, y + 1, 3500, -790560, -790560);
            guiGraphics.fillGradient((x + this.width) - 1, y + 1, x + this.width, (y + this.height) - 1, 3500, -790560, -790560);
            guiGraphics.fillGradient(x, (y + this.height) - 1, x + this.width, y + this.height, 3500, -790560, -790560);
        }
        if (this.isStarred) {
            guiGraphics.fillGradient(x, y, x + this.width, y + this.height, 3500, -1625152990, -1625152990);
        }
        if (minecraft.player != null) {
            minecraft.player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(cap -> {
                if (cap.containsModel(this.modelIdHolder.getModelId())) {
                    guiGraphics.blit(ICON_TEXTURE, (x + this.width) - 14, y, i3, 16.0f, 0.0f, 16, 16, 256, 256);
                }
            });
        }
    }

    public void renderTooltip(GuiGraphics guiGraphics, Screen screen, int i, int i2) {
        if (isHovered()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0f, 0.0f, 4000.0f);
            String selected = Minecraft.getInstance().getLanguageManager().getSelected();
            if (!Objects.equals(this.cachedLanguage, selected)) {
                this.cachedLanguage = selected;
                this.detailedTooltipLines = null;
                this.tooltipLines = null;
            }
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344)) {
                if (this.detailedTooltipLines == null) {
                    this.detailedTooltipLines = ModelMetadataPresenter.buildModelTooltip(this.renderContext, selected, this.modelIdHolder.getModelId(), true);
                }
                guiGraphics.renderComponentTooltip(screen.getMinecraft().font, this.detailedTooltipLines, i, i2);
            } else {
                if (this.tooltipLines == null) {
                    this.tooltipLines = ModelMetadataPresenter.buildModelTooltip(this.renderContext, selected, this.modelIdHolder.getModelId(), false);
                }
                guiGraphics.renderComponentTooltip(screen.getMinecraft().font, this.tooltipLines, i, i2);
            }
            guiGraphics.pose().popPose();
        }
    }

    public boolean clicked(double d, double d2) {
        return !this.isStarred && super.clicked(d, d2);
    }
}