package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.event.AnimationLockEvent;
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.resource.models.ModelProperties;
import com.elfmcys.yesstevemodel.client.gui.button.AnimationSlider;
import com.elfmcys.yesstevemodel.client.gui.button.ConfigCheckBox;
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.client.gui.button.FlatIconButton;
import com.elfmcys.yesstevemodel.client.input.AnimationRouletteKey;
import com.elfmcys.yesstevemodel.client.input.ExtraAnimationKey;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.config.ServerConfig;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.CheckboxConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RadioConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RangeConfig;
import com.elfmcys.yesstevemodel.molang.parser.ParseException;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SPlayAnimationPacket;
import com.elfmcys.yesstevemodel.network.message.C2SRequestExecuteMolangPacket;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.Consumer;

public class AnimationRouletteScreen extends Screen {

    private static final String SUBMENU_PREFIX = "#";

    private static final String RETURN_KEY = "#return";

    private static final String CONFIG_TITLE_FORMAT = "properties.extra_animation_buttons.%s.config_forms.%d.title";

    private static final String CONFIG_DESC_FORMAT = "properties.extra_animation_buttons.%s.config_forms.%d.description";

    private static final String CONFIG_LABEL_FORMAT = "properties.extra_animation_buttons.%s.config_forms.%d.labels.%d";

    private static final int ITEMS_PER_PAGE = 8;

    private static final LinkedList<Pair<String, Integer>> navigationStack = Lists.newLinkedList();

    private static String lastModelId = StringPool.EMPTY;

    private int centerX;

    private int centerY;

    private int hoveredIndex;

    private int hoveredConfigIndex;

    private ExtraAnimationButtons currentConfigGroup;

    private Pair<String, Integer> currentNavEntry;

    private int configScrollOffset;

    private int maxConfigScroll;

    @Nullable
    private FlatColorButton scrollUpButton;

    @Nullable
    private FlatColorButton scrollDownButton;

    private final OrderedStringMap<String, String> currentProperties;

    private final Map<String, ExtraAnimationButtons> renderGroups;

    private final Map<String, OrderedStringMap<String, String>> textProperties;

    private final ModelProperties timingConfig;

    private final AnimatableEntity<?> animatableModel;

    private final ModelAssembly renderContext;

    public AnimationRouletteScreen(Map<String, ExtraAnimationButtons> map, Map<String, OrderedStringMap<String, String>> map2, ModelAssembly modelAssembly, AnimatableEntity<?> animatableEntity) {
        super(Component.literal("Animation Roulette GUI"));
        this.hoveredIndex = -1;
        this.hoveredConfigIndex = -1;
        this.currentConfigGroup = null;
        this.configScrollOffset = 0;
        this.maxConfigScroll = 0;
        this.renderContext = modelAssembly;
        this.timingConfig = modelAssembly.getModelData().getModelProperties();
        this.animatableModel = animatableEntity;
        this.textProperties = map2;
        this.renderGroups = map;
        this.currentNavEntry = navigationStack.peekLast();
        if (this.currentNavEntry != null && this.textProperties.containsKey(this.currentNavEntry.getLeft())) {
            this.currentProperties = this.textProperties.get(this.currentNavEntry.getLeft());
            return;
        }
        this.currentProperties = this.timingConfig.getExtraAnimation();
        navigationStack.clear();
        navigationStack.add(MutablePair.of(StringPool.EMPTY, Integer.valueOf(this.currentNavEntry == null ? 0 : this.currentNavEntry.getRight().intValue())));
        this.currentNavEntry = navigationStack.peekLast();
    }

    public AnimationRouletteScreen(String str, ModelAssembly modelAssembly, AnimatableEntity<?> animatableEntity) {
        super(Component.literal("Animation Roulette GUI"));
        this.hoveredIndex = -1;
        this.hoveredConfigIndex = -1;
        this.currentConfigGroup = null;
        this.configScrollOffset = 0;
        this.maxConfigScroll = 0;
        this.renderContext = modelAssembly;
        this.timingConfig = modelAssembly.getModelData().getModelProperties();
        this.animatableModel = animatableEntity;
        this.textProperties = this.timingConfig.getExtraAnimationClassify();
        this.renderGroups = this.timingConfig.getExtraAnimationButtons();
        if (!lastModelId.equals(str)) {
            navigationStack.clear();
            lastModelId = str;
        }
        if (navigationStack.isEmpty()) {
            navigationStack.add(MutablePair.of(StringPool.EMPTY, 0));
        }
        this.currentNavEntry = navigationStack.peekLast();
        if (this.textProperties.containsKey(this.currentNavEntry.getLeft())) {
            this.currentProperties = this.textProperties.get(this.currentNavEntry.getLeft());
            return;
        }
        this.currentProperties = this.timingConfig.getExtraAnimation();
        navigationStack.clear();
        navigationStack.add(MutablePair.of(StringPool.EMPTY, this.currentNavEntry.getRight()));
        this.currentNavEntry = navigationStack.peekLast();
    }

    public void init() {
        clearWidgets();
        this.centerX = (this.width / 2) - 70;
        this.centerY = (this.height / 2) - 8;
        if (this.currentProperties.size() < (this.currentNavEntry.getRight().intValue() * 8) + 1) {
            this.currentNavEntry.setValue(0);
        }
        if (this.currentProperties.size() <= this.hoveredIndex) {
            this.hoveredIndex = 0;
        }
        if (this.animatableModel.getEntity() instanceof Player) {
            addRenderableWidget(new FlatColorButton(this.centerX - 20, this.centerY - 10, 40, 20, Component.empty(), button -> {
                AnimationLockEvent.toggleLock();
            }) {
                @NotNull
                public Component getMessage() {
                    if (AnimationLockEvent.isLocked()) {
                        return Component.translatable("gui.yes_steve_model.roulette.lock_on");
                    }
                    return Component.translatable("gui.yes_steve_model.roulette.lock_off");
                }
            });
        } else {
            addRenderableWidget(new FlatColorButton(this.centerX - 20, this.centerY - 10, 40, 20, Component.translatable("gui.yes_steve_model.roulette.stop"), button2 -> {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket.createWithIndex(this.animatableModel.getEntity().getId()));
                onClose();
            }));
        }
        addRenderableWidget(new FlatColorButton(this.centerX + 125, this.centerY - 102, 30, 30, Component.literal("<"), button3 -> {
            previousPage();
        }));
        addRenderableWidget(new FlatColorButton(this.centerX + 240, this.centerY - 102, 30, 30, Component.literal(">"), button4 -> {
            nextPage();
        }));
        addRenderableWidget(new FlatColorButton(this.centerX + 125, this.centerY - 70, 145, 22, Component.translatable("gui.yes_steve_model.model.return"), button5 -> {
            navigateBack();
        }));
        if (this.currentConfigGroup != null) {
            this.scrollUpButton = new FlatColorButton(this.centerX + 242, this.centerY - 46, 28, 60, Component.literal("↑"), button6 -> {
                scrollConfigUp(50);
                if (this.configScrollOffset == 0 && this.scrollUpButton != null) {
                    this.scrollUpButton.active = false;
                }
                if (this.scrollDownButton != null) {
                    this.scrollDownButton.active = true;
                }
            });
            this.scrollDownButton = new FlatColorButton(this.centerX + 242, this.centerY + 50, 28, 60, Component.literal("↓"), button7 -> {
                scrollConfigDown(50);
                if (this.configScrollOffset == this.maxConfigScroll && this.scrollDownButton != null) {
                    this.scrollDownButton.active = false;
                }
                if (this.scrollUpButton != null) {
                    this.scrollUpButton.active = true;
                }
            });
            addRenderableWidget(this.scrollUpButton);
            addRenderableWidget(this.scrollDownButton);
            int[] iArr = {-46};
            int[] iArr2 = {0};
            for (AbstractConfig config : this.currentConfigGroup.getConfigForms()) {
                renderConfigFormItem(config, iArr, iArr2);
            }
        }
    }

    private void renderConfigFormItem(AbstractConfig abstractConfig, int[] iArr, int[] iArr2) {
        if (abstractConfig instanceof CheckboxConfig config) {
            executeExpression(abstractConfig.getValue(), str -> {
                this.minecraft.execute(() -> {
                    addRenderableWidget(createCheckbox(config, str, iArr, iArr2));
                    iArr[0] = iArr[0] + 14;
                    iArr2[0] = iArr2[0] + 1;
                    this.maxConfigScroll = Math.max(0, iArr[0] - 110);
                });
            });
        }
        if (abstractConfig instanceof RangeConfig config) {
            executeExpression(abstractConfig.getValue(), str2 -> {
                this.minecraft.execute(() -> {
                    addRenderableWidget(createSlider(config, str2, iArr, iArr2));
                    iArr[0] = iArr[0] + 17;
                    iArr2[0] = iArr2[0] + 1;
                    this.maxConfigScroll = Math.max(0, iArr[0] - 110);
                });
            });
        }
        if (abstractConfig instanceof RadioConfig config) {
            executeExpression(abstractConfig.getValue(), str3 -> {
                this.minecraft.execute(() -> {
                    renderRadioGroup(config, str3, iArr, iArr2);
                });
            });
        }
    }

    private void renderRadioGroup(RadioConfig radioConfig, String str, int[] iArr, int[] iArr2) {
        int iRound = Math.round(parseFloatValue(str));
        OrderedStringMap<String, String> orderedStringMap = radioConfig.getLabels();
        if (iRound < 0 || orderedStringMap.size() < iRound) {
            iRound = 0;
        }
        int iMax = 0;
        int i = 0;
        Iterator<String> it = orderedStringMap.getKeys().iterator();
        while (it.hasNext()) {
            iMax = Math.max(iMax, this.font.width(ModelMetadataPresenter.getLocalizedModelString(this.renderContext, CONFIG_LABEL_FORMAT.formatted(this.currentConfigGroup.getId(), Integer.valueOf(iArr2[0]), Integer.valueOf(i)), it.next())) + 16);
            i++;
        }
        if (iMax == 0) {
            iMax = 115;
        }
        int iMax2 = Math.max(1, 115 / iMax);
        String str2 = ModelMetadataPresenter.getLocalizedModelString(this.renderContext, CONFIG_TITLE_FORMAT.formatted(this.currentConfigGroup.getId(), Integer.valueOf(iArr2[0])), radioConfig.getTitle());
        String str3 = ModelMetadataPresenter.getLocalizedModelString(this.renderContext, CONFIG_DESC_FORMAT.formatted(this.currentConfigGroup.getId(), Integer.valueOf(iArr2[0])), radioConfig.getDescription());
        MutableComponent mutableComponentLiteral = Component.literal(str2);
        Tooltip tooltipCreate = Tooltip.create(Component.literal(str3));
        int size = ((((orderedStringMap.size() - 1) / iMax2) + 1) * 14) + 14;
        FlatIconButton iconButton = new FlatIconButton(this.centerX + 125, this.centerY + iArr[0], size, mutableComponentLiteral);
        iconButton.setTooltip(tooltipCreate);
        addRenderableOnly(iconButton);
        int i2 = iArr[0] + 14;
        int i3 = 0;
        while (i3 < orderedStringMap.size()) {
            MutableComponent mutableComponentLiteral2 = Component.literal(ModelMetadataPresenter.getLocalizedModelString(this.renderContext, CONFIG_LABEL_FORMAT.formatted(this.currentConfigGroup.getId(), Integer.valueOf(iArr2[0]), Integer.valueOf(i3)), orderedStringMap.getKeyAt(i3)));
            String str4 = orderedStringMap.getValueAt(i3);
            boolean z = iRound == i3;
            int iRound2 = Math.round(110.0f / iMax2);
            ConfigCheckBox configCheckBox = new ConfigCheckBox(this.centerX + 127 + (iRound2 * (i3 % iMax2)), this.centerY + i2, iRound2, mutableComponentLiteral2, bool -> {
                executeExpression(str4, null);
                if (!GeckoLibCache.isRoamingVariableAssignment(str4) && NetworkHandler.isClientConnected() && !ServerConfig.LOW_BANDWIDTH_USAGE.get().booleanValue()) {
                    NetworkHandler.sendToServer(new C2SRequestExecuteMolangPacket(str4, this.animatableModel.getEntity().getId()));
                }
                init();
            });
            configCheckBox.setStateTriggered(z);
            addRenderableWidget(configCheckBox);
            if (i3 % iMax2 == iMax2 - 1) {
                i2 += 14;
            }
            i3++;
        }
        iArr[0] = iArr[0] + size + 3;
        iArr2[0] = iArr2[0] + 1;
        this.maxConfigScroll = Math.max(0, iArr[0] - 110);
    }

    @NotNull
    private AnimationSlider createSlider(RangeConfig rangeConfig, String str, int[] iArr, int[] iArr2) {
        String str2 = ModelMetadataPresenter.getLocalizedModelString(this.renderContext, CONFIG_TITLE_FORMAT.formatted(this.currentConfigGroup.getId(), Integer.valueOf(iArr2[0])), rangeConfig.getTitle());
        String str3 = ModelMetadataPresenter.getLocalizedModelString(this.renderContext, CONFIG_DESC_FORMAT.formatted(this.currentConfigGroup.getId(), Integer.valueOf(iArr2[0])), rangeConfig.getDescription());
        MutableComponent mutableComponentLiteral = Component.literal(str2);
        Tooltip tooltipCreate = Tooltip.create(Component.literal(str3));
        AnimationSlider animationSlider = new AnimationSlider(this.centerX + 125, this.centerY + iArr[0], mutableComponentLiteral, parseFloatValue(str), this.animatableModel, rangeConfig.getValue(), rangeConfig.getStep(), rangeConfig.getMin(), rangeConfig.getMax());
        animationSlider.setTooltip(tooltipCreate);
        return animationSlider;
    }

    @NotNull
    private ConfigCheckBox createCheckbox(CheckboxConfig checkboxConfig, String str, int[] iArr, int[] iArr2) throws NumberFormatException {
        String str3 = ModelMetadataPresenter.getLocalizedModelString(this.renderContext, CONFIG_TITLE_FORMAT.formatted(this.currentConfigGroup.getId(), Integer.valueOf(iArr2[0])), checkboxConfig.getTitle());
        String str4 = ModelMetadataPresenter.getLocalizedModelString(this.renderContext, CONFIG_DESC_FORMAT.formatted(this.currentConfigGroup.getId(), Integer.valueOf(iArr2[0])), checkboxConfig.getDescription());
        MutableComponent mutableComponentLiteral = Component.literal(str3);
        Tooltip tooltipCreate = Tooltip.create(Component.literal(str4));
        float f2 = parseFloatValue(str);
        ConfigCheckBox configCheckBox = new ConfigCheckBox(this.centerX + 125, this.centerY + iArr[0], mutableComponentLiteral, bool -> {
            String str2 = checkboxConfig.getValue() + "=" + (bool.booleanValue() ? "1" : "0");
            executeExpression(str2, null);
            if (!GeckoLibCache.isRoamingVariableAssignment(str2) && NetworkHandler.isClientConnected() && !ServerConfig.LOW_BANDWIDTH_USAGE.get().booleanValue()) {
                NetworkHandler.sendToServer(new C2SRequestExecuteMolangPacket(str2, this.animatableModel.getEntity().getId()));
            }
        }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int i, int i2, float f) {
                guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), -280804798);
                super.renderWidget(guiGraphics, i, i2, f);
            }
        };
        configCheckBox.setStateTriggered(f2 > 0.0f);
        configCheckBox.setTooltip(tooltipCreate);
        return configCheckBox;
    }

    private float parseFloatValue(String str) throws NumberFormatException {
        float f;
        if ("null".equals(str)) {
            f = 0.0f;
        } else if (NumberUtils.isParsable(str)) {
            f = Float.parseFloat(str);
        } else if (BooleanUtils.toBooleanObject(str) != null) {
            f = BooleanUtils.toBoolean(str) ? 1.0f : 0.0f;
        } else {
            f = 0.0f;
        }
        return f;
    }

    public void render(GuiGraphics guiGraphics, int i, int i2, float f) {
        int i3;
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.yes_steve_model.roulette.path", StringUtils.joinWith(" > ", navigationStack.stream().map((v0) -> {
            return v0.getLeft();
        }).toArray())), this.centerX + 195, this.centerY - 100, 16777215);
        renderRadialBackground(guiGraphics.pose(), i, i2);
        renderRadialButtons(guiGraphics);
        renderPageInfo(guiGraphics);
        for (Renderable renderable : this.renderables) {
            if (!(renderable instanceof ISpecialWidget)) {
                renderable.render(guiGraphics, i, i2, f);
            }
        }
        guiGraphics.enableScissor(0, this.centerY - 46, this.width, this.centerY + 110);
        if (i2 < this.centerY - 46 || this.centerY + 110 < i2) {
            i3 = -1000;
        } else {
            i3 = i2 + this.configScrollOffset;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, -this.configScrollOffset, 0.0f);
        for (Renderable renderable2 : this.renderables) {
            if (renderable2 instanceof ISpecialWidget) {
                renderable2.render(guiGraphics, i, i3, f);
            }
        }
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
        renderHoverTooltip(guiGraphics, i, i3);
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int i, int i2) {
        if (-1 < this.hoveredIndex && this.hoveredIndex < this.currentProperties.size()) {
            String str = ModelMetadataPresenter.getLocalizedModelString(this.renderContext, "properties.extra_animation.%s.desc".formatted(this.currentProperties.getKeyAt(this.hoveredIndex)), StringPool.EMPTY);
            if (StringUtils.isNotBlank(str)) {
                guiGraphics.renderTooltip(this.font, this.font.split(Component.literal(str), 240), i, i2);
            }
        }
    }

    private void executeExpression(String str, @Nullable Consumer<String> consumer) {
        try {
            this.animatableModel.executeExpression(GeckoLibCache.parseSimpleExpression(str), true, false, consumer);
        } catch (ParseException e) {
            YesSteveModel.LOGGER.error(e);
        }
    }

    private void renderPageInfo(GuiGraphics guiGraphics) {
        guiGraphics.fill(this.centerX + 157, this.centerY - 87, this.centerX + 238, this.centerY - 72, 0, -822083584);
        guiGraphics.drawCenteredString(this.font, String.format("%d/%d", Integer.valueOf(this.currentNavEntry.getRight().intValue() + 1), Integer.valueOf(((this.currentProperties.size() - 1) / 8) + 1)), this.centerX + 197, this.centerY - 83, ChatFormatting.AQUA.getColor().intValue());
    }

    public boolean mouseScrolled(double d, double d2, double d3) {
        if (d3 < 0.0d) {
            if (d < this.centerX + 110) {
                nextPage();
                return true;
            }
            scrollConfigDown(20);
            return true;
        }
        if (d3 <= 0.0d) {
            return false;
        }
        if (d < this.centerX + 110) {
            previousPage();
            return true;
        }
        scrollConfigUp(20);
        return true;
    }

    private void previousPage() {
        this.currentNavEntry.setValue(Integer.valueOf(Math.max(0, this.currentNavEntry.getRight().intValue() - 1)));
    }

    private void nextPage() {
        if (this.currentProperties.size() > (this.currentNavEntry.getRight().intValue() + 1) * 8) {
            this.currentNavEntry.setValue(Integer.valueOf(this.currentNavEntry.getRight().intValue() + 1));
        }
    }

    private void scrollConfigUp(int i) {
        this.configScrollOffset = Math.max(0, this.configScrollOffset - i);
    }

    private void scrollConfigDown(int i) {
        this.configScrollOffset = Math.min(this.maxConfigScroll, this.configScrollOffset + i);
    }

    public boolean mouseClicked(double d, double d2, int i) {
        if (-1 < this.hoveredIndex && this.hoveredIndex < this.currentProperties.size()) {
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            String str = this.currentProperties.getKeyAt(this.hoveredIndex);
            if (RETURN_KEY.equals(str)) {
                navigateBack();
            } else if (str.startsWith(SUBMENU_PREFIX)) {
                navigateToSubmenu(str);
            } else {
                playAnimation(str);
            }
        } else if (-1 < this.hoveredConfigIndex && this.hoveredConfigIndex < this.currentProperties.size()) {
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            String str2 = this.currentProperties.getValueAt(this.hoveredConfigIndex);
            if (str2.startsWith(SUBMENU_PREFIX)) {
                String strSubstring = str2.substring(SUBMENU_PREFIX.length());
                if (this.renderGroups.containsKey(strSubstring)) {
                    showConfigGroup(strSubstring);
                }
            }
        }
        for (GuiEventListener guiEventListener : children()) {
            double d3 = d2;
            if (guiEventListener instanceof ISpecialWidget) {
                d3 = d2 + this.configScrollOffset;
            }
            if (guiEventListener.mouseClicked(d, d3, i)) {
                setFocused(guiEventListener);
                if (i == 0) {
                    setDragging(true);
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int i, int i2, int i3) {
        if (AnimationRouletteKey.KEY_ROULETTE.matches(i, i2) && AnimationRouletteKey.KEY_ROULETTE.getKeyModifier().isActive(null)) {
            onClose();
            return true;
        }
        return super.keyPressed(i, i2, i3);
    }

    private void showConfigGroup(String str) {
        this.currentConfigGroup = this.renderGroups.get(str);
        this.configScrollOffset = 0;
        this.maxConfigScroll = 0;
        init();
    }

    private void playAnimation(String str) {
        LocalPlayer localPlayer = getMinecraft().player;
        if (NetworkHandler.isClientConnected()) {
            Pair<String, Integer> pairPeekLast = navigationStack.peekLast();
            String str2 = StringPool.EMPTY;
            if (pairPeekLast != null && StringUtils.isNotBlank(pairPeekLast.getLeft())) {
                str2 = pairPeekLast.getLeft();
            }
            Entity entity = this.animatableModel.getEntity();
            if (entity instanceof Player) {
                NetworkHandler.CHANNEL.sendToServer(new C2SPlayAnimationPacket(this.hoveredIndex, str2));
            } else {
                NetworkHandler.CHANNEL.sendToServer(new C2SPlayAnimationPacket(this.hoveredIndex, str2, entity.getId()));
            }
        } else if (localPlayer != null) {
            localPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                cap.requestModelSwitch(str);
            });
        }
        if (localPlayer != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG.get().booleanValue()) {
            localPlayer.sendSystemMessage(Component.translatable("message.yes_steve_model.model.animation_roulette.play", str));
        }
        getMinecraft().setScreen(null);
    }

    private void navigateToSubmenu(String str) {
        if (navigationStack.size() > 5) {
            LocalPlayer localPlayer = getMinecraft().player;
            if (localPlayer != null) {
                localPlayer.sendSystemMessage(Component.translatable("gui.yes_steve_model.roulette.too_long"));
                return;
            }
            return;
        }
        String strSubstring = str.substring(SUBMENU_PREFIX.length());
        if (this.textProperties.get(strSubstring) != null) {
            navigationStack.addLast(MutablePair.of(strSubstring, 0));
            getMinecraft().setScreen(new AnimationRouletteScreen(this.renderGroups, this.textProperties, this.renderContext, this.animatableModel));
        }
    }

    private void navigateBack() {
        if (navigationStack.size() > 1) {
            navigationStack.removeLast();
            getMinecraft().setScreen(new AnimationRouletteScreen(this.renderGroups, this.textProperties, this.renderContext, this.animatableModel));
            return;
        }
        getMinecraft().setScreen(null);
    }

    public static void setInitialSubmenu(String str) {
        navigationStack.clear();
        navigationStack.addLast(MutablePair.of(StringPool.EMPTY, 0));
        navigationStack.addLast(MutablePair.of(str, 0));
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void renderRadialButtons(GuiGraphics guiGraphics) {
        float f = 0.3926991f;
        int size = this.currentProperties.size() - (this.currentNavEntry.getRight().intValue() * 8);
        for (int i = 0; i < Math.min(8, size); i++) {
            int iIntValue = i + (this.currentNavEntry.getRight().intValue() * 8);
            int iCos = (int) (this.centerX + (65 * Mth.cos(f)));
            float fSin = this.centerY + (65 * Mth.sin(f));
            Objects.requireNonNull(this.font);
            int i2 = (int) (fSin - (9.0f / 2.0f));
            String str = this.currentProperties.getValueAt(iIntValue);
            boolean zStartsWith = this.currentProperties.getKeyAt(iIntValue).startsWith(SUBMENU_PREFIX);
            if (str.startsWith(SUBMENU_PREFIX)) {
                String strSubstring = str.substring(SUBMENU_PREFIX.length());
                if (this.renderGroups.containsKey(strSubstring)) {
                    str = this.renderGroups.get(strSubstring).getName();
                    int iCos2 = (int) (this.centerX + (35 * Mth.cos(f)));
                    float fSin2 = this.centerY + (35 * Mth.sin(f));
                    Objects.requireNonNull(this.font);
                    guiGraphics.drawCenteredString(this.font, Component.literal("⚙").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), iCos2, (int) (fSin2 - (9.0f / 2.0f)), 16777215);
                }
            }
            if (StringUtils.isNoneBlank(str)) {
                renderWrappedLabel(guiGraphics, Component.literal(ModelMetadataPresenter.getLocalizedModelString(this.renderContext, "properties.extra_animation.%s".formatted(this.currentProperties.getKeyAt(iIntValue)), str)), iCos, i2, zStartsWith);
            } else {
                guiGraphics.drawCenteredString(this.font, Component.literal(ModelMetadataPresenter.getLocalizedModelString(this.renderContext, "properties.extra_animation.%s".formatted(this.currentProperties.getKeyAt(iIntValue)), String.valueOf(iIntValue))), iCos, i2 - 8, 15986656);
            }
            if (this.currentNavEntry.getRight().intValue() == 0 && navigationStack.size() == 1) {
                renderKeyBindings(guiGraphics, iIntValue, iCos, i2);
            }
            f += 0.7853982f;
        }
    }

    private void renderKeyBindings(GuiGraphics guiGraphics, int i, int i2, int i3) {
        MutableComponent mutableComponentWithStyle = Component.literal("[ ").withStyle(ChatFormatting.YELLOW);
        KeyMapping keyMapping = ExtraAnimationKey.KEY_MAPPINGS.get(i);
        if (keyMapping.getKey() == InputConstants.UNKNOWN) {
            mutableComponentWithStyle.append(Component.translatable("key.yes_steve_model.extra_animation.none"));
        } else {
            mutableComponentWithStyle.append(keyMapping.getTranslatedKeyMessage());
        }
        mutableComponentWithStyle.append(" ]");
        guiGraphics.drawCenteredString(this.font, mutableComponentWithStyle, i2, i3 + 4, 15986656);
    }

    private void renderWrappedLabel(GuiGraphics guiGraphics, MutableComponent mutableComponent, int i, int i2, boolean z) {
        Objects.requireNonNull(this.font);
        if (z) {
            mutableComponent = mutableComponent.withStyle(ChatFormatting.RED);
        }
        List listSplit = this.font.split(mutableComponent, 50);
        int size = (i2 - (listSplit.size() * 9)) + 2;
        if (this.currentNavEntry.getRight().intValue() != 0 || navigationStack.size() > 1) {
            size += 9;
        }
        Iterator it = listSplit.iterator();
        while (it.hasNext()) {
            guiGraphics.drawCenteredString(this.font, (FormattedCharSequence) it.next(), i, size, 15986656);
            size += 9;
        }
    }

    private void renderRadialBackground(PoseStack poseStack, int i, int i2) {
        if (this.currentProperties.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4fPose = poseStack.last().pose();
        float fAtan2 = (float) Mth.atan2(i2 - this.centerY, i - this.centerX);
        if (fAtan2 < 0.0f) {
            fAtan2 = 6.2831855f + fAtan2;
        }
        float fSqrt = Mth.sqrt(Mth.square(i2 - this.centerY) + Mth.square(i - this.centerX));
        boolean flag = false;
        boolean z = false;
        for (int i3 = 0; i3 < Math.min(8, this.currentProperties.size() - (this.currentNavEntry.getRight().intValue() * 8)); i3++) {
            float f = ((6.2831855f / 8) * i3) + 0.034906585f;
            float f2 = ((6.2831855f / 8) * (i3 + 1)) - 0.034906585f;
            int iIntValue = i3 + (this.currentNavEntry.getRight().intValue() * 8);
            boolean zStartsWith = this.currentProperties.getValueAt(iIntValue).startsWith(SUBMENU_PREFIX);
            flag = checkRadialHover(f, fAtan2, f2, fSqrt, flag, zStartsWith, i3, builder, matrix4fPose);
            boolean z2 = f < fAtan2 && fAtan2 < f2 && 20.0f < fSqrt && fSqrt < 50.0f;
            if (zStartsWith) {
                if (z2) {
                    drawRadialSegment(builder, matrix4fPose, 15.0f, 50.0f, f, f2, -268382465);
                    z = true;
                    this.hoveredConfigIndex = iIntValue;
                } else {
                    drawRadialSegment(builder, matrix4fPose, 25.0f, 50.0f, f, f2, 1879101183);
                }
            }
        }
        if (!flag) {
            this.hoveredIndex = -1;
        }
        if (!z) {
            this.hoveredConfigIndex = -1;
        }
        tesselator.end();
        RenderSystem.disableBlend();
    }

    private boolean checkRadialHover(float f, float f2, float f3, float f4, boolean z, boolean z2, int i, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
        boolean z3 = f < f2 && f2 < f3 && 50.0f < f4 && f4 < 100.0f;
        if (z3) {
            z = true;
            this.hoveredIndex = i + (this.currentNavEntry.getRight().intValue() * 8);
        }
        if (z3 && i < this.currentProperties.size()) {
            if (z2) {
                drawRadialSegment(bufferBuilder, matrix4f, 50.0f, 115.0f, f, f3, -251678464);
                drawRadialSegment(bufferBuilder, matrix4f, 25.0f, 50.0f, f, f3, -1879048192);
            } else {
                drawRadialSegment(bufferBuilder, matrix4f, 25.0f, 115.0f, f, f3, -251678464);
            }
        } else {
            drawRadialSegment(bufferBuilder, matrix4f, 25.0f, 105.0f, f, f3, -1879048192);
        }
        return z;
    }

    private void drawRadialSegment(BufferBuilder bufferBuilder, Matrix4f matrix4f, float f, float f2, float f3, float f4, int i) {
        float f5 = ((i >> 24) & 255) / 255.0f;
        float f6 = ((i >> 16) & 255) / 255.0f;
        float f7 = ((i >> 8) & 255) / 255.0f;
        float f8 = (i & 255) / 255.0f;
        bufferBuilder.vertex(matrix4f, this.centerX + (f2 * Mth.cos(f3)), this.centerY + (f2 * Mth.sin(f3)), 0.0f).color(f6, f7, f8, f5).endVertex();
        bufferBuilder.vertex(matrix4f, this.centerX + (f * Mth.cos(f3)), this.centerY + (f * Mth.sin(f3)), 0.0f).color(f6, f7, f8, f5).endVertex();
        bufferBuilder.vertex(matrix4f, this.centerX + (f * Mth.cos(f4)), this.centerY + (f * Mth.sin(f4)), 0.0f).color(f6, f7, f8, f5).endVertex();
        bufferBuilder.vertex(matrix4f, this.centerX + (f2 * Mth.cos(f4)), this.centerY + (f2 * Mth.sin(f4)), 0.0f).color(f6, f7, f8, f5).endVertex();
    }
}