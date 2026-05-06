package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity;
import com.elfmcys.yesstevemodel.client.event.ModScreenEvent;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.AuthModelsCapability;
import com.elfmcys.yesstevemodel.capability.AuthModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.client.gui.button.*;
import com.elfmcys.yesstevemodel.client.input.PlayerModelToggleKey;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.config.ServerConfig;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.resource.models.ModelPackData;
import com.elfmcys.yesstevemodel.util.FileTypeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class PlayerModelScreen extends Screen implements IGuiWidget {

    private static final String AUTHOR_SEARCH_PREFIX = "@";

    private static final String TAG_SEARCH_PREFIX = "#";

    private final HashSet<String> hiddenModels;

    private final Map<String, ModelPackData> modelPackMap;

    private Map<String, ModelAssembly> filteredModels;

    private Map<String, ModelPackData> filteredPacks;

    private List<String> sortedModelKeys;

    private List<String> sortedPackKeys;

    public int guiLeft;

    public int guiTop;

    private int maxPage;

    private EditBox searchBox;

    private Category category;

    private static final PlayerPreviewEntity[] previewHolders = new PlayerPreviewEntity[10];

    private static final Object2IntMap<String> pageIndexMap = new Object2IntOpenHashMap();

    private static String currentPath = StringPool.EMPTY;

    static {
        for (int i = 0; i < previewHolders.length; i++) {
            previewHolders[i] = new PlayerPreviewEntity();
        }
    }

    public PlayerModelScreen() {
        super(Component.literal("YSM Player Model GUI"));
        this.hiddenModels = Sets.newHashSet();
        this.filteredModels = Maps.newHashMap();
        this.filteredPacks = Maps.newHashMap();
        this.category = Category.ALL;
        if (NetworkHandler.isClientConnected()) {
            this.hiddenModels.addAll(ServerConfig.CLIENT_NOT_DISPLAY_MODELS.get());
        }
        ClientModelManager.registerGuiWidget(this);
        this.modelPackMap = new Object2ReferenceOpenHashMap<>(ClientModelManager.getModelPackMap());
    }

    public ModelButton createModelButton(int i, int i2, boolean z, PlayerPreviewEntity previewEntity, ModelAssembly modelAssembly) {
        return new ModelButton(i, i2, z, previewEntity, modelAssembly);
    }

    public PlayerTextureScreen createTextureScreen(PlayerModelScreen other, String str, ModelAssembly modelAssembly) {
        return new PlayerTextureScreen(other, str, modelAssembly);
    }

    public ModelInfoScreen createModelInfoScreen(PlayerModelScreen other, ModelAssembly modelAssembly) {
        return new ModelInfoScreen(other, modelAssembly);
    }

    private Map<String, ModelAssembly> buildFilteredModelMap() {
        HashMap mapNewHashMap = Maps.newHashMap();
        if (StringUtils.isBlank(currentPath)) {
            mapNewHashMap.putAll(ClientModelManager.getModelAssemblyMap());
        }
        ClientModelManager.getModelAssemblyMap().forEach((str, modelAssembly) -> {
            if (str.startsWith(currentPath)) {
                mapNewHashMap.put(str, modelAssembly);
            }
            String str2 = FileTypeUtil.splitFileNameAndParentDir(str).right();
            if (StringUtils.isNotBlank(str2)) {
                ensurePackHierarchy(str2, this.modelPackMap);
            }
        });
        return mapNewHashMap;
    }

    private static void ensurePackHierarchy(String str, Map<String, ModelPackData> map) {
        if (StringUtils.isBlank(str) || !str.contains("/")) {
            return;
        }
        String[] strArrSplit = str.split("/");
        StringBuilder sb = new StringBuilder();
        for (String str2 : strArrSplit) {
            if (!str2.isEmpty()) {
                sb.append(str2).append("/");
                String string = sb.toString();
                map.putIfAbsent(string, new ModelPackData(string, FileTypeUtil.getFinalPathSegment(string), StringPool.EMPTY, null, null));
            }
        }
    }

    private Map<String, ModelPackData> buildFilteredPackMap() {
        HashMap mapNewHashMap = Maps.newHashMap();
        if (StringUtils.isBlank(currentPath)) {
            return Maps.newHashMap(this.modelPackMap);
        }
        this.modelPackMap.forEach((str, c0616x1389bc7f) -> {
            if (str.startsWith(currentPath)) {
                mapNewHashMap.put(str, c0616x1389bc7f);
            }
        });
        return mapNewHashMap;
    }

    private void refreshModelList() {
        String lowerCase;
        this.filteredModels = Maps.newHashMap();
        this.filteredPacks = Maps.newHashMap();
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        LocalPlayer localPlayer = this.minecraft.player;
        if (this.category == Category.ALL) {
            this.filteredModels = buildFilteredModelMap();
            this.filteredPacks = buildFilteredPackMap();
        }
        if (this.category == Category.AUTH) {
            localPlayer.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(cap -> {
                for (Map.Entry<String, ModelAssembly> entry : ClientModelManager.getModelAssemblyMap().entrySet()) {
                    if (cap.containsModel(entry.getKey()) || !entry.getValue().getTextureRegistry().isAuthModel()) {
                        this.filteredModels.put(entry.getKey(), entry.getValue());
                    }
                }
            });
        }
        if (this.category == Category.STAR) {
            localPlayer.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(cap2 -> {
                for (Map.Entry<String, ModelAssembly> entry : ClientModelManager.getModelAssemblyMap().entrySet()) {
                    if (cap2.containsModel(entry.getKey())) {
                        this.filteredModels.put(entry.getKey(), entry.getValue());
                    }
                }
            });
        }
        if (this.searchBox != null) {
            lowerCase = this.searchBox.getValue().toLowerCase(Locale.ENGLISH);
        } else {
            lowerCase = StringPool.EMPTY;
        }
        if (StringUtils.isBlank(lowerCase)) {
            this.filteredModels.entrySet().removeIf(entry -> {
                Pair<String, String> pair = FileTypeUtil.splitFileNameAndParentDir(entry.getKey());
                return this.hiddenModels.contains(pair.left()) || !pair.right().equals(currentPath);
            });
            this.filteredPacks.entrySet().removeIf(entry2 -> {
                return !isDirectChild(currentPath, entry2.getKey());
            });
        } else {
            String str = lowerCase;
            this.filteredModels.entrySet().removeIf(entry3 -> {
                return shouldFilterModel(FileTypeUtil.splitFileNameAndParentDir(entry3.getKey()).left(), entry3.getValue(), str);
            });
            String str2 = lowerCase;
            this.filteredPacks.entrySet().removeIf(entry4 -> {
                return shouldFilterPack(FileTypeUtil.splitFileNameAndParentDir(entry4.getKey()).left(), entry4.getValue(), str2);
            });
        }
        this.sortedModelKeys = Lists.newArrayList(this.filteredModels.keySet());
        this.sortedModelKeys.sort((v0, v1) -> {
            return v0.compareTo(v1);
        });
        this.sortedPackKeys = Lists.newArrayList(this.filteredPacks.keySet());
        this.sortedPackKeys.sort((v0, v1) -> {
            return v0.compareTo(v1);
        });
        this.maxPage = ((this.filteredModels.size() + this.filteredPacks.size()) - 1) / 10;
    }

    private boolean isDirectChild(String str, String str2) {
        String strSubstring;
        int iIndexOf;
        if (str.equals(str2)) {
            return false;
        }
        if (!StringUtils.isBlank(str)) {
            return str2.startsWith(str) && (iIndexOf = (strSubstring = str2.substring(str.length())).indexOf(47)) == strSubstring.length() - 1 && strSubstring.lastIndexOf(47) == iIndexOf;
        }
        int iIndexOf2 = str2.indexOf(47);
        return iIndexOf2 == str2.length() - 1 && str2.lastIndexOf(47) == iIndexOf2;
    }

    private boolean shouldFilterPack(String str, ModelPackData packData, String str2) {
        if (StringUtils.isBlank(str2)) {
            return false;
        }
        if (str2.startsWith(TAG_SEARCH_PREFIX)) {
            str2 = str2.substring(TAG_SEARCH_PREFIX.length());
        }
        if (str.toLowerCase(Locale.ENGLISH).contains(str2)) {
            return false;
        }
        if (packData.getTranslations() != null) {
            if (ModelMetadataPresenter.getLocalizedString(packData, "name", packData.getName()).toLowerCase(Locale.ENGLISH).contains(str2)) {
                return false;
            }
            String str3 = packData.getDescription();
            return str3 == null || !ModelMetadataPresenter.getLocalizedString(packData, "description", str3).toLowerCase(Locale.ENGLISH).contains(str2);
        }
        return true;
    }

    private boolean shouldFilterModel(String str, ModelAssembly modelAssembly, String str2) {
        if (this.hiddenModels.contains(str)) {
            return true;
        }
        if (StringUtils.isBlank(str2)) {
            return false;
        }
        if (str2.startsWith(TAG_SEARCH_PREFIX)) {
            return true;
        }
        if (str2.startsWith(AUTHOR_SEARCH_PREFIX)) {
            String strSubstring = str2.substring(AUTHOR_SEARCH_PREFIX.length());
            Metadata metadata2 = modelAssembly.getModelData().getExtraInfo();
            if (metadata2 != null) {
                return matchesAuthorSearch(modelAssembly, strSubstring, metadata2);
            }
            return true;
        }
        if (str.toLowerCase(Locale.ENGLISH).contains(str2)) {
            return false;
        }
        Metadata metadata3 = modelAssembly.getModelData().getExtraInfo();
        if (metadata3 != null) {
            if (ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "metadata.name", metadata3.getName()).toLowerCase(Locale.ENGLISH).contains(str2) || ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "metadata.tips", metadata3.getTips()).toLowerCase(Locale.ENGLISH).contains(str2)) {
                return false;
            }
            return matchesAuthorSearch(modelAssembly, str2, metadata3);
        }
        return true;
    }

    public String getParentPath(String str) {
        if (str == null || str.isEmpty()) {
            return StringPool.EMPTY;
        }
        String strSubstring = str.endsWith("/") ? str.substring(0, str.length() - 1) : str;
        int iLastIndexOf = strSubstring.lastIndexOf(47);
        if (iLastIndexOf < 0) {
            return StringPool.EMPTY;
        }
        return strSubstring.substring(0, iLastIndexOf + 1);
    }

    private boolean matchesAuthorSearch(ModelAssembly modelAssembly, String str, Metadata metadata2) {
        int i = 0;
        Iterator<AuthorInfo> it = metadata2.getAuthors().iterator();
        while (it.hasNext()) {
            if (ModelMetadataPresenter.getLocalizedModelString(modelAssembly, "metadata.authors.%d.name".formatted(Integer.valueOf(i)), it.next().getName()).toLowerCase(Locale.ENGLISH).contains(str)) {
                return false;
            }
            i++;
        }
        return true;
    }

    public void init() {
        clearWidgets();
        refreshModelList();
        if (getCurrentPage() > this.maxPage) {
            resetCurrentPage();
        }
        this.guiLeft = (this.width - 420) / 2;
        this.guiTop = (this.height - 235) / 2;
        String value = StringPool.EMPTY;
        boolean zIsFocused = false;
        if (this.searchBox != null) {
            value = this.searchBox.getValue();
            zIsFocused = this.searchBox.isFocused();
        }
        this.searchBox = new EditBox(getMinecraft().font, this.guiLeft + 144, this.guiTop + 6, 140, 16, Component.literal("YSM Search Box"));
        this.searchBox.setValue(value);
        this.searchBox.setTextColor(15986656);
        this.searchBox.setFocused(zIsFocused);
        this.searchBox.moveCursorToEnd();
        addWidget(this.searchBox);
        addRenderableWidget(new IconButton(this.guiLeft + 5, this.guiTop + 5, 20, 20, 80, 16, button -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                    ModelAssembly modelAssembly = cap.getModelAssembly();
                    if (modelAssembly.getModelData().getExtraInfo() != null) {
                        Minecraft.getInstance().setScreen(createModelInfoScreen(this, modelAssembly));
                    }
                });
            }
        })).setTooltipText("gui.yes_steve_model.model.info");
        addRenderableWidget(new IconButton(this.guiLeft + 28, this.guiTop + 5, 79, 20, 32, 16, button2 -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                    Minecraft.getInstance().setScreen(createTextureScreen(this, cap.getModelId(), cap.getModelAssembly()));
                });
            }
        }).setTooltipText("gui.yes_steve_model.model.texture"));
        addRenderableWidget(new ModIconButton(this.guiLeft + 110, this.guiTop + 5));
        if (StringUtils.isNotBlank(currentPath)) {
            addRenderableWidget(new IconButton(this.guiLeft + 110, this.guiTop + 27, 20, 20, 0, 32, button3 -> {
                navigateUp();
            }).setTooltipText("gui.back"));
        }
        addRenderableWidget(new Checkbox(this.guiLeft + 5, this.guiTop - 22, 20, 20, Component.translatable("gui.yes_steve_model.show_model_id_first"), GeneralConfig.SHOW_MODEL_ID_FIRST.get(), true) {
            public void onPress() {
                super.onPress();
                GeneralConfig.SHOW_MODEL_ID_FIRST.set(selected());
                GeneralConfig.SHOW_MODEL_ID_FIRST.save();
            }
        });
        addRenderableWidget(new IconButton(this.guiLeft + 328, this.guiTop + 5, 18, 18, 32, 0, button4 -> {
            if (this.category != Category.ALL) {
                this.category = Category.ALL;
                resetCurrentPage();
                init();
            }
        }).setTooltipText("gui.yes_steve_model.all_models"));
        addRenderableWidget(new IconButton(this.guiLeft + 308, this.guiTop + 5, 18, 18, 48, 0, button5 -> {
            if (this.category != Category.AUTH) {
                this.category = Category.AUTH;
                resetCurrentPage();
                init();
            }
        }).setTooltipText("gui.yes_steve_model.auth_models"));
        addRenderableWidget(new IconButton(this.guiLeft + 288, this.guiTop + 5, 18, 18, 0, 0, button6 -> {
            if (this.category != Category.STAR) {
                this.category = Category.STAR;
                resetCurrentPage();
                init();
            }
        }).setTooltipText("gui.yes_steve_model.star_models"));
        addRenderableWidget(new IconButton(this.guiLeft + 397, this.guiTop + 5, 18, 18, 16, 16, button7 -> {
            getMinecraft().setScreen(new ExtraPlayerConfigScreen(this));
        }).setTooltipText("gui.yes_steve_model.config"));
        addRenderableWidget(new IconButton(this.guiLeft + 377, this.guiTop + 5, 18, 18, 0, 16, button8 -> {
            ModScreenEvent.openScreen(this);
        }).setTooltipText("gui.yes_steve_model.download"));
        addRenderableWidget(new IconButton(this.guiLeft + 357, this.guiTop + 5, 18, 18, 80, 0, button9 -> {
            getMinecraft().setScreen(new OpenModelFolderScreen(this));
        }).setTooltipText("gui.yes_steve_model.open_model_folder.open"));
        addRenderableWidget(new FlatColorButton(this.guiLeft + 198, this.guiTop + 215, 52, 14, Component.translatable("gui.yes_steve_model.pre_page"), button10 -> {
            int i4 = getCurrentPage();
            if (i4 > 0) {
                setCurrentPage(i4 - 1);
                init();
            }
        }));
        addRenderableWidget(new FlatColorButton(this.guiLeft + 308, this.guiTop + 215, 52, 14, Component.translatable("gui.yes_steve_model.next_page"), button11 -> {
            int i4 = getCurrentPage();
            if (i4 < this.maxPage) {
                setCurrentPage(i4 + 1);
                init();
            }
        }));
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        LazyOptional<AuthModelsCapability> capability = this.minecraft.player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP);
        for (int i = 0; i < 10; i++) {
            int i4 = i + (getCurrentPage() * 10);
            int i2 = this.guiLeft + 143 + (55 * (i % 5));
            int i3 = this.guiTop + 28 + (93 * (i / 5));
            if (i4 < this.sortedPackKeys.size()) {
                String str = this.sortedPackKeys.get(i4);
                getPackData(str).ifPresent(value2 -> {
                    addRenderableWidget(new PackIconButton(i2, i3, 52, 90, value2, button12 -> {
                        currentPath = str;
                        resetCurrentPage();
                        init();
                    }));
                });
            }
            int size = i4 - this.sortedPackKeys.size();
            if (0 <= size && size < this.sortedModelKeys.size()) {
                String str2 = this.sortedModelKeys.get(size);
                PlayerPreviewEntity previewEntity = previewHolders[i];
                previewEntity.resetModel();
                capability.ifPresent(value3 -> {
                    ModelAssembly modelAssembly2 = this.filteredModels.get(str2);
                    boolean z = modelAssembly2.getTextureRegistry().isAuthModel() && !value3.getAuthModels().contains(str2);
                    previewEntity.initModelWithTexture(str2, modelAssembly2.getAnimationBundle().getDefaultTextureName());
                    previewEntity.getAnimationStateMachine().setCurrentAnimation(modelAssembly2.getModelData().getModelProperties().getPreviewAnimation());
                    addRenderableWidget(createModelButton(i2, i3, z, previewEntity, modelAssembly2));
                });
            }
        }
    }

    public void render(GuiGraphics guiGraphics, int i, int i2, float f) {
        renderBackground(guiGraphics);
        guiGraphics.fillGradient(this.guiLeft, this.guiTop, this.guiLeft + 135, this.guiTop + 235, -14540254, -14540254);
        guiGraphics.fillGradient(this.guiLeft + 138, this.guiTop, this.guiLeft + 420, this.guiTop + 235, -14540254, -14540254);
        guiGraphics.fillGradient(this.guiLeft + 351, this.guiTop + 7, this.guiLeft + 352, this.guiTop + 21, -790560, -790560);
        this.searchBox.render(guiGraphics, i, i2, f);
        renderModelPreview(guiGraphics, i, i2, this.minecraft.getFrameTime());
        if (this.searchBox.getValue().isEmpty() && !this.searchBox.isFocused()) {
            guiGraphics.drawString(this.font, Component.translatable("gui.yes_steve_model.search").withStyle(ChatFormatting.ITALIC), this.guiLeft + 148, this.guiTop + 10, 7829367);
        }
        String str = String.format("%d/%d", getCurrentPage() + 1, Integer.valueOf(this.maxPage + 1));
        Font font = this.font;
        int iWidth = this.guiLeft + 138 + ((282 - this.font.width(str)) / 2);
        int i3 = this.guiTop + 223;
        Objects.requireNonNull(this.font);
        guiGraphics.drawString(font, str, iWidth, i3 - (9 / 2), 15986656);
        String strVersionString = ModList.get().getModFileById(YesSteveModel.MOD_ID).versionString();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 1000.0f);
        guiGraphics.drawString(this.font, strVersionString, this.guiLeft + 2, this.guiTop + 226, ChatFormatting.DARK_GRAY.getColor().intValue());
        guiGraphics.pose().popPose();
        if (StringUtils.isNotBlank(currentPath)) {
            int i4 = 0;
            List listSplit = this.font.split(Component.literal("📂 " + currentPath).withStyle(ChatFormatting.GRAY), 270);
            Iterator it = listSplit.iterator();
            while (it.hasNext()) {
                guiGraphics.drawString(this.font, (FormattedCharSequence) it.next(), this.guiLeft + 142, this.guiTop + (((-(listSplit.size() - i4)) * 10) - 2), 15986656);
                i4++;
            }
        }
        renderSyncStatus(guiGraphics);
        super.render(guiGraphics, i, i2, f);
        this.renderables.stream().filter(renderable -> {
            return renderable instanceof IconButton;
        }).forEach(renderable2 -> {
            ((IconButton) renderable2).renderTooltip(guiGraphics, this, i, i2);
        });
        this.renderables.stream().filter(renderable3 -> {
            return renderable3 instanceof ModelButton;
        }).forEach(renderable4 -> {
            ((ModelButton) renderable4).renderTooltip(guiGraphics, this, i, i2);
        });
        this.renderables.stream().filter(renderable5 -> {
            return renderable5 instanceof PackIconButton;
        }).forEach(renderable6 -> {
            ((PackIconButton) renderable6).renderDescription(guiGraphics, this, i, i2);
        });
        if (this.searchBox.isHovered()) {
            MutableComponent mutableComponentWithStyle = Component.translatable("gui.yes_steve_model.search.tip").withStyle(ChatFormatting.GRAY);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0f, 0.0f, 4000.0f);
            guiGraphics.renderTooltip(this.font, this.font.split(mutableComponentWithStyle, 320), i, i2);
            guiGraphics.pose().popPose();
        }
    }

    private void renderSyncStatus(GuiGraphics guiGraphics) {
        MutableComponent mutableComponentLiteral;
        ClientModelManager.SyncStatus currentState = ClientModelManager.getSyncStatus();
        switch (currentState.getCurrentState()) {
            case WAITING:
                mutableComponentLiteral = Component.translatable("gui.yes_steve_model.sync_hint.waiting");
                break;
            case LOADING:
                mutableComponentLiteral = Component.translatable("gui.yes_steve_model.sync_hint.loading");
                break;
            case PREPARING:
                mutableComponentLiteral = Component.translatable("gui.yes_steve_model.sync_hint.preparing");
                break;
            case SYNCING:
                if (currentState.getSyncedModels() == 0) {
                    mutableComponentLiteral = Component.translatable("gui.yes_steve_model.sync_hint.syncing");
                    break;
                } else {
                    mutableComponentLiteral = Component.literal(String.format("%s/%s", currentState.getSyncedModels(), currentState.getTotalModels()));
                    break;
                }
            default:
                return;
        }
        int iWidth = (this.guiLeft + 414) - this.font.width(mutableComponentLiteral);
        int i = this.guiTop + 215;
        Objects.requireNonNull(this.font);
        guiGraphics.drawString(this.font, mutableComponentLiteral, iWidth, i + Math.round((14 - 9) / 2.0f), ChatFormatting.DARK_GRAY.getColor().intValue());
    }

    public void renderModelPreview(GuiGraphics guiGraphics, int i, int i2, float f) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
            RenderSystem.enableScissor((int) ((this.guiLeft + 5) * guiScale), (int) (Minecraft.getInstance().getWindow().getHeight() - ((this.guiTop + 200) * guiScale)), (int) (125.0d * guiScale), (int) (171.0d * guiScale));
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0f, 0.0f, 100.0f);
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.guiLeft + 67, this.guiTop + 190, 70, (this.guiLeft + 67) - i, ((this.guiTop + 180) - 95) - i2, localPlayer);
            guiGraphics.pose().popPose();
            RenderSystem.disableScissor();
            localPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
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

    public void resize(Minecraft minecraft, int i, int i2) {
        String value = this.searchBox.getValue();
        super.resize(minecraft, i, i2);
        this.searchBox.setValue(value);
    }

    public void tick() {
        this.searchBox.tick();
    }

    public boolean mouseClicked(double d, double d2, int i) {
        if (this.searchBox.mouseClicked(d, d2, i)) {
            setFocused(this.searchBox);
            return true;
        }
        if (this.searchBox.isFocused()) {
            this.searchBox.setFocused(false);
        }
        boolean zMouseClicked = super.mouseClicked(d, d2, i);
        if (!zMouseClicked && i == 1 && StringUtils.isNotBlank(currentPath)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            navigateUp();
            zMouseClicked = true;
        }
        return zMouseClicked;
    }

    public boolean charTyped(char c, int i) {
        if (this.searchBox == null) {
            return false;
        }
        String value = this.searchBox.getValue();
        if (this.searchBox.charTyped(c, i)) {
            if (!Objects.equals(value, this.searchBox.getValue())) {
                resetCurrentPage();
                init();
                return true;
            }
            return true;
        }
        return false;
    }

    public boolean keyPressed(int i, int i2, int i3) {
        if (handleToggleKey(i, i2, i3)) {
            return true;
        }
        boolean zIsPresent = InputConstants.getKey(i, i2).getNumericKeyValue().isPresent();
        String value = this.searchBox.getValue();
        if (zIsPresent) {
            return true;
        }
        if (!this.searchBox.keyPressed(i, i2, i3)) {
            return (this.searchBox.isFocused() && this.searchBox.isVisible() && i != 256) || super.keyPressed(i, i2, i3);
        }
        if (!Objects.equals(value, this.searchBox.getValue())) {
            resetCurrentPage();
            init();
            return true;
        }
        return true;
    }

    private boolean handleToggleKey(int i, int i2, int i3) {
        if (PlayerModelToggleKey.KEY_MAPPING.matches(i, i2) && !this.searchBox.isFocused()) {
            onClose();
            return true;
        }
        return false;
    }

    public void insertText(String str, boolean z) {
        if (z) {
            this.searchBox.setValue(str);
        } else {
            this.searchBox.insertText(str);
        }
    }

    public boolean mouseScrolled(double d, double d2, double d3) {
        if (this.minecraft == null) {
            return false;
        }
        if (d3 != 0.0d && isInModelArea(d, d2)) {
            return handleScrollPage(d3);
        }
        return super.mouseScrolled(d, d2, d3);
    }

    private boolean isInModelArea(double d, double d2) {
        return ((((double) (this.guiLeft + 143)) > d ? 1 : (((double) (this.guiLeft + 143)) == d ? 0 : -1)) < 0 && (d > ((double) (this.guiLeft + 430)) ? 1 : (d == ((double) (this.guiLeft + 430)) ? 0 : -1)) < 0) && ((((double) (this.guiTop + 25)) > d2 ? 1 : (((double) (this.guiTop + 25)) == d2 ? 0 : -1)) < 0 && (d2 > ((double) (this.guiTop + 235)) ? 1 : (d2 == ((double) (this.guiTop + 235)) ? 0 : -1)) < 0);
    }

    private void navigateUp() {
        String str2 = getParentPath(currentPath);
        if (!currentPath.equals(str2)) {
            String str = currentPath;
            currentPath = str2;
            pageIndexMap.removeInt(str);
            init();
        }
    }

    private boolean handleScrollPage(double d) {
        int i = getCurrentPage();
        if (d > 0.0d && i > 0) {
            setCurrentPage(i - 1);
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
        }
        if (d < 0.0d && i < this.maxPage) {
            setCurrentPage(i + 1);
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            init();
            return true;
        }
        return true;
    }

    public int getCurrentPage() {
        return pageIndexMap.getOrDefault(currentPath, 0);
    }

    public void setCurrentPage(int i) {
        pageIndexMap.put(currentPath, i);
    }

    public void resetCurrentPage() {
        pageIndexMap.put(currentPath, 0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onModelsLoaded(Map<String, ModelAssembly> map) {
        init();
    }

    @Override
    public void onModelsUpdated(Map<String, ModelAssembly> map) {
        init();
    }

    private Optional<ModelPackData> getPackData(String str) {
        return Optional.ofNullable(this.modelPackMap.get(str));
    }

    private enum Category {
        ALL,
        AUTH,
        STAR
    }
}