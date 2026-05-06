package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.client.gui.button.IconButton;
import com.elfmcys.yesstevemodel.client.gui.button.TextureButton;
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerTextureScreen extends Screen {

    private static final String HIDDEN_PREFIX = "——";

    private static final float MAX_ZOOM = 360.0f;

    private static final float MIN_ZOOM = 18.0f;

    private static final float MAX_PITCH = 90.0f;

    private static final float MIN_PITCH = -90.0f;

    private static final PlayerPreviewEntity[] texturePreviewHolders = new PlayerPreviewEntity[4];

    private static final int LEFT_MOUSE_BUTTON = 0;

    private static final int RIGHT_MOUSE_BUTTON = 1;

    public final PlayerPreviewEntity modelHolder;

    public final ModelAssembly renderContext;

    private final PlayerModelScreen parentScreen;

    private final String modelId;

    private final OrderedStringMap<String, ? extends AbstractTexture> textureMap;

    private final List<String> animationKeys;

    private String currentAnimation;

    private int textureMaxPage;

    private int textureCurrentPage;

    private int animationMaxPage;

    private int animationCurrentPage;

    public int guiLeft;

    public int guiTop;

    public float offsetX;

    public float offsetY;

    public float zoom;

    public float yaw;

    public float pitch;

    public boolean showGround;

    static {
        for (int i = 0; i < texturePreviewHolders.length; i++) {
            texturePreviewHolders[i] = new PlayerPreviewEntity();
        }
    }

    public PlayerTextureScreen(PlayerModelScreen modelScreen, String str, ModelAssembly modelAssembly) {
        super(Component.literal("Player Texture GUI"));
        this.currentAnimation = StringPool.EMPTY;
        this.offsetX = 0.0f;
        this.offsetY = -60.0f;
        this.zoom = 80.0f;
        this.yaw = 165.0f;
        this.pitch = -5.0f;
        this.showGround = true;
        this.modelHolder = new PlayerPreviewEntity();
        for (PlayerPreviewEntity c0685xf513e8bf : texturePreviewHolders) {
            c0685xf513e8bf.resetModel();
            c0685xf513e8bf.getAnimationStateMachine().setCurrentAnimation("idle");
        }
        this.parentScreen = modelScreen;
        this.modelId = str;
        this.renderContext = modelAssembly;
        this.textureMap = modelAssembly.getAnimationBundle().getTextures();
        this.animationKeys = new ArrayList(modelAssembly.getAnimationBundle().getMainAnimations().keySet());
        this.animationKeys.removeIf(str2 -> {
            return str2.startsWith(HIDDEN_PREFIX);
        });
        this.animationKeys.sort((v0, v1) -> {
            return v0.compareTo(v1);
        });
    }

    public TextureButton createTextureButton(int i, int i2, PlayerPreviewEntity previewEntity, int i3) {
        return new TextureButton(i, i2, previewEntity, this.renderContext);
    }

    public void init() {
        int i;
        int i2;
        MutableComponent mutableComponentLiteral;
        clearWidgets();
        this.guiLeft = (this.width - 420) / 2;
        this.guiTop = (this.height - 235) / 2;
        this.textureMaxPage = (this.textureMap.size() - 1) / 4;
        this.animationMaxPage = (this.animationKeys.size() - 1) / 11;
        if (this.textureCurrentPage > this.textureMaxPage) {
            this.textureCurrentPage = 0;
        }
        if (this.animationCurrentPage > this.animationMaxPage) {
            this.animationCurrentPage = 0;
        }
        addRenderableWidget(new FlatColorButton(this.guiLeft + 5, this.guiTop, 80, 18, Component.translatable("gui.yes_steve_model.model.return"), button -> {
            getMinecraft().setScreen(this.parentScreen);
        }));
        addRenderableWidget(new IconButton(this.guiLeft + 281, this.guiTop + 2, 16, 16, 64, 16, button2 -> {
            this.currentAnimation = "idle";
        }).setTooltipText("gui.yes_steve_model.model.stop"));
        addRenderableWidget(new IconButton(this.guiLeft + 263, this.guiTop + 2, 16, 16, 48, 16, button3 -> {
            this.offsetX = 0.0f;
            this.offsetY = -60.0f;
            this.zoom = 80.0f;
            this.yaw = 165.0f;
            this.pitch = -5.0f;
        }).setTooltipText("gui.yes_steve_model.model.reset"));
        addRenderableWidget(new IconButton(this.guiLeft + 245, this.guiTop + 2, 16, 16, 64, 0, button4 -> {
            this.showGround = !this.showGround;
        }).setTooltipText("gui.yes_steve_model.model.ground"));
        addRenderableWidget(new FlatColorButton(this.guiLeft + 321, this.guiTop + 213, 18, 18, Component.literal("<"), button5 -> {
            if (this.textureCurrentPage > 0) {
                this.textureCurrentPage--;
                init();
            }
        }));
        addRenderableWidget(new FlatColorButton(this.guiLeft + 383, this.guiTop + 213, 18, 18, Component.literal(">"), button6 -> {
            if (this.textureCurrentPage < this.textureMaxPage) {
                this.textureCurrentPage++;
                init();
            }
        }));
        addRenderableWidget(new FlatColorButton(this.guiLeft + 11, this.guiTop + 214, 16, 16, Component.literal("<"), button7 -> {
            if (this.animationCurrentPage > 0) {
                this.animationCurrentPage--;
                init();
            }
        }));
        addRenderableWidget(new FlatColorButton(this.guiLeft + 63, this.guiTop + 214, 16, 16, Component.literal(">"), button8 -> {
            if (this.animationCurrentPage < this.animationMaxPage) {
                this.animationCurrentPage++;
                init();
            }
        }));
        for (int i3 = 0; i3 < 11 && (i2 = i3 + (this.animationCurrentPage * 11)) < this.animationKeys.size(); i3++) {
            String str = this.animationKeys.get(i2);
            int i4 = this.guiTop + 27 + (17 * i3);
            String str2 = String.format("gui.yes_steve_model.texture.button.%s", str.replaceAll("\\:", "."));
            String str3 = String.format("gui.yes_steve_model.texture.button.%s.desc", str.replaceAll("\\:", "."));
            if (I18n.exists(str2)) {
                mutableComponentLiteral = Component.translatable(str2);
            } else {
                mutableComponentLiteral = Component.literal(str);
            }
            FlatColorButton colorButton = new FlatColorButton(this.guiLeft + 5, i4, 80, 16, mutableComponentLiteral, button9 -> {
                this.currentAnimation = str;
            });
            if (I18n.exists(str3)) {
                colorButton.setTooltipLines(Lists.newArrayList(new Component[]{Component.translatable(str3).withStyle(ChatFormatting.GOLD), Component.translatable("gui.yes_steve_model.texture.button.animation_name", str).withStyle(ChatFormatting.GRAY)}));
            }
            addRenderableWidget(colorButton);
        }
        for (int i5 = 0; i5 < 4 && (i = i5 + (this.textureCurrentPage * 4)) < this.textureMap.size(); i5++) {
            int i6 = this.guiLeft + 306 + (56 * (i5 % 2));
            int i7 = this.guiTop + 5 + (104 * (i5 / 2));
            PlayerPreviewEntity previewEntity = texturePreviewHolders[i5];
            previewEntity.initModelWithTexture(this.modelId, this.textureMap.getKeyAt(i));
            addRenderableWidget(createTextureButton(i6, i7, previewEntity, i));
        }
    }

    public void render(GuiGraphics guiGraphics, int i, int i2, float f) {
        if (getMinecraft().player == null) {
            return;
        }
        renderBackground(guiGraphics);
        guiGraphics.fillGradient(this.guiLeft, this.guiTop + 22, this.guiLeft + 90, this.guiTop + 235, -14540254, -14540254);
        guiGraphics.fillGradient(this.guiLeft + 93, this.guiTop, this.guiLeft + 299, this.guiTop + 235, -14540254, -14540254);
        guiGraphics.fillGradient(this.guiLeft + 302, this.guiTop, this.guiLeft + 420, this.guiTop + 235, -14540254, -14540254);
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        int i3 = (int) ((this.guiLeft + 93) * guiScale);
        int height = (int) (Minecraft.getInstance().getWindow().getHeight() - ((this.guiTop + 235) * guiScale));
        int i4 = (int) (206.0d * guiScale);
        int i5 = (int) (235.0d * guiScale);
        if (!this.modelHolder.getAnimationStateMachine().isCurrentAnimation(this.currentAnimation)) {
            this.modelHolder.getAnimationStateMachine().setCurrentAnimation(this.currentAnimation);
        }
        renderTexturePreview(guiGraphics, i3, height, i4, i5, this.minecraft.getFrameTime());
        String str = String.format("%d/%d", this.textureCurrentPage + 1, this.textureMaxPage + 1);
        Font font = this.font;
        int iWidth = this.guiLeft + 302 + ((118 - this.font.width(str)) / 2);
        int i6 = this.guiTop + 223;
        Objects.requireNonNull(this.font);
        guiGraphics.drawString(font, str, iWidth, i6 - (9 / 2), 15986656);
        String str2 = String.format("%d/%d", this.animationCurrentPage + 1, this.animationMaxPage + 1);
        guiGraphics.drawString(this.font, str2, this.guiLeft + 5 + ((80 - this.font.width(str2)) / 2), this.guiTop + 218, 15986656);
        super.render(guiGraphics, i, i2, f);
        this.renderables.stream().filter(renderable -> {
            return renderable instanceof FlatColorButton;
        }).forEach(renderable2 -> {
            ((FlatColorButton) renderable2).renderTooltip(guiGraphics, this, i, i2);
        });
    }

    public void renderTexturePreview(GuiGraphics guiGraphics, int i, int i2, int i3, int i4, float f) {
        RenderSystem.enableScissor(i, i2, i3, i4);
        this.minecraft.player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
            this.modelHolder.initModelWithTexture(this.modelId, cap.getCurrentTextureName());
            ModelPreviewRenderer.renderEntityPreview(this.guiLeft + 149.5f + 40.0f + this.offsetX, this.guiTop + 117.5f + 80.0f + this.offsetY, this.zoom, this.pitch, this.yaw, f, this.modelHolder, RendererManager.getPlayerRenderer(), this.showGround);
        });
        RenderSystem.disableScissor();
    }

    public boolean mouseDragged(double d, double d2, int i, double d3, double d4) {
        if (this.minecraft == null || !isInPreviewArea(d, d2)) {
            return false;
        }
        if (i == 0) {
            this.yaw = (float) (this.yaw + (1.5d * d3));
            adjustPitch((float) d4);
        }
        if (i == 1) {
            this.offsetX = (float) (this.offsetX + d3);
            this.offsetY = (float) (this.offsetY + d4);
            return true;
        }
        return true;
    }

    public boolean mouseScrolled(double d, double d2, double d3) {
        if (this.minecraft == null) {
            return false;
        }
        if (d3 != 0.0d) {
            if (isInPreviewArea(d, d2)) {
                adjustZoom(((float) d3) * 0.07f);
                return true;
            }
            if (isInAnimationArea(d, d2)) {
                return scrollAnimationPage(d3);
            }
            if (isInTextureArea(d, d2)) {
                return scrollTexturePage(d3);
            }
        }
        return super.mouseScrolled(d, d2, d3);
    }

    private boolean scrollTexturePage(double d) {
        if (d > 0.0d && this.textureCurrentPage > 0) {
            this.textureCurrentPage--;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
        }
        if (d < 0.0d && this.textureCurrentPage < this.textureMaxPage) {
            this.textureCurrentPage++;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
            return true;
        }
        return true;
    }

    private boolean scrollAnimationPage(double d) {
        if (d > 0.0d && this.animationCurrentPage > 0) {
            this.animationCurrentPage--;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
        }
        if (d < 0.0d && this.animationCurrentPage < this.animationMaxPage) {
            this.animationCurrentPage++;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
            return true;
        }
        return true;
    }

    private boolean isInPreviewArea(double d, double d2) {
        return ((((double) (this.guiLeft + 93)) > d ? 1 : (((double) (this.guiLeft + 93)) == d ? 0 : -1)) < 0 && (d > ((double) (this.guiLeft + 299)) ? 1 : (d == ((double) (this.guiLeft + 299)) ? 0 : -1)) < 0) && ((((double) this.guiTop) > d2 ? 1 : (((double) this.guiTop) == d2 ? 0 : -1)) < 0 && (d2 > ((double) (this.guiTop + 235)) ? 1 : (d2 == ((double) (this.guiTop + 235)) ? 0 : -1)) < 0);
    }

    private boolean isInAnimationArea(double d, double d2) {
        return ((((double) this.guiLeft) > d ? 1 : (((double) this.guiLeft) == d ? 0 : -1)) < 0 && (d > ((double) (this.guiLeft + 90)) ? 1 : (d == ((double) (this.guiLeft + 90)) ? 0 : -1)) < 0) && ((((double) (this.guiTop + 22)) > d2 ? 1 : (((double) (this.guiTop + 22)) == d2 ? 0 : -1)) < 0 && (d2 > ((double) (this.guiTop + 235)) ? 1 : (d2 == ((double) (this.guiTop + 235)) ? 0 : -1)) < 0);
    }

    private boolean isInTextureArea(double d, double d2) {
        return ((((double) (this.guiLeft + 302)) > d ? 1 : (((double) (this.guiLeft + 302)) == d ? 0 : -1)) < 0 && (d > ((double) (this.guiLeft + 420)) ? 1 : (d == ((double) (this.guiLeft + 420)) ? 0 : -1)) < 0) && ((((double) this.guiTop) > d2 ? 1 : (((double) this.guiTop) == d2 ? 0 : -1)) < 0 && (d2 > ((double) (this.guiTop + 235)) ? 1 : (d2 == ((double) (this.guiTop + 235)) ? 0 : -1)) < 0);
    }

    private void adjustPitch(float f) {
        if (this.pitch - f > MAX_PITCH) {
            this.pitch = MAX_PITCH;
        } else if (this.pitch - f < MIN_PITCH) {
            this.pitch = MIN_PITCH;
        } else {
            this.pitch -= f;
        }
    }

    private void adjustZoom(float f) {
        this.zoom = Mth.clamp(this.zoom + (f * this.zoom), MIN_ZOOM, MAX_ZOOM);
    }

    public boolean isPauseScreen() {
        return false;
    }
}