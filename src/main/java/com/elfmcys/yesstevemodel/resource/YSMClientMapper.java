package com.elfmcys.yesstevemodel.resource;

import com.elfmcys.yesstevemodel.NativeLibLoader;
import com.elfmcys.yesstevemodel.audio.AudioCodec;
import com.elfmcys.yesstevemodel.audio.AudioTrackData;
import com.elfmcys.yesstevemodel.client.ClientModelInfo;
import com.elfmcys.yesstevemodel.client.compat.oculus.ShadersTextureType;
import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.CheckboxConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RadioConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RangeConfig;
import com.elfmcys.yesstevemodel.client.model.MainModelData;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.ParticleEventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrameProcessor;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.EasingType;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.RawBoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.FloatValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.file.*;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable;
import com.elfmcys.yesstevemodel.geckolib3.util.LinearKeyframeInterpolator;
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.resource.models.*;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.elfmcys.yesstevemodel.util.data.StringMapPair;
import com.elfmcys.yesstevemodel.util.data.StringPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.apache.commons.lang3.tuple.Pair;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.vorbis.VorbisFile;
import org.joml.Vector2f;
import org.joml.Vector3f;
import rip.ysm.imagestream.avif.AvifDecoder;
import rip.ysm.imagestream.jpeg.JpegDecoder;
import rip.ysm.imagestream.webp.WebpDecoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

public class YSMClientMapper {

    private static byte[] toPng(byte[] data, int imageFormat, int width, int height) {
        if (data == null || data.length == 0 || imageFormat == 2) {
            return data;
        }

        if (imageFormat == 0) {
            imageFormat = YSMFolderDeserializer.detectFormat(data);
            if (imageFormat == 0) {
                imageFormat = 1;
            }
        }

        try {
            BufferedImage img = null;
            if (imageFormat == -1) {
                if (width > 0 && height > 0 && data.length >= width * height * 4) {
                    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    int[] pixels = new int[width * height];
                    for (int i = 0; i < pixels.length; i++) {
                        int r = data[i * 4] & 0xFF;
                        int g = data[i * 4 + 1] & 0xFF;
                        int b = data[i * 4 + 2] & 0xFF;
                        int a = data[i * 4 + 3] & 0xFF;
                        pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                    img.setRGB(0, 0, width, height, pixels, 0, width);
                } else throw new RuntimeException("Invalid RGBA texture");
            } else {
                switch (imageFormat) {
                    case 1 -> img = ImageIO.read(new ByteArrayInputStream(data));
                    case 3 -> img = new JpegDecoder().read(data);
                    case 4 -> img = new WebpDecoder().read(data);
                    case 5 -> img = new AvifDecoder().read(data);
                }
            }

            if (img != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static ClientModelInfo buildParsedBundle(RawYsmModel raw, String modelId) {
        Map<String, OuterFileTexture> mainTextures = new LinkedHashMap<>();
        for (RawYsmModel.RawTexture rt : raw.mainEntity.textures.values()) {
            byte[] processedData = toPng(rt.data, rt.imageFormat,rt.width,rt.height);
            OuterFileTexture tex = new OuterFileTexture(processedData);
            Map<ShadersTextureType, OuterFileTexture> suffixTextures = new LinkedHashMap<>();
            for (RawYsmModel.RawTexture.SubTexture sub : rt.subTextures) {
                if (sub.data == null) continue;
                byte[] processedSubData = toPng(sub.data, sub.imageFormat,sub.width,sub.height);
                if (sub.specularType == 1) {
                    suffixTextures.put(ShadersTextureType.NORMAL, new OuterFileTexture(processedSubData));
                } else if (sub.specularType == 2) {
                    suffixTextures.put(ShadersTextureType.SPECULAR, new OuterFileTexture(processedSubData));
                }
            }
            tex.setSuffixTextures(suffixTextures);
            mainTextures.put(rt.name, tex);
        }
        Map<String, OuterFileTexture> avatarTextures = new LinkedHashMap<>();
        for (RawYsmModel.RawMetadata.Author author : raw.metadata.authors) {
            if (author.avatarImage == null) continue;
            byte[] processedAvatarData = toPng(author.avatarImage.data, author.avatarImage.format,author.avatarImage.width,author.avatarImage.height);
            OuterFileTexture tex = new OuterFileTexture(processedAvatarData);
            avatarTextures.put(author.avatarImage.name, tex);
        }
        OrderedStringMap<String, OuterFileTexture> textureMap = buildTextureMap(mainTextures);

        GeometryDescription context = buildContext(raw.mainEntity.mainModel);
        int textureCount = Math.max(1, raw.mainEntity.textures.size());
        GeoModel mainMesh = buildMesh(raw.mainEntity.mainModel, context, textureCount);
        GeoModel armMesh = raw.mainEntity.armModel != null ? buildMesh(raw.mainEntity.armModel, context, textureCount) : mainMesh;
        GeoModel[] meshes = new GeoModel[]{mainMesh, armMesh};

        Map<String, AnimationFile> animations = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawAnimationFile> entry : raw.mainEntity.animationFiles.entrySet()) {
            animations.put(entry.getKey(), new AnimationFile(buildAnimations(entry.getValue(), raw.properties.mergeMultilineExpr)));
        }

        List<AnimationControllerFile> controllersList = new ArrayList<>();
        Map<String, AnimationController> controllerMap = buildControllers(raw.mainEntity.animationControllers, raw.properties.mergeMultilineExpr);
        if (!controllerMap.isEmpty()) {
            controllersList.add(new AnimationControllerFile(controllerMap));
        }

        MainModelData mainModelData = new MainModelData(meshes, animations, controllersList.toArray(new AnimationControllerFile[0]), textureMap);
        ServerModelInfo modelInfo = buildModelInfo(raw/*, modelId*/);
        ModelExtraResourcesFile extraResources = buildExtraResources(raw);
        ProjectileModelFiles[] extraItemModels = buildExtraItemModels(raw, context, raw.properties.mergeMultilineExpr);
        VehicleModelFiles[] extraEntityModels = buildExtraEntityModels(raw, context, raw.properties.mergeMultilineExpr);
        Map<String, OuterFileTexture> extraTextures = buildExtraTextures(raw);

        return new ClientModelInfo(mainModelData, extraItemModels, extraEntityModels, extraResources, modelInfo, avatarTextures, extraTextures);
    }

    private static GeoModel buildMesh(RawYsmModel.RawGeometry rawGeo, GeometryDescription context, int textureCount) {
        if (rawGeo == null || rawGeo.bones.isEmpty()) {
            return buildMesh(new GeoBone[0], new HashMap<>(), context, textureCount);
        }

        List<GeoBone> geoBones = new ArrayList<>();
        List<GeoModel.BakedBone> bakedBones = new ArrayList<>();
        Map<String, String> parentMap = new HashMap<>();

        for (RawYsmModel.RawBone rb : rawGeo.bones) {
            parentMap.put(rb.name, rb.parentName);
            geoBones.add(new GeoBone(rb.name, false, false, false, rb.pivot[0], rb.pivot[1], rb.pivot[2], rb.rotation[0], rb.rotation[1], rb.rotation[2]));

            GeoModel.BakedBone bb = new GeoModel.BakedBone();
            bb.name = rb.name;
            if (rb.name.startsWith("ysmGlow")) bb.glow = true;
            bb.pivotX = rb.pivot[0];
            bb.pivotY = rb.pivot[1];
            bb.pivotZ = rb.pivot[2];
            bb.rotX = rb.rotation[0];
            bb.rotY = rb.rotation[1];
            bb.rotZ = rb.rotation[2];
            bb.parentIdx = -1;

            // TODO: 优化算法
            for (RawYsmModel.RawCube rc : rb.cubes) {
                GeoModel.BakedCube bc = new GeoModel.BakedCube();
                boolean isNegativeVolume = false;

                for (RawYsmModel.RawFace rf : rc.faces) {
                    Vector3f v0 = new Vector3f(rf.positions[0][0], rf.positions[0][1], rf.positions[0][2]);
                    Vector3f v1 = new Vector3f(rf.positions[1][0], rf.positions[1][1], rf.positions[1][2]);
                    Vector3f v2 = new Vector3f(rf.positions[2][0], rf.positions[2][1], rf.positions[2][2]);
                    Vector3f normal = new Vector3f(rf.normal[0], rf.normal[1], rf.normal[2]);
                    Vector3f e1 = new Vector3f(v1).sub(v0);
                    Vector3f e2 = new Vector3f(v2).sub(v1);
                    Vector3f cross = new Vector3f(e1).cross(e2);
                    if (cross.dot(normal) < -1e-5f) {
                        isNegativeVolume = true;
                        break;
                    }
                }

                if (!isNegativeVolume) {
                    for (int i = 0; i < rc.faces.size(); i++) {
                        RawYsmModel.RawFace faceA = rc.faces.get(i);
                        Vector3f normA = new Vector3f(faceA.normal[0], faceA.normal[1], faceA.normal[2]);

                        for (int j = i + 1; j < rc.faces.size(); j++) {
                            RawYsmModel.RawFace faceB = rc.faces.get(j);
                            Vector3f normB = new Vector3f(faceB.normal[0], faceB.normal[1], faceB.normal[2]);
                            if (normA.dot(normB) < -0.99f) {
                                Vector3f centerA = getFaceCenter(faceA);
                                Vector3f centerB = getFaceCenter(faceB);
                                Vector3f diff = new Vector3f(centerA).sub(centerB);
                                if (diff.dot(normA) < -1e-5f) {
                                    isNegativeVolume = true;
                                    break;
                                }
                            }
                        }
                        if (isNegativeVolume) break;
                    }
                }
                // TODO: END

                bc.cullable = !(bb.glow && !isNegativeVolume);

                for (RawYsmModel.RawFace rf : rc.faces) {
                    GeoModel.BakedQuad bq = new GeoModel.BakedQuad();
                    bq.normal = new Vector3f(rf.normal[0], rf.normal[1], rf.normal[2]);
                    bq.positions = new Vector3f[4];
                    bq.uvs = new Vector2f[4];
                    for (int i = 0; i < 4; i++) {
                        bq.positions[i] = new Vector3f(rf.positions[i][0], rf.positions[i][1], rf.positions[i][2]);
                        bq.uvs[i] = new Vector2f(rf.u[i], rf.v[i]);
                    }
                    bc.quads.add(bq);
                }
                bb.cubes.add(bc);
            }
            bakedBones.add(bb);
        }

        // 回填父级索引
        for (GeoModel.BakedBone b : bakedBones) {
            String parentName = parentMap.get(b.name);
            if (parentName != null && !parentName.isEmpty()) {
                for (int i = 0; i < bakedBones.size(); i++) {
                    if (bakedBones.get(i).name.equals(parentName)) {
                        b.parentIdx = i;
                        break;
                    }
                }
            }
            if (b.name.equals("LeftArm")) b.partMask = 1;
            else if (b.name.equals("RightArm")) b.partMask = 2;
            else if (b.name.equals("Background")) b.partMask = 3;
            else if (b.parentIdx != -1) b.partMask = bakedBones.get(b.parentIdx).partMask;
            else b.partMask = 0;
        }

        GeoModel mesh = buildMesh(geoBones.toArray(new GeoBone[0]), parentMap, context, textureCount);
        mesh.bakedBones = bakedBones;
        if (NativeLibLoader.isLoaded()) mesh.buildNativeCache();
        return mesh;
    }

    private static Map<String, Animation> buildAnimations(RawYsmModel.RawAnimationFile animFile, boolean mergeMultilineExpr) {
        Map<String, Animation> result = new LinkedHashMap<>();
        for (RawYsmModel.RawAnimation ra : animFile.animations.values()) {
            ILoopType loopMode = ILoopType.EDefaultLoopTypes.PLAY_ONCE;
            if (ra.loopMode == 1) loopMode = ILoopType.EDefaultLoopTypes.LOOP;
            else if (ra.loopMode == 3) loopMode = ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;

            List<BoneAnimation> boneAnims = new ArrayList<>();
            for (RawYsmModel.RawBoneAnimation rba : ra.boneAnimations) {
//                if (rba.boneName.equals("gun")) {
//                    "".hashCode();
//                }
                List<BoneKeyFrame> rotFrames = parseKeyframes(rba.rotation, true);
                List<BoneKeyFrame> posFrames = parseKeyframes(rba.position, false);
                List<BoneKeyFrame> scaleFrames = parseKeyframes(rba.scale, false);
                boneAnims.add(new BoneAnimation(rba.boneName, rotFrames, posFrames, scaleFrames));
            }

            List<EventKeyFrame<String>> soundEffects = new ArrayList<>();
            for (RawYsmModel.RawSoundEffect rse : ra.soundEffects) {
                soundEffects.add(new EventKeyFrame<>(rse.timestamp * 20.0f, rse.effectName));
            }

            List<EventKeyFrame<IValue[]>> timelineEvents = new ArrayList<>();
            for (RawYsmModel.RawTimelineEvent rte : ra.timelineEvents) {
                List<IValue> values = parse(rte.events, mergeMultilineExpr);
                timelineEvents.add(new EventKeyFrame<>(rte.timestamp * 20.0f, values.toArray(new IValue[0])));
            }

            IValue blendWeight;
            if (ra.blendWeight instanceof Float)
                blendWeight = new FloatValue((Float) ra.blendWeight);
            else if (ra.blendWeight instanceof String)
                try {
                    blendWeight = parse((String) ra.blendWeight);
                } catch (Exception e) {
                    blendWeight = null;
                }
            else blendWeight = null;

            Animation anim = new Animation(ra.name, ra.length * 20.0f, loopMode, blendWeight, null, null, null, boneAnims.toArray(new BoneAnimation[0]), soundEffects.toArray(new EventKeyFrame[0]), new ParticleEventKeyFrame[0], timelineEvents.toArray(new EventKeyFrame[0]));
            result.put(ra.name, anim);
        }
        return result;
    }

    private static List<BoneKeyFrame> parseKeyframes(List<RawYsmModel.RawKeyframe> frames, boolean isRotation) {
        List<RawBoneKeyFrame> builders = new ArrayList<>();
        for (RawYsmModel.RawKeyframe rk : frames) {
            RawBoneKeyFrame builder = new RawBoneKeyFrame();
            builder.startTick = rk.timestamp * 20.0f;
            builder.easingType = rk.interpolationMode == 2 ? EasingType.CATMULLROM : EasingType.LINEAR;
            builder.contiguous = !rk.hasPreData;

            if (rk.hasPreData) {
                assignToBuilder(builder, rk.preData, true);
                assignToBuilder(builder, rk.postData, false);
            } else {
                assignToBuilder(builder, rk.postData, true);
            }
            builders.add(builder);
        }
        return BoneKeyFrameProcessor.process(builders, isRotation);
    }

    private static void assignToBuilder(RawBoneKeyFrame builder, Object[] data, boolean isPre) {
        for (int axis = 0; axis < 3; axis++) {
            double dVal = 0.0;
            IValue iVal = null;
            Object val = data[axis];
            if (val instanceof Float) dVal = (Float) val;
            else if (val instanceof String) {
                try {
                    iVal = parse((String) val);
                } catch (Exception ignore) {
                }
            }
            if (isPre) {
                if (axis == 0) {
                    builder.preX = dVal;
                    builder.preXValue = iVal;
                } else if (axis == 1) {
                    builder.preY = dVal;
                    builder.preYValue = iVal;
                } else if (axis == 2) {
                    builder.preZ = dVal;
                    builder.preZValue = iVal;
                }
            } else {
                if (axis == 0) {
                    builder.postX = dVal;
                    builder.postXValue = iVal;
                } else if (axis == 1) {
                    builder.postY = dVal;
                    builder.postYValue = iVal;
                } else if (axis == 2) {
                    builder.postZ = dVal;
                    builder.postZValue = iVal;
                }
            }
        }
    }

    private static Map<String, AnimationController> buildControllers(Map<String, RawYsmModel.RawAnimationController> rawControllers, boolean mergeMultilineExpr) {
        Map<String, AnimationController> result = new LinkedHashMap<>();
        for (RawYsmModel.RawAnimationController rac : rawControllers.values()) {
            List<AnimationState> states = new ArrayList<>();
            for (RawYsmModel.RawControllerState rs : rac.states) {
                List<Pair<String, IValue>> animations = new ArrayList<>();
                for (Map.Entry<String, String> e : rs.animations.entrySet()) {
                    IValue blend = null;
                    if (!e.getValue().isEmpty()) {
                        try {
                            blend = parse(e.getValue());
                        } catch (Exception ignore) {
                        }
                    }
                    animations.add(Pair.of(e.getKey(), blend));
                }

                List<Pair<String, IValue>> transitions = new ArrayList<>();
                for (Map.Entry<String, String> e : rs.transitions.entrySet()) {
                    IValue condition = null;
                    if (!e.getValue().isEmpty()) {
                        try {
                            condition = parse(e.getValue());
                        } catch (Exception ignore) {
                        }
                    }
                    transitions.add(Pair.of(e.getKey(), condition));
                }

                List<IValue> onEntry = parse(rs.onEntry, mergeMultilineExpr);
                List<IValue> onExit = parse(rs.onExit, mergeMultilineExpr);

                IInterpolable blendTransition;
                if (!rs.blendTransitions.isEmpty()) {
                    float[] keys = new float[rs.blendTransitions.size()];
                    float[] values = new float[rs.blendTransitions.size()];
                    int i = 0;
                    for (Map.Entry<Float, Float> e : rs.blendTransitions.entrySet()) {
                        keys[i] = e.getKey();
                        values[i] = e.getValue();
                        i++;
                    }
                    blendTransition = new LinearKeyframeInterpolator(keys, values);
                } else {
                    blendTransition = new TicksInterpolator(rs.blendTransitionValue);
                }

                states.add(new AnimationState(rs.name, animations.toArray(new Pair[0]), transitions.toArray(new Pair[0]), rs.soundEffects.toArray(new String[0]), onEntry.toArray(new IValue[0]), onExit.toArray(new IValue[0]), blendTransition, rs.blendViaShortestPath));
            }
            String initialState = rac.initialState != null && !rac.initialState.isEmpty() ? rac.initialState : "default";
            result.put(rac.animationName, new AnimationController(initialState, states.toArray(new AnimationState[0])));
        }
        return result;
    }

    public static ServerModelInfo buildModelInfo(RawYsmModel raw/*, String modelId*/) {
        RawYsmModel.RawMetadata rm = raw.metadata;
        List<AuthorInfo> authors = new ArrayList<>();
        for (RawYsmModel.RawMetadata.Author a : rm.authors) {
            authors.add(new AuthorInfo(a.name, a.role, new OrderedStringMap<>(new Object2ObjectArrayMap<>(a.contacts)), a.comment));
        }

        Metadata extraInfo = new Metadata(rm.name, rm.tips, new StringPair(rm.licenseType, rm.licenseDescription), authors.toArray(new AuthorInfo[0]), new OrderedStringMap<>(new Object2ObjectArrayMap<>(rm.links)));

        RawYsmModel.RawProperties rp = raw.properties;
        List<StringMapPair> classifyList = new ArrayList<>();
        for (RawYsmModel.ExtraAnimationClassify rCls : rp.extraAnimationClassifies) {
            classifyList.add(new StringMapPair(rCls.id, new OrderedStringMap<>(new Object2ObjectArrayMap<>(rCls.extras))));
        }

        List<ExtraAnimationButtons> buttonsList = new ArrayList<>();
        for (RawYsmModel.ExtraAnimationButton rBtn : rp.extraAnimationButtons) {
            List<AbstractConfig> metaList = new ArrayList<>();
            for (RawYsmModel.ConfigForm form : rBtn.forms) {
                if ("checkbox".equals(form.type)) {
                    metaList.add(new CheckboxConfig(form.title, form.description, form.defaultValue));
                } else if ("radio".equals(form.type)) {
                    metaList.add(new RadioConfig(form.title, form.description, form.defaultValue, new OrderedStringMap<>(new Object2ObjectArrayMap<>(form.labels))));
                } else if ("range".equals(form.type)) {
                    metaList.add(new RangeConfig(form.title, form.description, form.defaultValue, form.step, form.min, form.max));
                }
            }
            buttonsList.add(new ExtraAnimationButtons(rBtn.id, rBtn.name, rBtn.description, metaList.toArray(new AbstractConfig[0])));
        }
        ModelProperties properties = new ModelProperties(rp.heightScale, rp.widthScale, rp.defaultTexture, rp.previewAnimation, new OrderedStringMap<>(new Object2ObjectArrayMap<>(rp.extraAnimations)), buttonsList.toArray(new ExtraAnimationButtons[0]), classifyList.toArray(new StringMapPair[0]), rp.isFree, rp.renderLayersFirst, rp.disablePreviewRotation);

        int bones = 0;
        int cubes = 0;
        int faces = 0;
        if (raw.mainEntity.mainModel != null) {
            bones = raw.mainEntity.mainModel.bones.size();
            for (RawYsmModel.RawBone bone : raw.mainEntity.mainModel.bones) {
                cubes += bone.cubes.size();
                for (RawYsmModel.RawCube cube : bone.cubes) {
                    faces += cube.faces.size();
                }
            }
        }
        MainModelInfo stats = new MainModelInfo(bones, cubes, faces);

        RawYsmModel.RawFooter footer = raw.footer;
        return new ServerModelInfo(extraInfo,
                properties,
                stats,
                footer.version,
                rp.sha256 != null ? rp.sha256 : "",
                footer.extra, footer.time, footer.rand);
    }

    private static ModelExtraResourcesFile buildExtraResources(RawYsmModel raw) {
        Map<String, AudioTrackData> sounds = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawDataFile> entry : raw.soundFiles.entrySet()) {
            String name = entry.getKey();
            byte[] data = entry.getValue().data;
            AudioTrackData track = parseAudioTrackData(data);
            if (track != null) sounds.put(name, track);
        }

        Map<String, IValue> functions = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawDataFile> entry : raw.functionFiles.entrySet()) {
            String name = entry.getKey();
            byte[] data = entry.getValue().data;
            String molangScript = new String(data, StandardCharsets.UTF_8);
            try {
                functions.put(name, GeckoLibCache.getMolangParser().parseExpression(molangScript, true));
            } catch (Exception e) {
            }
        }

        Map<String, Map<String, String>> translations = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawLanguageFile> entry : raw.languageFiles.entrySet()) {
            translations.put(entry.getKey(), entry.getValue().data);
        }

        return new ModelExtraResourcesFile(sounds, functions, translations);
    }

    private static AudioTrackData parseAudioTrackData(byte[] oggData) {
        if (oggData == null || oggData.length < 8) return null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(oggData);
            OggFile oggFile = new OggFile(bais);
            String header = new String(oggData, 0, Math.min(oggData.length, 100), StandardCharsets.US_ASCII);
            boolean isOpus = header.contains("OpusHead");

            AudioCodec codec = isOpus ? AudioCodec.OPUS : AudioCodec.VORBIS;
            int sampleRate;
            if (isOpus) {
                OpusFile opus = new OpusFile(oggFile);
                sampleRate = (int) opus.getInfo().getRate();
            } else {
                VorbisFile vorbis = new VorbisFile(oggFile);
                sampleRate = (int) vorbis.getInfo().getRate();
            }

            OggPacketReader reader = oggFile.getPacketReader();
            long durationSamples = 0;
            var packet = reader.getNextPacket();
            while (packet != null) {
                long granule = packet.getGranulePosition();
                if (granule > 0) durationSamples = granule;
                packet = reader.getNextPacket();
            }

            ByteBuffer directBuf = ByteBuffer.allocateDirect(oggData.length);
            directBuf.put(oggData);
            directBuf.flip();

            return new AudioTrackData(directBuf, codec.ordinal(), sampleRate, durationSamples);
        } catch (Exception e) {
            return null;
        }
    }

    private static ProjectileModelFiles[] buildExtraItemModels(RawYsmModel raw, GeometryDescription context, boolean mergeMultilineExpr) {
        List<ProjectileModelFiles> list = new ArrayList<>();
        for (Map.Entry<String, RawYsmModel.RawSubEntity> entry : raw.projectiles.entrySet()) {
            RawYsmModel.RawSubEntity sub = entry.getValue();
            ProjectileModelFiles holder = buildSubEntityHolder(sub, context, 1, mergeMultilineExpr);
            list.add(holder);
        }
        return list.toArray(new ProjectileModelFiles[0]);
    }

    private static VehicleModelFiles[] buildExtraEntityModels(RawYsmModel raw, GeometryDescription context, boolean mergeMultilineExpr) {
        List<VehicleModelFiles> list = new ArrayList<>();
        for (Map.Entry<String, RawYsmModel.RawSubEntity> entry : raw.vehicles.entrySet()) {
            RawYsmModel.RawSubEntity sub = entry.getValue();
            VehicleModelFiles wrapper = buildSubEntityWrapper(sub, context, 1, mergeMultilineExpr);
            list.add(wrapper);
        }
        return list.toArray(new VehicleModelFiles[0]);
    }

    private static ProjectileModelFiles buildSubEntityHolder(RawYsmModel.RawSubEntity sub, GeometryDescription context, int textureCount, boolean mergeMultilineExpr) {
        GeoModel mesh = buildMesh(sub.model, context, textureCount);

        Map<String, Animation> allAnimations = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawAnimationFile> entry : sub.animationFiles.entrySet()) {
            Map<String, Animation> fileAnims = buildAnimations(entry.getValue(), mergeMultilineExpr);
            allAnimations.putAll(fileAnims);
        }
        AnimationFile combinedAnim = new AnimationFile(allAnimations);

        AnimationControllerFile controllers = new AnimationControllerFile(buildSubEntityControllers(sub, "projectile", mergeMultilineExpr));

        OuterFileTexture texture = null;
        if (!sub.textures.isEmpty()) {
            RawYsmModel.RawTexture rt = sub.textures.values().iterator().next();
            byte[] processedData = toPng(rt.data, rt.imageFormat,rt.width,rt.height);
            texture = new OuterFileTexture(processedData);
        }

        String[] matchIds = sub.matchIds != null ? sub.matchIds : new String[]{sub.identifier};
        return new ProjectileModelFiles(matchIds, mesh, combinedAnim, controllers, texture);
    }

    private static VehicleModelFiles buildSubEntityWrapper(RawYsmModel.RawSubEntity sub, GeometryDescription context, int textureCount, boolean mergeMultilineExpr) {
        GeoModel mesh = buildMesh(sub.model, context, textureCount);

        Map<String, Animation> allAnimations = new LinkedHashMap<>();
        for (RawYsmModel.RawAnimationFile animFile : sub.animationFiles.values()) {
            Map<String, Animation> fileAnims = buildAnimations(animFile, mergeMultilineExpr);
            allAnimations.putAll(fileAnims);
        }
        AnimationFile combinedAnim = new AnimationFile(allAnimations);

        AnimationControllerFile controllers = new AnimationControllerFile(buildSubEntityControllers(sub, "vehicle", mergeMultilineExpr));

        OuterFileTexture texture = null;
        if (!sub.textures.isEmpty()) {
            RawYsmModel.RawTexture rt = sub.textures.values().iterator().next();
            byte[] processedData = toPng(rt.data, rt.imageFormat,rt.width,rt.height);
            texture = new OuterFileTexture(processedData);
        }

        String[] matchIds = sub.matchIds != null ? sub.matchIds : new String[]{sub.identifier};
        return new VehicleModelFiles(matchIds, mesh, combinedAnim, controllers, texture);
    }

    private static Map<String, AnimationController> buildSubEntityControllers(RawYsmModel.RawSubEntity sub, String prefix, boolean mergeMultilineExpr) {
        Map<String, AnimationController> controllers = buildControllers(sub.animationControllers, mergeMultilineExpr);
        if (!controllers.isEmpty() || sub.animationFiles.isEmpty()) {
            return controllers;
        }

        String firstAnimation = null;
        for (RawYsmModel.RawAnimationFile animationFile : sub.animationFiles.values()) {
            for (Animation animation : buildAnimations(animationFile, mergeMultilineExpr).values()) {
                if (animation != null && !animation.isEmpty()) {
                    firstAnimation = animation.animationName;
                    break;
                }
            }
            if (firstAnimation != null) break;
        }
        if (firstAnimation == null) {
            return controllers;
        }

        AnimationState defaultState = new AnimationState(
                "default",
                new Pair[]{Pair.of(firstAnimation, null)},
                new Pair[0],
                new String[0],
                new IValue[0],
                new IValue[0],
                new TicksInterpolator(0.0f),
                false
        );
        controllers.put(prefix + ".main", new AnimationController("default", new AnimationState[]{defaultState}));
        return controllers;
    }

    private static Map<String, OuterFileTexture> buildExtraTextures(RawYsmModel raw) {
        Map<String, OuterFileTexture> result = new LinkedHashMap<>();
        for (RawYsmModel.RawImage img : raw.properties.backgroundImages) {
            // img.name可能是"gui_background"或"gui_foreground"
            if (img.name != null && !img.name.isEmpty()) {
                byte[] processedData = toPng(img.data, img.format, img.width, img.height);
                result.put(img.name, new OuterFileTexture(processedData));
            }
        }
        return result;
    }

    public static List<IValue> parse(List<String> array, boolean mergeMultilineExpr) {
        List<IValue> values = new ArrayList<>();

        if (!mergeMultilineExpr) {
            for (String expr : array) values.add(parse(expr));
            return values;
        }

        try {
            StringBuilder parserText = new StringBuilder();

            for (int i = 0; i < array.size(); i++) {
                parserText.append(array.get(i));
                if (i < array.size() - 1) {
                    parserText.append("\n");
                }
            }

            values.add(parse(parserText.toString()));
        } catch (Throwable ex) {
            values.add(FloatValue.ZERO);
        }
        return values;
    }

    public static IValue parse(String str) {
        try {
            return GeckoLibCache.getMolangParser().parseExpression(str, false);
        } catch (Throwable ex) {
            return FloatValue.ZERO;
        }
    }

    private static String[] buildPath(String targetBone, Map<String, String> parentMap) {
        if (!parentMap.containsKey(targetBone)) {
            return new String[0];
        }
        List<String> path = new ArrayList<>();
        String current = targetBone;
        while (current != null && !current.isEmpty()) {
            path.add(current);
            current = parentMap.get(current);
        }
        Collections.reverse(path);
        return path.toArray(new String[0]);
    }

    private static String[][] buildBoneNameArrays(Map<String, String> parentMap) {
        String[][] arrays = new String[35][];

        // 模型骨骼大全
        String[] targetLocators = new String[]{
                "LeftHandLocator",
                "RightHandLocator",
                "ElytraLocator",
                "PistolLocator",
                "RifleLocator",
                "LeftWaistLocator",
                "RightWaistLocator",
                "LeftShoulderLocator",
                "RightShoulderLocator",
                "BladeLocator",
                "SheathLocator",
                "Head",
                "BackpackLocator",
                "LeftHandLocator2",
                "LeftHandLocator3",
                "LeftHandLocator4",
                "LeftHandLocator5",
                "LeftHandLocator6",
                "LeftHandLocator7",
                "LeftHandLocator8",
                "RightHandLocator2",
                "RightHandLocator3",
                "RightHandLocator4",
                "RightHandLocator5",
                "RightHandLocator6",
                "RightHandLocator7",
                "RightHandLocator8",
                "PassengerLocator",
                "PassengerLocator2",
                "PassengerLocator3",
                "PassengerLocator4",
                "PassengerLocator5",
                "PassengerLocator6",
                "PassengerLocator7",
                "PassengerLocator8"
        };

        for (int i = 0; i < arrays.length; i++) {
            if (targetLocators[i] != null && !targetLocators[i].isEmpty()) {
                arrays[i] = buildPath(targetLocators[i], parentMap);
            } else {
                arrays[i] = new String[0];
            }
        }

        return arrays;
    }

    public static GeoModel buildMesh(GeoBone[] bones, Map<String, String> parentMap, GeometryDescription context, int textureCount) {
        String[][] boneNameArrays = buildBoneNameArrays(parentMap);
        boolean[] flags = new boolean[]{parentMap.containsKey("LeftArm"), parentMap.containsKey("RightArm"), parentMap.containsKey("Background")};
        boolean[] translucencyArray = new boolean[Math.max(1, textureCount)];
        return new GeoModel(bones, boneNameArrays, flags, context, translucencyArray);
    }

    public static OrderedStringMap<String, OuterFileTexture> buildTextureMap(Map<String, OuterFileTexture> textures) {
        if (textures.isEmpty()) {
            return new OrderedStringMap<>(new String[0], new OuterFileTexture[0]);
        }
        String[] keys = textures.keySet().toArray(new String[0]);
        OuterFileTexture[] values = textures.values().toArray(new OuterFileTexture[0]);
        return new OrderedStringMap<>(keys, values);
    }

    public static GeometryDescription buildContext(RawYsmModel.RawGeometry model) {
        return new GeometryDescription(
                model.identifier,
                model.textureWidth, // default texture width ratio
                model.textureHeight, // default texture height ratio
                model.visibleBoundsWidth, // offset X
                model.visibleBoundsHeight, // offset Y
                IntStream.range(0, model.visibleBoundsOffset.length)
                        .mapToDouble(i -> model.visibleBoundsOffset[i])
                        .toArray()
        );
    }

    private static Vector3f getFaceCenter(RawYsmModel.RawFace face) {
        Vector3f center = new Vector3f(0, 0, 0);
        for (int i = 0; i < 4; i++) {
            center.add(face.positions[i][0], face.positions[i][1], face.positions[i][2]);
        }
        return center.div(4.0f);
    }
}
