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

    public TextureButton createTextureButton(int x, int y, PlayerPreviewEntity previewEntity, int textureIndex) {
        return new TextureButton(x, y, previewEntity, this.renderContext);
    }

    public void init() {
        int texIndex;
        int animIndex;
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
        for (int animSlot = 0; animSlot < 11 && (animIndex = animSlot + (this.animationCurrentPage * 11)) < this.animationKeys.size(); animSlot++) {
            String str = this.animationKeys.get(animIndex);
            int animButtonY = this.guiTop + 27 + (17 * animSlot);
            String str2 = String.format("gui.yes_steve_model.texture.button.%s", str.replaceAll("\\:", "."));
            String str3 = String.format("gui.yes_steve_model.texture.button.%s.desc", str.replaceAll("\\:", "."));
            if (I18n.exists(str2)) {
                mutableComponentLiteral = Component.translatable(str2);
            } else {
                mutableComponentLiteral = Component.literal(str);
            }
            FlatColorButton colorButton = new FlatColorButton(this.guiLeft + 5, animButtonY, 80, 16, mutableComponentLiteral, button9 -> {
                this.currentAnimation = str;
            });
            if (I18n.exists(str3)) {
                colorButton.setTooltipLines(Lists.newArrayList(new Component[]{Component.translatable(str3).withStyle(ChatFormatting.GOLD), Component.translatable("gui.yes_steve_model.texture.button.animation_name", str).withStyle(ChatFormatting.GRAY)}));
            }
            addRenderableWidget(colorButton);
        }
        for (int texSlot = 0; texSlot < 4 && (texIndex = texSlot + (this.textureCurrentPage * 4)) < this.textureMap.size(); texSlot++) {
            int texButtonX = this.guiLeft + 306 + (56 * (texSlot % 2));
            int texButtonY = this.guiTop + 5 + (104 * (texSlot / 2));
            PlayerPreviewEntity previewEntity = texturePreviewHolders[texSlot];
            previewEntity.initModelWithTexture(this.modelId, this.textureMap.getKeyAt(texIndex));
            addRenderableWidget(createTextureButton(texButtonX, texButtonY, previewEntity, texIndex));
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (getMinecraft().player == null) {
            return;
        }
        renderBackground(guiGraphics);
        guiGraphics.fillGradient(this.guiLeft, this.guiTop + 22, this.guiLeft + 90, this.guiTop + 235, -14540254, -14540254);
        guiGraphics.fillGradient(this.guiLeft + 93, this.guiTop, this.guiLeft + 299, this.guiTop + 235, -14540254, -14540254);
        guiGraphics.fillGradient(this.guiLeft + 302, this.guiTop, this.guiLeft + 420, this.guiTop + 235, -14540254, -14540254);
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        int scissorX = (int) ((this.guiLeft + 93) * guiScale);
        int height = (int) (Minecraft.getInstance().getWindow().getHeight() - ((this.guiTop + 235) * guiScale));
        int scissorWidth = (int) (206.0d * guiScale);
        int scissorHeight = (int) (235.0d * guiScale);
        if (!this.modelHolder.getAnimationStateMachine().isCurrentAnimation(this.currentAnimation)) {
            this.modelHolder.getAnimationStateMachine().setCurrentAnimation(this.currentAnimation);
        }
        renderTexturePreview(guiGraphics, scissorX, height, scissorWidth, scissorHeight, this.minecraft.getFrameTime());
        String str = String.format("%d/%d", this.textureCurrentPage + 1, this.textureMaxPage + 1);
        Font font = this.font;
        int iWidth = this.guiLeft + 302 + ((118 - this.font.width(str)) / 2);
        int pageY = this.guiTop + 223;
        Objects.requireNonNull(this.font);
        guiGraphics.drawString(font, str, iWidth, pageY - (9 / 2), 15986656);
        String str2 = String.format("%d/%d", this.animationCurrentPage + 1, this.animationMaxPage + 1);
        guiGraphics.drawString(this.font, str2, this.guiLeft + 5 + ((80 - this.font.width(str2)) / 2), this.guiTop + 218, 15986656);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderables.stream().filter(renderable -> {
            return renderable instanceof FlatColorButton;
        }).forEach(renderable2 -> {
            ((FlatColorButton) renderable2).renderTooltip(guiGraphics, this, mouseX, mouseY);
        });
    }

    public void renderTexturePreview(GuiGraphics guiGraphics, int scissorX, int scissorY, int scissorWidth, int scissorHeight, float partialTick) {
        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        this.minecraft.player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
            this.modelHolder.initModelWithTexture(this.modelId, cap.getCurrentTextureName());
            ModelPreviewRenderer.renderEntityPreview(this.guiLeft + 149.5f + 40.0f + this.offsetX, this.guiTop + 117.5f + 80.0f + this.offsetY, this.zoom, this.pitch, this.yaw, partialTick, this.modelHolder, RendererManager.getPlayerRenderer(), this.showGround);
        });
        RenderSystem.disableScissor();
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.minecraft == null || !isInPreviewArea(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            this.yaw = (float) (this.yaw + (1.5d * dragX));
            adjustPitch((float) dragY);
        }
        if (button == 1) {
            this.offsetX = (float) (this.offsetX + dragX);
            this.offsetY = (float) (this.offsetY + dragY);
            return true;
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.minecraft == null) {
            return false;
        }
        if (delta != 0.0d) {
            if (isInPreviewArea(mouseX, mouseY)) {
                adjustZoom(((float) delta) * 0.07f);
                return true;
            }
            if (isInAnimationArea(mouseX, mouseY)) {
                return scrollAnimationPage(delta);
            }
            if (isInTextureArea(mouseX, mouseY)) {
                return scrollTexturePage(delta);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean scrollTexturePage(double delta) {
        if (delta > 0.0d && this.textureCurrentPage > 0) {
            this.textureCurrentPage--;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
        }
        if (delta < 0.0d && this.textureCurrentPage < this.textureMaxPage) {
            this.textureCurrentPage++;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
            return true;
        }
        return true;
    }

    private boolean scrollAnimationPage(double delta) {
        if (delta > 0.0d && this.animationCurrentPage > 0) {
            this.animationCurrentPage--;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
        }
        if (delta < 0.0d && this.animationCurrentPage < this.animationMaxPage) {
            this.animationCurrentPage++;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
            return true;
        }
        return true;
    }

    private boolean isInPreviewArea(double mouseX, double mouseY) {
        return ((((double) (this.guiLeft + 93)) > mouseX ? 1 : (((double) (this.guiLeft + 93)) == mouseX ? 0 : -1)) < 0 && (mouseX > ((double) (this.guiLeft + 299)) ? 1 : (mouseX == ((double) (this.guiLeft + 299)) ? 0 : -1)) < 0) && ((((double) this.guiTop) > mouseY ? 1 : (((double) this.guiTop) == mouseY ? 0 : -1)) < 0 && (mouseY > ((double) (this.guiTop + 235)) ? 1 : (mouseY == ((double) (this.guiTop + 235)) ? 0 : -1)) < 0);
    }

    private boolean isInAnimationArea(double mouseX, double mouseY) {
        return ((((double) this.guiLeft) > mouseX ? 1 : (((double) this.guiLeft) == mouseX ? 0 : -1)) < 0 && (mouseX > ((double) (this.guiLeft + 90)) ? 1 : (mouseX == ((double) (this.guiLeft + 90)) ? 0 : -1)) < 0) && ((((double) (this.guiTop + 22)) > mouseY ? 1 : (((double) (this.guiTop + 22)) == mouseY ? 0 : -1)) < 0 && (mouseY > ((double) (this.guiTop + 235)) ? 1 : (mouseY == ((double) (this.guiTop + 235)) ? 0 : -1)) < 0);
    }

    private boolean isInTextureArea(double mouseX, double mouseY) {
        return ((((double) (this.guiLeft + 302)) > mouseX ? 1 : (((double) (this.guiLeft + 302)) == mouseX ? 0 : -1)) < 0 && (mouseX > ((double) (this.guiLeft + 420)) ? 1 : (mouseX == ((double) (this.guiLeft + 420)) ? 0 : -1)) < 0) && ((((double) this.guiTop) > mouseY ? 1 : (((double) this.guiTop) == mouseY ? 0 : -1)) < 0 && (mouseY > ((double) (this.guiTop + 235)) ? 1 : (mouseY == ((double) (this.guiTop + 235)) ? 0 : -1)) < 0);
    }

    private void adjustPitch(float deltaY) {
        if (this.pitch - deltaY > MAX_PITCH) {
            this.pitch = MAX_PITCH;
        } else if (this.pitch - deltaY < MIN_PITCH) {
            this.pitch = MIN_PITCH;
        } else {
            this.pitch -= deltaY;
        }
    }

    private void adjustZoom(float zoomDelta) {
        this.zoom = Mth.clamp(this.zoom + (zoomDelta * this.zoom), MIN_ZOOM, MAX_ZOOM);
    }

    public boolean isPauseScreen() {
        return false;
    }
}