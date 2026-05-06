package com.elfmcys.yesstevemodel.resource.pojo;

import java.util.*;

public class RawYsmModel {
    public String modelId;
    public int formatVersion;
    public RawMetadata metadata = new RawMetadata();
    public RawProperties properties = new RawProperties();
    public RawMainEntity mainEntity = new RawMainEntity();
    public Map<String, RawSubEntity> vehicles = new LinkedHashMap<>();
    public Map<String, RawSubEntity> projectiles = new LinkedHashMap<>();
    public Map<String, RawDataFile> soundFiles = new LinkedHashMap<>();
    public Map<String, RawDataFile> functionFiles = new LinkedHashMap<>();
    public Map<String, RawLanguageFile> languageFiles = new LinkedHashMap<>(); // locale -> key/value
    public RawFooter footer = new RawFooter();

    public static class RawMainEntity {
        public RawGeometry mainModel;
        public RawGeometry armModel;
        public Map<String, RawTexture> textures = new LinkedHashMap<>();
        public Map<String, RawAnimationFile> animationFiles = new LinkedHashMap<>();
        public Map<String, RawAnimationController> animationControllers = new LinkedHashMap<>();
    }

    public static class RawSubEntity {
        public String identifier;
        public String[] matchIds;
        public RawGeometry model;
        public Map<String, RawTexture> textures = new LinkedHashMap<>();
        public Map<String, RawAnimationFile> animationFiles = new LinkedHashMap<>();
        public Map<String, RawAnimationController> animationControllers = new LinkedHashMap<>();
    }

    public static class RawGeometry {
        public int modelType; // 1=main, 2=arm, 3=arrow
        public String identifier = "";
        public String sha256 = "";
        public float textureWidth = 64f;
        public float textureHeight = 64f;
        public float visibleBoundsWidth;
        public float visibleBoundsHeight;
        public float[] visibleBoundsOffset;
        public float unkFloat1, unkFloat2;

        // 這三個值在解析後直接讀取但未使用，且通常為0。
        // 作用暫時不明確
        public int footerPad1, footerPad2, footerPad3;

        public List<RawBone> bones = new ArrayList<>();
    }

    public static class RawBone {
        public String name;
        public String parentName;
        public float[] pivot = new float[3];
        public float[] rotation = new float[3];
        public int unkPad1, unkPad2, unkPad3, unkPad4, unkPad5;
        public List<RawCube> cubes = new ArrayList<>();
    }

    public static class RawCube {
        public List<RawFace> faces = new ArrayList<>();
        // 作用暫時不明確，且通常為0
        public int unkInt1, unkInt2, unkInt3;
    }

    public static class RawFace {
        public float[] normal = new float[3];
        public float[][] positions = new float[4][3]; //4 個頂點的 xyz 座標
        public float[] u = new float[4];
        public float[] v = new float[4];
    }

    public static class RawAnimationFile {
        // 1 - main -> animation-main
        // 2 - arm -> animation-arm
        // 3 - extra -> animation-extra
        // 4 - tac -> animation-tac
        // 5 - arrow -> animation-arrow
        // 6 - carryon -> animation-carryon
        // 7 - parcool -> animation-parcool
        // 8 - swem -> animation-swem
        // 9 - slashblade -> animation-slashblade
        // 10 - tlm -> animation-tlm
        // 11 - fp_arm -> animation-fp_arm
        // 12 - immersive_melodies -> animation-immersive_melodies
        // 13 - irons_spell_books -> animation-irons_spell_books
        public int animType;
        public String fileHash;
        public Map<String, RawAnimation> animations = new LinkedHashMap<>();
    }

    public static class RawAnimation {
        public String name;
        public float length;
        public int loopMode;
        // Float or String
        public Object blendWeight;

        // 作用暫時不明確，且通常為0
        public int unkInt1, unkInt2, unkInt4;

        public List<RawBoneAnimation> boneAnimations = new ArrayList<>();
        public List<RawTimelineEvent> timelineEvents = new ArrayList<>();
        public List<RawSoundEffect> soundEffects = new ArrayList<>();
    }

    public static class RawBoneAnimation {
        public String boneName;
        public List<RawKeyframe> rotation = new ArrayList<>();
        public List<RawKeyframe> position = new ArrayList<>();
        public List<RawKeyframe> scale = new ArrayList<>();
    }

    public static class RawKeyframe {
        public float timestamp;
        public int interpolationMode; // 0: linear, 1: catmullrom
        public Object[] postData = new Object[3];
        public Object[] preData = new Object[3];
        public boolean hasPreData;
    }

    public static class RawTimelineEvent {
        public float timestamp;
        public List<String> events = new ArrayList<>();
    }

    public static class RawSoundEffect {
        public String effectName;
        public float timestamp;
    }

    public static class RawTexture {
        public String name;
        public String hash;
        public int width;
        public int height;
        public int imageFormat;
        public byte[] data;

        /**
         * 缺省值為1（表示正常紋理）
         * 在舊格式模型（≤15）中常出現0
         * 現代格式（≥26）幾乎全為1
         */
        public int unknownFlag;
        public List<SubTexture> subTextures = new ArrayList<>();

        public static class SubTexture {
            public String hash;
            // 1=法線貼圖，2=高光貼圖
            public int specularType;
            public int width;
            public int height;

            // 子紋理圖像格式
            public int imageFormat;
            public byte[] data;

            // 作用暫時不明確，且通常為1
            public int unknownFlag;
        }
    }

    public static class RawAnimationController {
        public String name;
        public String hash;
        public String animationName;
        public String initialState;

        // 作用暫時不明確，且通常為0，可能係歷史遺留問題
        public int legacyUnknownInt;
        public List<RawControllerState> states = new ArrayList<>();
    }

    public static class RawControllerState {
        public String name;
        public Map<String, String> animations = new LinkedHashMap<>();
        public Map<String, String> transitions = new LinkedHashMap<>();
        public List<String> onEntry = new ArrayList<>();
        public List<String> onExit = new ArrayList<>();
        public List<String> soundEffects = new ArrayList<>();
        public float blendTransitionValue;
        public boolean blendViaShortestPath;
        public Map<Float, Float> blendTransitions = new LinkedHashMap<>();
    }

    public static class RawMetadata {
        public String name = "";
        public String tips = "";
        public String licenseType = "";
        public String licenseDescription = "";
        public List<Author> authors = new ArrayList<>();
        public Map<String, String> links = new LinkedHashMap<>();
        public List<RawImage> extraAvatars = new ArrayList<>();

        public static class Author {
            public String name = "";
            public String role = "";
            public String comment = "";
            public Map<String, String> contacts = new LinkedHashMap<>();
            public String avatar = "";
            public RawImage avatarImage = null;
        }
    }

    public static class RawImage {
        public String name;
        public byte[] data;
        public int width;
        public int height;
        public int format;

        // 作用暫時不明確，且通常為1
        public int unknownFlag;
        public boolean isPng;
    }

    public static class RawProperties {
        public String sha256 = "";
        public float widthScale = 0.7f;
        public float heightScale = 0.7f;
        public String defaultTexture = "default";
        public String previewAnimation = "";

        public boolean isFree = false;
        public boolean renderLayersFirst = false;
        public boolean allCutout = false;
        public boolean disablePreviewRotation = false;
        public boolean guiNoLighting = false;
        public boolean mergeMultilineExpr = true; // 預設true

        public String guiForeground = "";
        public String guiBackground = "";

        public List<RawImage> backgroundImages = new ArrayList<>();

        public Map<String, String> extraAnimations = new LinkedHashMap<>();
        public List<ExtraAnimationClassify> extraAnimationClassifies = new ArrayList<>();
        public List<ExtraAnimationButton> extraAnimationButtons = new ArrayList<>();
    }

    public static class ExtraAnimationClassify {
        public String id;
        public Map<String, String> extras = new LinkedHashMap<>();
    }

    public static class ExtraAnimationButton {
        public String id;
        public String name;
        public String description;
        public List<ConfigForm> forms = new ArrayList<>();
    }

    public static class ConfigForm {
        public String type; // checkbox, radio, range
        public String title;
        public String description;
        public String defaultValue;
        public float step, min, max;
        public Map<String, String> labels = new LinkedHashMap<>();
    }


    public static class RawDataFile {
        public String hash = "";
        public byte[] data;
        public RawDataFile() {}
        public RawDataFile(String hash, byte[] data) {
            this.hash = hash;
            this.data = data;
        }
    }

    public static class RawLanguageFile {
        public String hash = "";
        public Map<String, String> data = new LinkedHashMap<>();
        public RawLanguageFile() {}
        public RawLanguageFile(String hash, Map<String, String> data) {
            this.hash = hash;
            this.data = data;
        }
    }

    public static class RawFooter {
        public int version = 65535;
        public int unkInt1 = 1;// 通常为1
        public String rand = "";
        public long time = 0;
        public String extra = "";
        public int unkInt2 = 0; // format >= 24
    }
}