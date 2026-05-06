package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.config.ExtraPlayerRenderConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public class ExtraPlayerRenderScreen extends Screen {

    private static final char RESET_KEY = 'r';

    private int mouseStartX;

    private int mouseStartY;

    private float rotationX;

    private float rotationY;

    private boolean isDragging;

    private boolean isRightDragging;

    private int offsetX;

    private int offsetY;

    public ExtraPlayerRenderScreen() {
        super(Component.literal("YSM Extra Player Render Config GUI"));
        this.isDragging = false;
        this.isRightDragging = false;
        this.offsetX = 5;
        this.offsetY = 1;
        this.mouseStartX = ExtraPlayerRenderConfig.PLAYER_POS_X.get().intValue();
        this.mouseStartY = ExtraPlayerRenderConfig.PLAYER_POS_Y.get().intValue();
        this.rotationX = ExtraPlayerRenderConfig.PLAYER_SCALE.get().floatValue();
        this.rotationY = ExtraPlayerRenderConfig.PLAYER_YAW_OFFSET.get().floatValue();
        if (PauseScreenButtonBuilder.isServerConnected()) {
            this.offsetX = 16;
            this.offsetY = 0;
        }
    }

    public void init() {
        clearWidgets();
        int i = -30;
        if (PauseScreenButtonBuilder.isServerConnected()) {
            addRenderableWidget(Button.builder(Component.translatable("controls.reset"), button -> {
                resetTransform();
            }).bounds((this.width / 2) - 50, this.height - 35, 100, 30).build());
            i = -60;
        }
        MutableComponent mutableComponentTranslatable = Component.translatable("gui.yes_steve_model.hide_or_show");
        int iWidth = this.font.width(mutableComponentTranslatable) + 24;
        addRenderableWidget(new Checkbox((this.width - iWidth) / 2, this.height + i, iWidth, 20, mutableComponentTranslatable, ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER.get().booleanValue(), true) {
            public void onPress() {
                super.onPress();
                ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER.set(Boolean.valueOf(selected()));
            }
        });
    }

    public void render(GuiGraphics guiGraphics, int i, int i2, float f) {
        int i3 = this.mouseStartX;
        int i4 = this.mouseStartY;
        int i5 = (int) (i3 + (this.rotationX));
        int i6 = (int) (i4 + (this.rotationX * 2.0f));
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, (-500.0f) - ((50.0f * this.rotationX) / 40.0f));
        guiGraphics.vLine((this.width / 2) - 1, -2, this.height + 2, -1610612737);
        guiGraphics.hLine(-2, this.width + 2, (this.height / 2) - 1, -1610612737);
        guiGraphics.vLine(10, -2, this.height + 2, -1610612737);
        guiGraphics.vLine(this.width - 10, -2, this.height + 2, -1610612737);
        guiGraphics.hLine(-2, this.width + 2, 10, -1610612737);
        guiGraphics.hLine(-2, this.width + 2, this.height - 10, -1610612737);
        guiGraphics.vLine(i3, i4, i6, -65536);
        guiGraphics.vLine(i5, i4, i6, -65536);
        guiGraphics.hLine(i3, i5, i4, -65536);
        guiGraphics.hLine(i3, i5, i6, -65536);
        guiGraphics.fillGradient(i3, i4, i5, i6, 1342177279, 1342177279);
        guiGraphics.fillGradient(i3 - this.offsetX, i4 - this.offsetX, i3 + this.offsetX, i4 + this.offsetX, -16711777, -16711777);
        guiGraphics.fillGradient(i5 - this.offsetX, i6 - this.offsetX, i5 + this.offsetX, i6 + this.offsetX, -16777057, -16777057);
        int i7 = 15;
        for (FormattedCharSequence formattedCharSequence : this.font.split(Component.translatable("gui.yes_steve_model.extra_player_render.tips"), 500)) {
            guiGraphics.drawString(this.font, formattedCharSequence, (this.width - 15) - this.font.width(formattedCharSequence), i7, 16777215);
            i7 += 10;
        }
        guiGraphics.pose().popPose();
        if (getMinecraft().player != null && !ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER.get().booleanValue()) {
            ModelPreviewRenderer.renderPlayerOverlay(guiGraphics, getMinecraft().player, this.mouseStartX, this.mouseStartY, this.rotationX, this.rotationY, -500, this.minecraft.getFrameTime());
        }
        super.render(guiGraphics, i, i2, f);
    }

    public boolean mouseClicked(double d, double d2, int i) {
        boolean z = ((double) (this.mouseStartX - this.offsetX)) < d && d < ((double) (this.mouseStartX + this.offsetX));
        boolean z2 = ((double) (this.mouseStartY - this.offsetX)) < d2 && d2 < ((double) (this.mouseStartY + this.offsetX));
        if (i == 0 && z && z2) {
            this.isDragging = true;
        }
        int i2 = (int) (this.mouseStartX + (this.rotationX));
        int i3 = (int) (this.mouseStartY + (this.rotationX * 2.0f));
        boolean z3 = ((double) (i2 - this.offsetX)) < d && d < ((double) (i2 + this.offsetX));
        boolean z4 = ((double) (i3 - this.offsetX)) < d2 && d2 < ((double) (i3 + this.offsetX));
        if (i == 0 && z3 && z4) {
            this.isRightDragging = true;
        }
        return super.mouseClicked(d, d2, i);
    }

    public boolean mouseReleased(double d, double d2, int i) {
        this.isDragging = false;
        this.isRightDragging = false;
        return super.mouseReleased(d, d2, i);
    }

    public boolean mouseDragged(double d, double d2, int i, double d3, double d4) {
        if (this.isRightDragging) {
            this.rotationX = (float) Math.min(d - this.mouseStartX, (d2 - this.mouseStartY) / 2.0d);
            return true;
        }
        if (this.isDragging) {
            this.mouseStartX = (int) d;
            this.mouseStartY = (int) d2;
            return true;
        }
        if (i == this.offsetY) {
            this.rotationY += (float) (d3 * 2.0d);
            return true;
        }
        return false;
    }

    public boolean charTyped(char c, int i) {
        if (Character.toLowerCase(c) == RESET_KEY && hasAltDown()) {
            resetTransform();
        }
        return super.charTyped(c, i);
    }

    private void resetTransform() {
        this.mouseStartX = 10;
        this.mouseStartY = 10;
        this.rotationX = 40.0f;
        this.rotationY = 5.0f;
    }

    public void onClose() {
        ExtraPlayerRenderConfig.PLAYER_POS_X.set(Integer.valueOf(this.mouseStartX));
        ExtraPlayerRenderConfig.PLAYER_POS_Y.set(Integer.valueOf(this.mouseStartY));
        ExtraPlayerRenderConfig.PLAYER_SCALE.set(Double.valueOf(this.rotationX));
        ExtraPlayerRenderConfig.PLAYER_YAW_OFFSET.set(Double.valueOf(this.rotationY));
        super.onClose();
    }
}