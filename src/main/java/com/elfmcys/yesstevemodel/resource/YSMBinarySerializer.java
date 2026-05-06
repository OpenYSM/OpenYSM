package com.elfmcys.yesstevemodel.resource;

import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import rip.ysm.security.YSMByteBuf;
import io.netty.buffer.Unpooled;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class YSMBinarySerializer {

    public static YSMByteBuf serialize(RawYsmModel model, int format, boolean writeFooter) {
        YSMByteBuf buf = new YSMByteBuf(Unpooled.buffer());
        if (format >= 16) {
            writeModern(buf, model, format);

            if (writeFooter) {
                writeFooter(buf, model); // 这个是服务器模型模型下发的，不是导出.ysm的，两个很像，待分析尝试合并
            }

        } else {
            throw new UnsupportedOperationException();
        }
        return buf;
    }

    private static void writeFooter(YSMByteBuf buf, RawYsmModel model) {
        // 如果没有footer数据（比如纯本地json加载的散件模型）
        if (model.footer == null || model.footer.version == 65535) {
            buf.writeVarInt(65535); // 默认未加密标识版本号
            buf.writeVarInt(0);
            buf.writeVarLong(0L);
            return;
        }

        buf.writeVarInt(model.footer.version != 0 ? model.footer.version : 65535);

        buf.writeVarInt(model.footer.unkInt1);

        if (model.footer.unkInt1 != 0) {
            buf.writeString(model.footer.rand != null ? model.footer.rand : "");
        }

        buf.writeVarLong(model.footer.time);

        if (model.footer.unkInt1 != 0) {
            buf.writeString(model.footer.extra != null ? model.footer.extra : "");
            buf.writeVarInt(model.footer.unkInt2);
        }
    }

    private static void writeModern(YSMByteBuf buf, RawYsmModel model, int format) {
        writeSoundFiles(buf, model.soundFiles, format);
        writeFunctionFiles(buf, model.functionFiles);
        writeLanguageFiles(buf, model.languageFiles);
        if (format < 26) {
            throw new UnsupportedOperationException();
        } else {
            writeSubEntities(buf, model.vehicles, format, "Vehicle");
            writeSubEntities(buf, model.projectiles, format, "Projectile");
        }
        buf.writeVarInt(1); // unknown

        Map<String, RawYsmModel.RawAnimationFile> mainAnimFiles = model.mainEntity.animationFiles;
        buf.writeVarInt(mainAnimFiles.size());
        for (RawYsmModel.RawAnimationFile animFile : mainAnimFiles.values()) {
            buf.writeVarInt(animFile.animType);
            buf.writeString(animFile.fileHash != null ? animFile.fileHash : "");
            writeAnimationFileContent(buf, animFile, format);
        }

        writeAnimationControllers(buf, model.mainEntity.animationControllers, format);
        writeTextureFiles(buf, model.mainEntity.textures);

        List<RawYsmModel.RawGeometry> geoList = new ArrayList<>();
        if (model.mainEntity.mainModel != null) geoList.add(model.mainEntity.mainModel);
        if (model.mainEntity.armModel != null) geoList.add(model.mainEntity.armModel);
//        RawYsmModel.RawSubEntity arrowEntity = model.projectiles.get("minecraft:arrow");
//        if (arrowEntity != null && arrowEntity.model != null) geoList.add(arrowEntity.model);

        buf.writeVarInt(geoList.size());
        for (RawYsmModel.RawGeometry geo : geoList) {
            buf.writeVarInt(geo.modelType);
            buf.writeString(geo.sha256 != null ? geo.sha256 : "");
            writeGeometry(buf, geo, format);
        }

        writeYsmJson(buf, model, format);
    }

    private static void writeSubEntities(YSMByteBuf buf, Map<String, RawYsmModel.RawSubEntity> entities, int format, String category) {
        buf.writeVarInt(entities.size());
        int index = 0;
        for (RawYsmModel.RawSubEntity sub : entities.values()) {
            buf.writeVarInt(sub.animationFiles.size());
            for (RawYsmModel.RawAnimationFile animFile : sub.animationFiles.values()) {
                buf.writeString(animFile.fileHash != null ? animFile.fileHash : "");
                writeAnimationFileContent(buf, animFile, format);
            }
            buf.writeVarInt(0); // separator
            RawYsmModel.RawTexture baseTex = sub.textures.values().iterator().next();
            byte[] baseData = baseTex.data;
            int baseFormat = baseTex.imageFormat;
            if (baseFormat == -1) {
                baseData = convertRgbaToPng(baseData, baseTex.width, baseTex.height);
                baseFormat = 2;
            }
            writeSpecialImage(buf, baseTex.hash, baseData);
            buf.writeVarInt(baseTex.width);
            buf.writeVarInt(baseTex.height);
            buf.writeVarInt(baseFormat);
            buf.writeVarInt(baseTex.unknownFlag);
            buf.writeVarInt(baseTex.subTextures.size());
            for (RawYsmModel.RawTexture.SubTexture subTex : baseTex.subTextures) {
                buf.writeVarInt(subTex.specularType);
                byte[] subData = subTex.data;
                int subFormat = subTex.imageFormat;
                if (subFormat == -1) {
                    subData = convertRgbaToPng(subData, subTex.width, subTex.height);
                    subFormat = 2;
                }
                writeSpecialImage(buf, subTex.hash, subData);
                buf.writeVarInt(subTex.width);
                buf.writeVarInt(subTex.height);
                buf.writeVarInt(subFormat);
                buf.writeVarInt(subTex.unknownFlag);
            }
            buf.writeString(sub.model != null && sub.model.sha256 != null ? sub.model.sha256 : "");
            writeGeometry(buf, sub.model, format);
            if (format > 26) {
                buf.writeVarInt(0x01); // footer
                buf.writeString(sub.identifier);
            }
            index++;
        }
    }

    private static void writeGeometry(YSMByteBuf buf, RawYsmModel.RawGeometry geo, int format) {
        buf.writeVarInt(geo.bones.size());
        for (RawYsmModel.RawBone bone : geo.bones) {
            buf.writeString(bone.parentName != null ? bone.parentName : "");
            buf.writeVarInt(bone.cubes.size());
            for (RawYsmModel.RawCube cube : bone.cubes) {
                buf.writeVarInt(cube.faces.size());
                for (RawYsmModel.RawFace face : cube.faces) {
                    writeVector3D(buf, face.normal);
                    for (int v = 0; v < 4; v++) {
                        writeVector3D(buf, face.positions[v]);
                        buf.writeFloat(face.u[v]);
                        buf.writeFloat(face.v[v]);
                    }
                }
                buf.writeVarInt(cube.unkInt1);
                buf.writeVarInt(cube.unkInt2);
                buf.writeVarInt(cube.unkInt3);
            }
            buf.writeString(bone.name != null ? bone.name : "");
            buf.writeVarInt(bone.unkPad1);
            buf.writeVarInt(bone.unkPad2);
            buf.writeVarInt(bone.unkPad3);
            buf.writeVarInt(bone.unkPad4);
            buf.writeVarInt(bone.unkPad5);
            writeVector3D(buf, bone.pivot);
            writeVector3D(buf, bone.rotation);
        }
        buf.writeString(geo.identifier != null ? geo.identifier : "");
        buf.writeFloat(geo.textureHeight);
        buf.writeFloat(geo.textureWidth);
        buf.writeFloat(geo.visibleBoundsHeight);
        buf.writeFloat(geo.visibleBoundsWidth);
        buf.writeVarInt(geo.visibleBoundsOffset != null ? geo.visibleBoundsOffset.length : 0);
        if (geo.visibleBoundsOffset != null) {
            for (float v : geo.visibleBoundsOffset) buf.writeFloat(v);
        }
        buf.writeFloat(geo.unkFloat1);
        buf.writeFloat(geo.unkFloat2);
        buf.writeVarInt(0); // hasInfoJsonFlag
        buf.writeVarInt(geo.footerPad1);
        buf.writeVarInt(geo.footerPad2);
        buf.writeVarInt(geo.footerPad3);
    }
    private static void writeAnimationFileContent(YSMByteBuf buf, RawYsmModel.RawAnimationFile animFile, int format) {
        buf.writeVarInt(animFile.animations.size());
        for (RawYsmModel.RawAnimation anim : animFile.animations.values()) {
            buf.writeString(anim.name);
            buf.writeFloat(anim.length * 20f); // 还原ticks
            buf.writeVarInt(anim.loopMode);

            if (format > 9) {
                buf.writeVarInt(anim.unkInt1);
                buf.writeVarInt(anim.unkInt2);

                int hasBlend = (anim.blendWeight != null) ? 1 : 0;
                buf.writeVarInt(hasBlend);
                if (hasBlend > 0) {
                    writeMolangValue(buf, anim.blendWeight);
                }
                buf.writeVarInt(anim.unkInt4);
            }

            // 骨骼动画
            buf.writeVarInt(anim.boneAnimations.size());
            for (RawYsmModel.RawBoneAnimation ba : anim.boneAnimations) {
                buf.writeString(ba.boneName);
                writeChannel(buf, ba.rotation);
                writeChannel(buf, ba.position);
                writeChannel(buf, ba.scale);
            }

            // timeline
            buf.writeVarInt(anim.timelineEvents.size());
            for (RawYsmModel.RawTimelineEvent event : anim.timelineEvents) {
                buf.writeVarInt(event.events.size());
                for (String e : event.events) buf.writeString(e);
                buf.writeFloat(event.timestamp * 20f);
            }

            // sound effects
            if (format > 9) {
                buf.writeVarInt(anim.soundEffects.size());
                for (RawYsmModel.RawSoundEffect sfx : anim.soundEffects) {
                    buf.writeString(sfx.effectName);
                    buf.writeFloat(sfx.timestamp * 20f);
                }
            }
        }
    }

    private static void writeChannel(YSMByteBuf buf, List<RawYsmModel.RawKeyframe> keyframes) {
        buf.writeVarInt(keyframes.size());
        for (RawYsmModel.RawKeyframe kf : keyframes) {
            buf.writeFloat(kf.timestamp * 20f);
            buf.writeVarInt(kf.interpolationMode);

            if (kf.hasPreData) {
                for (int i = 0; i < 3; i++) writeMolangValue(buf, kf.preData[i]);
                buf.writeVarInt(1);
                for (int i = 0; i < 3; i++) writeMolangValue(buf, kf.postData[i]);
            } else {
                for (int i = 0; i < 3; i++) writeMolangValue(buf, kf.postData[i]);
                buf.writeVarInt(0);
            }
        }
    }

    private static void writeMolangValue(YSMByteBuf buf, Object val) {
        if (val instanceof Float) {
            buf.writeByte((byte) 0x01);
            buf.writeFloat((Float) val);
        } else if (val instanceof String) {
            buf.writeByte((byte) 0x02);
            buf.writeString((String) val);
        } else {
            throw new IllegalArgumentException("Unknown molang value type: " + val.getClass());
        }
    }

    private static void writeAnimationControllers(YSMByteBuf buf, Map<String, RawYsmModel.RawAnimationController> controllers, int format) {
        if (controllers.isEmpty()) {
            buf.writeVarInt(0);
            return;
        }

        RawYsmModel.RawAnimationController first = controllers.values().iterator().next();
        buf.writeVarInt(1); // controllerCount
        buf.writeString(first.name != null ? first.name : "");
        buf.writeString(first.hash != null ? first.hash : "");

        buf.writeVarInt(controllers.size()); // animationCount
        for (RawYsmModel.RawAnimationController ac : controllers.values()) {
            buf.writeString(ac.animationName);
            buf.writeString(ac.initialState != null ? ac.initialState : "");

            buf.writeVarInt(ac.states.size());
            for (RawYsmModel.RawControllerState state : ac.states) {
                buf.writeString(state.name);

                // animations
                buf.writeVarInt(state.animations.size());
                for (Map.Entry<String, String> e : state.animations.entrySet()) {
                    buf.writeString(e.getKey());
                    buf.writeString(e.getValue());
                }

                // transitions
                buf.writeVarInt(state.transitions.size());
                for (Map.Entry<String, String> e : state.transitions.entrySet()) {
                    buf.writeString(e.getKey());
                    buf.writeString(e.getValue());
                }

                // on_entry
                buf.writeVarInt(state.onEntry.size());
                for (String s : state.onEntry) buf.writeString(s);

                // on_exit
                buf.writeVarInt(state.onExit.size());
                for (String s : state.onExit) buf.writeString(s);

                // blend_transition
                if (!state.blendTransitions.isEmpty()) {
                    buf.writeVarInt(0);
                    buf.writeVarInt(state.blendTransitions.size());
                    for (Map.Entry<Float, Float> e : state.blendTransitions.entrySet()) {
                        buf.writeFloat(e.getKey());
                        buf.writeFloat(e.getValue());
                    }
                } else {
                    buf.writeVarInt(1);
                    buf.writeFloat(state.blendTransitionValue);
                }

                buf.writeVarInt(state.blendViaShortestPath ? 1 : 0);

                if (format > 26) {
                    buf.writeVarInt(state.soundEffects.size());
                    for (String eff : state.soundEffects) buf.writeString(eff);
                }
            }
        }
    }

    private static void writeTextureFiles(YSMByteBuf buf, Map<String, RawYsmModel.RawTexture> textures) {
        buf.writeVarInt(textures.size());
        for (RawYsmModel.RawTexture tex : textures.values()) {
            buf.writeString(tex.name);
            buf.writeString(tex.hash != null ? tex.hash : "");
            byte[] texData = tex.data;
            int texFormat = tex.imageFormat;
            if (texFormat == -1) {
                texData = convertRgbaToPng(texData, tex.width, tex.height);
                texFormat = 2;
            }
            buf.writeByteArray(texData);
            buf.writeVarInt(tex.width);
            buf.writeVarInt(tex.height);
            buf.writeVarInt(texFormat);
            buf.writeVarInt(tex.unknownFlag);

            buf.writeVarInt(tex.subTextures.size());
            for (RawYsmModel.RawTexture.SubTexture sub : tex.subTextures) {
                buf.writeVarInt(sub.specularType);
                byte[] subData = sub.data;
                int subFormat = sub.imageFormat;
                if (subFormat == -1) {
                    subData = convertRgbaToPng(subData, sub.width, sub.height);
                    subFormat = 2;
                }
                writeSpecialImage(buf, sub.hash, subData);
                buf.writeVarInt(sub.width);
                buf.writeVarInt(sub.height);
                buf.writeVarInt(subFormat);
                buf.writeVarInt(sub.unknownFlag);
            }
        }
    }

    private static void writeSoundFiles(YSMByteBuf buf, Map<String, RawYsmModel.RawDataFile> sounds, int format) {
        buf.writeVarInt(sounds.size());
        for (Map.Entry<String, RawYsmModel.RawDataFile> entry : sounds.entrySet()) {
            buf.writeString(entry.getKey());
            if (format > 15) {
                buf.writeString(entry.getValue().hash != null ? entry.getValue().hash : "");
            }
            buf.writeByteArray(entry.getValue().data);
        }
    }

    private static void writeFunctionFiles(YSMByteBuf buf, Map<String, RawYsmModel.RawDataFile> functions) {
        buf.writeVarInt(functions.size());
        for (Map.Entry<String, RawYsmModel.RawDataFile> entry : functions.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeString(entry.getValue().hash != null ? entry.getValue().hash : "");
            buf.writeByteArray(entry.getValue().data);
        }
    }

    private static void writeLanguageFiles(YSMByteBuf buf, Map<String, RawYsmModel.RawLanguageFile> languages) {
        buf.writeVarInt(languages.size());
        for (Map.Entry<String, RawYsmModel.RawLanguageFile> entry : languages.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeString(entry.getValue().hash != null ? entry.getValue().hash : "");
            Map<String, String> nodes = entry.getValue().data;
            buf.writeVarInt(nodes.size());
            for (Map.Entry<String, String> kv : nodes.entrySet()) {
                buf.writeString(kv.getKey());
                buf.writeString(kv.getValue());
            }
        }
    }

    private static void writeSpecialImage(YSMByteBuf buf, String hash, byte[] data) {
        buf.writeString(hash != null ? hash : "");
        buf.writeByteArray(data);
    }

    private static void writeVector3D(YSMByteBuf buf, float[] vec) {
        buf.writeFloat(vec[0]);
        buf.writeFloat(vec[1]);
        buf.writeFloat(vec[2]);
    }

    private static void writeYsmJson(YSMByteBuf buf, RawYsmModel model, int format) {
        RawYsmModel.RawProperties props = model.properties;
        RawYsmModel.RawMetadata meta = model.metadata;

        buf.writeString(props.sha256 != null ? props.sha256 : "");
        buf.writeVarInt(1); // isNewVersionYsm

        buf.writeString(meta.name != null ? meta.name : "");
        buf.writeString(meta.tips != null ? meta.tips : "");
        buf.writeString(meta.licenseType != null ? meta.licenseType : "");
        buf.writeString(meta.licenseDescription != null ? meta.licenseDescription : "");

        buf.writeVarInt(meta.authors.size());
        for (RawYsmModel.RawMetadata.Author author : meta.authors) {
            buf.writeString(author.name != null ? author.name : "");
            buf.writeString(author.role != null ? author.role : "");
            buf.writeVarInt(author.contacts.size());
            for (Map.Entry<String, String> c : author.contacts.entrySet()) {
                buf.writeString(c.getKey());
                buf.writeString(c.getValue());
            }
            buf.writeString(author.comment != null ? author.comment : "");
        }

        buf.writeVarInt(meta.links.size());
        for (Map.Entry<String, String> l : meta.links.entrySet()) {
            buf.writeString(l.getKey());
            buf.writeString(l.getValue());
        }

        buf.writeFloat(props.widthScale);
        buf.writeFloat(props.heightScale);

        // extra_animation
        buf.writeVarInt(props.extraAnimations.size());
        for (Map.Entry<String, String> e : props.extraAnimations.entrySet()) {
            buf.writeString(e.getKey());
            buf.writeString(e.getValue());
        }

        if (format > 9) {
            // extra_animation_buttons
            buf.writeVarInt(props.extraAnimationButtons.size());
            for (RawYsmModel.ExtraAnimationButton btn : props.extraAnimationButtons) {
                buf.writeString(btn.id != null ? btn.id : "");
                buf.writeString(btn.name != null ? btn.name : "");
                buf.writeVarInt(0); // buttonPadding

                buf.writeVarInt(btn.forms.size());
                for (RawYsmModel.ConfigForm form : btn.forms) {
                    buf.writeString(form.type != null ? form.type : "");
                    buf.writeString(form.title != null ? form.title : "");
                    buf.writeString(form.description != null ? form.description : "");
                    buf.writeString(form.defaultValue != null ? form.defaultValue : "");
                    buf.writeFloat(form.step);
                    buf.writeFloat(form.min);
                    buf.writeFloat(form.max);
                    buf.writeVarInt(form.labels.size());
                    for (Map.Entry<String, String> lb : form.labels.entrySet()) {
                        buf.writeString(lb.getKey());
                        buf.writeString(lb.getValue());
                    }
                }
            }

            // extra_animation_classify
            buf.writeVarInt(props.extraAnimationClassifies.size());
            for (RawYsmModel.ExtraAnimationClassify cls : props.extraAnimationClassifies) {
                buf.writeString(cls.id != null ? cls.id : "");
                buf.writeVarInt(cls.extras.size());
                for (Map.Entry<String, String> e : cls.extras.entrySet()) {
                    buf.writeString(e.getKey());
                    buf.writeString(e.getValue());
                }
            }
        }

        buf.writeString(props.defaultTexture != null ? props.defaultTexture : "");
        buf.writeString(props.previewAnimation != null ? props.previewAnimation : "");
        buf.writeVarInt(props.isFree ? 1 : 0);

        if (format > 4) {
            buf.writeVarInt(props.renderLayersFirst ? 1 : 0);
        }
        if (format >= 15) {
            buf.writeVarInt(props.allCutout ? 1 : 0);
            buf.writeVarInt(props.disablePreviewRotation ? 1 : 0);
        }

        if (format > 15) {
            buf.writeVarInt(props.guiNoLighting ? 1 : 0);
            if (format >= 32) {
                buf.writeVarInt(props.mergeMultilineExpr ? 1 : 0);
            }
            buf.writeString(props.guiForeground != null ? props.guiForeground : "");
            buf.writeString(props.guiBackground != null ? props.guiBackground : "");

            // 头像
            List<RawYsmModel.RawImage> avatars = new ArrayList<>();
            for (RawYsmModel.RawMetadata.Author author : meta.authors) {
                if (author.avatarImage != null) {
                    avatars.add(author.avatarImage);
                }
            }
            avatars.addAll(meta.extraAvatars);
            buf.writeVarInt(avatars.size());
            for (RawYsmModel.RawImage img : avatars) {
                buf.writeString(img.name != null ? img.name : "");
                byte[] imgData = img.data;
                int imgFormat = img.format;
                if (imgFormat == -1) {
                    imgData = convertRgbaToPng(imgData, img.width, img.height);
                    imgFormat = 2;
                }
                buf.writeByteArray(imgData);
                buf.writeVarInt(img.width);
                buf.writeVarInt(img.height);
                buf.writeVarInt(imgFormat);
                buf.writeVarInt(img.unknownFlag);
            }
        }

        // 背景图
        if (format > 15) {
            buf.writeVarInt(props.backgroundImages.size());
            for (RawYsmModel.RawImage bg : props.backgroundImages) {
                buf.writeString(bg.name != null ? bg.name : "");
                byte[] bgData = bg.data;
                int bgFormat = bg.format;
                if (bgFormat == -1) {
                    bgData = convertRgbaToPng(bgData, bg.width, bg.height);
                    bgFormat = 2;
                }
                buf.writeByteArray(bgData);
                buf.writeVarInt(bg.width);
                buf.writeVarInt(bg.height);
                buf.writeVarInt(bgFormat);
                buf.writeVarInt(bg.unknownFlag);
            }
        }
    }

    private static byte[] convertRgbaToPng(byte[] rgbaData, int width, int height) {
        if (rgbaData == null || width <= 0 || height <= 0 || rgbaData.length < width * height * 4) {
            return rgbaData;
        }

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];

        for (int i = 0; i < pixels.length; i++) {
            int r = rgbaData[i * 4] & 0xFF;
            int g = rgbaData[i * 4 + 1] & 0xFF;
            int b = rgbaData[i * 4 + 2] & 0xFF;
            int a = rgbaData[i * 4 + 3] & 0xFF;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        img.setRGB(0, 0, width, height, pixels, 0, width);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return rgbaData;
        }
    }
}