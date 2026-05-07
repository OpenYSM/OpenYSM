package com.elfmcys.yesstevemodel.resource;

import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import com.google.gson.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import rip.ysm.imagestream.avif.AvifDecoder;
import rip.ysm.imagestream.jpeg.JpegDecoder;
import rip.ysm.imagestream.webp.WebpDecoder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

import static rip.ysm.util.HashUtils.md5Hex;
import static rip.ysm.util.HashUtils.sha256Hex;

public class YSMFolderDeserializer implements AutoCloseable {
    private final Map<String, String> readFilesMd5Map = new TreeMap<>();
    private String finalFolderHash;
    private final Path rootPath;
    private final FileSystem zipFileSystem;
    private final RawYsmModel model;

    public YSMFolderDeserializer(Path sourcePath) throws IOException {
        if (!Files.exists(sourcePath)) {
            throw new FileNotFoundException("Model source not found: " + sourcePath);
        }

        if (Files.isDirectory(sourcePath)) {
            this.rootPath = sourcePath;
            this.zipFileSystem = null;
        } else if (sourcePath.toString().endsWith(".zip") || sourcePath.toString().endsWith(".ysm")) {
            URI uri = URI.create("jar:" + sourcePath.toUri());
            this.zipFileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            this.rootPath = this.zipFileSystem.getPath("/");
        } else {
            throw new IllegalArgumentException("Unsupported file type. Expected directory or .zip");
        }

        this.model = new RawYsmModel();
        this.model.formatVersion = 65535;
    }

    private byte[] readResource(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return null;
        try {
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            Path target = rootPath.resolve(relativePath);
            if (Files.exists(target) && Files.isRegularFile(target)) {
                byte[] data = Files.readAllBytes(target);

                String normalizedPath = relativePath.replace('\\', '/');
                if (!readFilesMd5Map.containsKey(normalizedPath)) {
                    readFilesMd5Map.put(normalizedPath, md5Hex(data));
                }

                return data;
            }
        } catch (Exception e) {
            System.err.println("[YSM] Warning: Failed to read resource: " + relativePath);
        }
        return null;
    }

    public RawYsmModel deserialize() {
        byte[] ysmJsonBytes = readResource("ysm.json");
        if (ysmJsonBytes == null) {
            throw new RuntimeException("Missing ysm.json in the provided source");
        }

        String jsonStr = new String(ysmJsonBytes, StandardCharsets.UTF_8);
        JsonObject ysmJson = JsonParser.parseString(jsonStr).getAsJsonObject();

        parseYsmJson(ysmJson);
        parseGlobalResources();

        this.finalFolderHash = calculateFinalFolderHash();
        model.properties.sha256 = finalFolderHash;

        model.footer.version = 65535;
        return model;
    }

    @Override
    public void close() throws IOException {
        if (this.zipFileSystem != null) {
            this.zipFileSystem.close();
        }
    }

    private void parseYsmJson(JsonObject ysmJson) {
        if (ysmJson.has("metadata")) parseMetadata(ysmJson.getAsJsonObject("metadata"));
        if (ysmJson.has("properties")) parseProperties(ysmJson.getAsJsonObject("properties"));
        if (ysmJson.has("files")) {
            JsonObject files = ysmJson.getAsJsonObject("files");
            if (files.has("player")) parseMainEntity(files.getAsJsonObject("player"));
            if (files.has("vehicles")) parseSubEntities(files.get("vehicles"), model.vehicles, "vehicle");
            if (files.has("projectiles")) parseSubEntities(files.get("projectiles"), model.projectiles, "projectile");
        }
    }

    private void parseMetadata(JsonObject metaObj) {
        model.metadata.name = getStr(metaObj, "name", "");
        model.metadata.tips = getStr(metaObj, "tips", "");
        if (metaObj.has("license") && metaObj.get("license").isJsonObject()) {
            JsonObject licObj = metaObj.getAsJsonObject("license");
            model.metadata.licenseType = getStr(licObj, "type", "");
            model.metadata.licenseDescription = getStr(licObj, "desc", "");
        }

        if (metaObj.has("authors") && metaObj.get("authors").isJsonArray()) {
            for (JsonElement elem : metaObj.getAsJsonArray("authors")) {
                if (!elem.isJsonObject()) continue;
                JsonObject authorObj = elem.getAsJsonObject();
                RawYsmModel.RawMetadata.Author author = new RawYsmModel.RawMetadata.Author();
                author.name = getStr(authorObj, "name", "");
                author.role = getStr(authorObj, "role", "");
                author.comment = getStr(authorObj, "comment", "");

                if (authorObj.has("contact") && authorObj.get("contact").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> cEntry : authorObj.getAsJsonObject("contact").entrySet()) {
                        author.contacts.put(cEntry.getKey(), cEntry.getValue().getAsString());
                    }
                }

                if (authorObj.has("avatar")) {
                    String avatarPath = getStr(authorObj, "avatar", "");
                    if (!avatarPath.isEmpty()) {
                        byte[] avatarData = readResource(avatarPath);
                        if (avatarData != null) {
                            ImageMeta meta = parseImageMeta(avatarData, avatarPath);
                            RawYsmModel.RawImage img = new RawYsmModel.RawImage();
                            img.width = meta.width();
                            img.height = meta.height();
                            img.format = meta.format();
                            img.name = author.name;
                            img.data = avatarData;
                            img.isPng = (meta.format() == 2);
                            img.unknownFlag = 1;

                            author.avatar = avatarPath;
                            author.avatarImage = img;
                        }
                    }
                }
                model.metadata.authors.add(author);
            }
        }

        if (metaObj.has("link") && metaObj.get("link").isJsonObject()) {
            for (Map.Entry<String, JsonElement> linkEntry : metaObj.getAsJsonObject("link").entrySet()) {
                model.metadata.links.put(linkEntry.getKey(), linkEntry.getValue().getAsString());
            }
        }
    }

    private void parseProperties(JsonObject propsObj) {
        model.properties.widthScale = (float) getDouble(propsObj, "width_scale", 0.7);
        model.properties.heightScale = (float) getDouble(propsObj, "height_scale", 0.7);
        model.properties.defaultTexture = getStr(propsObj, "default_texture", "default");
        model.properties.previewAnimation = getStr(propsObj, "preview_animation", "");
        model.properties.isFree = getBool(propsObj, "free", false);
        model.properties.renderLayersFirst = getBool(propsObj, "render_layers_first", false);
        model.properties.allCutout = getBool(propsObj, "all_cutout", false);
        model.properties.disablePreviewRotation = getBool(propsObj, "disable_preview_rotation", false);
        model.properties.guiNoLighting = getBool(propsObj, "gui_no_lighting", false);
        model.properties.mergeMultilineExpr = getBool(propsObj, "merge_multiline_expr", false);
        model.properties.guiForeground = getStr(propsObj, "gui_foreground", "");
        model.properties.guiBackground = getStr(propsObj, "gui_background", "");
        if (propsObj.has("extra_animation") && propsObj.get("extra_animation").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : propsObj.getAsJsonObject("extra_animation").entrySet()) {
                model.properties.extraAnimations.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        if (propsObj.has("extra_animation_classify") && propsObj.get("extra_animation_classify").isJsonArray()) {
            for (JsonElement elem : propsObj.getAsJsonArray("extra_animation_classify")) {
                if (!elem.isJsonObject()) continue;
                JsonObject clsObj = elem.getAsJsonObject();
                RawYsmModel.ExtraAnimationClassify classify = new RawYsmModel.ExtraAnimationClassify();
                classify.id = getStr(clsObj, "id", "");
                if (clsObj.has("extra_animation") && clsObj.get("extra_animation").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> entry : clsObj.getAsJsonObject("extra_animation").entrySet()) {
                        classify.extras.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
                model.properties.extraAnimationClassifies.add(classify);
            }
        }

        if (propsObj.has("extra_animation_buttons") && propsObj.get("extra_animation_buttons").isJsonArray()) {
            for (JsonElement elem : propsObj.getAsJsonArray("extra_animation_buttons")) {
                if (!elem.isJsonObject()) continue;
                JsonObject btnObj = elem.getAsJsonObject();
                RawYsmModel.ExtraAnimationButton btn = new RawYsmModel.ExtraAnimationButton();
                btn.id = getStr(btnObj, "id", "");
                btn.name = getStr(btnObj, "name", "");
                btn.description = getStr(btnObj, "description", "");

                if (btnObj.has("config_forms") && btnObj.get("config_forms").isJsonArray()) {
                    for (JsonElement formElem : btnObj.getAsJsonArray("config_forms")) {
                        if (!formElem.isJsonObject()) continue;
                        JsonObject formObj = formElem.getAsJsonObject();
                        RawYsmModel.ConfigForm form = new RawYsmModel.ConfigForm();
                        form.type = getStr(formObj, "type", "");
                        form.title = getStr(formObj, "title", "");
                        form.description = getStr(formObj, "description", "");
                        form.defaultValue = getStr(formObj, "value", "");
                        form.step = (float) getDouble(formObj, "step", 0);
                        form.min = (float) getDouble(formObj, "min", 0);
                        form.max = (float) getDouble(formObj, "max", 0);
                        if (formObj.has("labels") && formObj.get("labels").isJsonObject()) {
                            for (Map.Entry<String, JsonElement> lEntry : formObj.getAsJsonObject("labels").entrySet()) {
                                form.labels.put(lEntry.getKey(), lEntry.getValue().getAsString());
                            }
                        }
                        btn.forms.add(form);
                    }
                }
                model.properties.extraAnimationButtons.add(btn);
            }
        }

        loadGuiImage(model.properties.guiBackground, "gui_background");
        loadGuiImage(model.properties.guiForeground, "gui_foreground");
    }

    private void loadGuiImage(String path, String id) {
        if (path == null || path.isEmpty()) return;
        byte[] data = readResource(path);
        if (data == null) data = readResource("background/" + id + ".png");

        if (data != null) {
            ImageMeta meta = parseImageMeta(data, path);
            RawYsmModel.RawImage img = new RawYsmModel.RawImage();
            img.width = meta.width();
            img.height = meta.height();
            img.format = meta.format();
            img.name = id;
            img.data = data;
            img.isPng = (meta.format() == 2);
            img.unknownFlag = 1;
            model.properties.backgroundImages.add(img);
        }
    }

    private void parseMainEntity(JsonObject playerObj) {
        if (playerObj.has("model") && playerObj.get("model").isJsonObject()) {
            JsonObject modelObj = playerObj.getAsJsonObject("model");
            if (modelObj.has("main")) {
                byte[] geoData = readResource(modelObj.get("main").getAsString());
                if (geoData != null) model.mainEntity.mainModel = parseGeometry(geoData, 1);
            }
            if (modelObj.has("arm")) {
                byte[] geoData = readResource(modelObj.get("arm").getAsString());
                if (geoData != null) model.mainEntity.armModel = parseGeometry(geoData, 2);
            }
        }

        if (playerObj.has("texture")) {
            JsonElement texElem = playerObj.get("texture");
            Iterable<JsonElement> texArr = texElem.isJsonArray() ? texElem.getAsJsonArray() : Collections.singletonList(texElem);
            for (JsonElement elem : texArr) {
                String texPath = null;
                if (elem.isJsonPrimitive()) {
                    texPath = elem.getAsString();
                } else if (elem.isJsonObject() && elem.getAsJsonObject().has("uv")) {
                    texPath = elem.getAsJsonObject().get("uv").getAsString();
                }
                if (texPath == null) continue;

                byte[] texData = readResource(texPath);
                if (texData != null) {
                    ImageMeta meta = parseImageMeta(texData, texPath);
                    RawYsmModel.RawTexture rt = new RawYsmModel.RawTexture();
                    rt.hash = sha256Hex(texData); // 计算原始数据的 hash
                    rt.width = meta.width();
                    rt.height = meta.height();
                    rt.imageFormat = meta.format();
                    rt.name = extractFileName(texPath);
                    rt.data = texData;
                    rt.unknownFlag = 1;

                    if (elem.isJsonObject()) {
                        JsonObject obj = elem.getAsJsonObject();
                        if (obj.has("specular")) {
                            byte[] spData = readResource(obj.get("specular").getAsString());
                            if (spData != null) {
                                ImageMeta spMeta = parseImageMeta(spData, "specular");
                                RawYsmModel.RawTexture.SubTexture sub = new RawYsmModel.RawTexture.SubTexture();
                                sub.specularType = 2;
                                sub.data = spData;
                                sub.unknownFlag = 1;
                                sub.hash = sha256Hex(spData);
                                sub.width = spMeta.width();
                                sub.height = spMeta.height();
                                sub.imageFormat = spMeta.format();
                                rt.subTextures.add(sub);
                            }
                        }
                        if (obj.has("normal")) {
                            byte[] nrData = readResource(obj.get("normal").getAsString());
                            if (nrData != null) {
                                ImageMeta nrMeta = parseImageMeta(nrData, "normal");
                                RawYsmModel.RawTexture.SubTexture sub = new RawYsmModel.RawTexture.SubTexture();
                                sub.specularType = 1;
                                sub.data = nrData;
                                sub.unknownFlag = 1;
                                sub.hash = sha256Hex(nrData);
                                sub.width = nrMeta.width();
                                sub.height = nrMeta.height();
                                sub.imageFormat = nrMeta.format();
                                rt.subTextures.add(sub);
                            }
                        }
                    }
                    model.mainEntity.textures.put(rt.name, rt);
                }
            }
        }

        if (playerObj.has("animation") && playerObj.get("animation").isJsonObject()) {
            JsonObject animObj = playerObj.getAsJsonObject("animation");
            for (Map.Entry<String, JsonElement> entry : animObj.entrySet()) {
                byte[] animData = readResource(entry.getValue().getAsString());
                if (animData != null) {
                    RawYsmModel.RawAnimationFile raf = parseAnimations(animData);
                    raf.fileHash = sha256Hex(animData);
                    raf.animType = getAnimTypeFromKey(entry.getKey());
                    model.mainEntity.animationFiles.put(entry.getKey(), raf);
                }
            }
        }
        if (playerObj.has("animation_controllers") && playerObj.get("animation_controllers").isJsonArray()) {
            for (JsonElement acElem : playerObj.getAsJsonArray("animation_controllers")) {
                byte[] acData = readResource(acElem.getAsString());
                if (acData != null) {
                    String acHash = sha256Hex(acData);
                    parseAnimationControllers(acData, acHash, model.mainEntity.animationControllers);
                }
            }
        }
    }

    private void parseSubEntities(JsonElement sectionElem, Map<String, RawYsmModel.RawSubEntity> targetMap, String defaultIdentifier) {
        if (!sectionElem.isJsonArray() && !sectionElem.isJsonObject()) return;
        List<JsonObject> items = new ArrayList<>();

        if (sectionElem.isJsonArray()) {
            for (JsonElement e : sectionElem.getAsJsonArray()) {
                if (e.isJsonObject()) items.add(e.getAsJsonObject());
            }
        } else {
            JsonObject mapObj = sectionElem.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : mapObj.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject item = entry.getValue().getAsJsonObject();
                    if (!item.has("match")) item.addProperty("__temp_identifier", entry.getKey());
                    items.add(item);
                }
            }
        }

        int index = 0;
        for (JsonObject item : items) {
            RawYsmModel.RawSubEntity sub = new RawYsmModel.RawSubEntity();
            sub.identifier = item.has("__temp_identifier") ? item.get("__temp_identifier").getAsString() : (defaultIdentifier + "_" + index);

            if (item.has("match")) {
                JsonElement match = item.get("match");
                if (match.isJsonArray()) {
                    JsonArray mArr = match.getAsJsonArray();
                    sub.matchIds = new String[mArr.size()];
                    for (int i = 0; i < mArr.size(); i++) sub.matchIds[i] = mArr.get(i).getAsString();
                } else if (match.isJsonPrimitive()) {
                    sub.matchIds = new String[]{match.getAsString()};
                }
            }

            if (item.has("model")) {
                byte[] geoData = readResource(item.get("model").getAsString());
                if (geoData != null) sub.model = parseGeometry(geoData, 3);
            }

            if (item.has("texture")) {
                String texPath = item.get("texture").isJsonObject() ? item.getAsJsonObject("texture").get("uv").getAsString() : item.get("texture").getAsString();
                byte[] texData = readResource(texPath);
                if (texData != null) {
                    ImageMeta meta = parseImageMeta(texData, texPath);
                    RawYsmModel.RawTexture rt = new RawYsmModel.RawTexture();

                    rt.hash = sha256Hex(texData);
                    rt.width = meta.width();
                    rt.height = meta.height();
                    rt.imageFormat = meta.format();

                    rt.name = "base_texture_" + index;
                    rt.data = texData;
                    rt.unknownFlag = 1;
                    sub.textures.put(rt.name, rt);
                }
            }

            if (item.has("animation")) {
                byte[] animData = readResource(item.get("animation").getAsString());
                if (animData != null) {
                    RawYsmModel.RawAnimationFile raf = parseAnimations(animData);
                    raf.fileHash = sha256Hex(animData);
                    raf.animType = getAnimTypeFromKey("extra");
                    sub.animationFiles.put("sub_anim", raf);
                }
            }
            if (item.has("animation_controllers")) {
                JsonElement controllers = item.get("animation_controllers");
                if (controllers.isJsonArray()) {
                    for (JsonElement acElem : controllers.getAsJsonArray()) {
                        byte[] acData = readResource(acElem.getAsString());
                        if (acData != null) {
                            parseAnimationControllers(acData, sha256Hex(acData), sub.animationControllers);
                        }
                    }
                } else if (controllers.isJsonPrimitive()) {
                    byte[] acData = readResource(controllers.getAsString());
                    if (acData != null) {
                        parseAnimationControllers(acData, sha256Hex(acData), sub.animationControllers);
                    }
                }
            }

            targetMap.put(sub.identifier, sub);
            index++;
        }
    }

    private RawYsmModel.RawGeometry parseGeometry(byte[] data, int modelType) {
        String json = new String(data, StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray geometries = root.has("minecraft:geometry") ? root.getAsJsonArray("minecraft:geometry") : null;
        if (geometries == null || geometries.isEmpty()) return new RawYsmModel.RawGeometry();

        JsonObject geoObj = geometries.get(0).getAsJsonObject();
        RawYsmModel.RawGeometry geo = new RawYsmModel.RawGeometry();
        geo.sha256 = sha256Hex(data);

        geo.modelType = modelType;
        geo.unkFloat1 = 0.7f;
        geo.unkFloat2 = 0.7f;

        if (geoObj.has("description")) {
            JsonObject desc = geoObj.getAsJsonObject("description");
            geo.identifier = getStr(desc, "identifier", "");
            geo.textureWidth = (float) getDouble(desc, "texture_width", 64.0);
            geo.textureHeight = (float) getDouble(desc, "texture_height", 64.0);
            geo.visibleBoundsWidth = (float) getDouble(desc, "visible_bounds_width", 0);
            geo.visibleBoundsHeight = (float) getDouble(desc, "visible_bounds_height", 0);
            if (desc.has("visible_bounds_offset") && desc.get("visible_bounds_offset").isJsonArray()) {
                JsonArray offsetArr = desc.getAsJsonArray("visible_bounds_offset");
                geo.visibleBoundsOffset = new float[offsetArr.size()];
                for (int i = 0; i < offsetArr.size(); i++) geo.visibleBoundsOffset[i] = offsetArr.get(i).getAsFloat();
            } else {
                geo.visibleBoundsOffset = new float[0];
            }
        }

        if (geoObj.has("bones") && geoObj.get("bones").isJsonArray()) {
            for (JsonElement boneElem : geoObj.getAsJsonArray("bones")) {
                if (!boneElem.isJsonObject()) continue;
                JsonObject bObj = boneElem.getAsJsonObject();
                RawYsmModel.RawBone bone = new RawYsmModel.RawBone();
                bone.name = getStr(bObj, "name", "");
                bone.parentName = getStr(bObj, "parent", "");

                if (bObj.has("pivot")) {
                    JsonArray pivot = bObj.getAsJsonArray("pivot");
                    bone.pivot = new float[]{-pivot.get(0).getAsFloat(), pivot.get(1).getAsFloat(), pivot.get(2).getAsFloat()};
                }
                if (bObj.has("rotation")) {
                    JsonArray rot = bObj.getAsJsonArray("rotation");
                    bone.rotation = new float[]{(float) -Math.toRadians(rot.get(0).getAsFloat()), (float) -Math.toRadians(rot.get(1).getAsFloat()), (float) Math.toRadians(rot.get(2).getAsFloat())};
                }

                float boneInflate = (float) getDouble(bObj, "inflate", 0.0);
                boolean boneMirror = getBool(bObj, "mirror", false);

                if (bObj.has("cubes") && bObj.get("cubes").isJsonArray()) {
                    for (JsonElement cElem : bObj.getAsJsonArray("cubes")) {
                        if (!cElem.isJsonObject()) continue;
                        JsonObject cObj = cElem.getAsJsonObject();
                        RawYsmModel.RawCube cube = new RawYsmModel.RawCube();

                        float inflate = cObj.has("inflate") ? cObj.get("inflate").getAsFloat() : boneInflate;
                        boolean mirror = cObj.has("mirror") ? cObj.get("mirror").getAsBoolean() : boneMirror;

                        float[] origin = getFloatArray(cObj, "origin", 3);
                        float[] size = getFloatArray(cObj, "size", 3);

                        float cx = -origin[0] - size[0] - inflate;
                        float cy = origin[1] - inflate;
                        float cz = origin[2] - inflate;
                        float cw = size[0] + inflate * 2;
                        float ch = size[1] + inflate * 2;
                        float cd = size[2] + inflate * 2;

                        Matrix4f cubeBakeMat = new Matrix4f();
                        if (cObj.has("rotation") || cObj.has("pivot")) {
                            float[] cpvt = getFloatArray(cObj, "pivot", 3);
                            float[] crot = getFloatArray(cObj, "rotation", 3);
                            cubeBakeMat.translate(-cpvt[0] / 16f, cpvt[1] / 16f, cpvt[2] / 16f);
                            cubeBakeMat.rotateZ((float) Math.toRadians(crot[2]));
                            cubeBakeMat.rotateY((float) -Math.toRadians(crot[1]));
                            cubeBakeMat.rotateX((float) -Math.toRadians(crot[0]));
                            cubeBakeMat.translate(cpvt[0] / 16f, -cpvt[1] / 16f, -cpvt[2] / 16f);
                        }
                        Matrix3f cubeNormalMat = new Matrix3f();
                        cubeBakeMat.normal(cubeNormalMat);

                        if (cObj.has("uv")) {
                            JsonElement uvElem = cObj.get("uv");
                            if (uvElem.isJsonObject()) {
                                JsonObject uvObj = uvElem.getAsJsonObject();
                                bakeFaceToRaw(cube, uvObj, "north", "north", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(0, 0, -1), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, uvObj, "south", "south", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(0, 0, 1), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, uvObj, "east", mirror ? "west" : "east", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(1, 0, 0), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, uvObj, "west", mirror ? "east" : "west", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(-1, 0, 0), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, uvObj, "up", "up", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(0, 1, 0), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, uvObj, "down", "down", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(0, -1, 0), cubeBakeMat, cubeNormalMat);
                            } else if (uvElem.isJsonArray()) {
                                JsonArray uvArr = uvElem.getAsJsonArray();
                                float uvX = uvArr.get(0).getAsFloat();
                                float uvY = uvArr.get(1).getAsFloat();
                                float dx = (float) Math.floor(size[0]);
                                float dy = (float) Math.floor(size[1]);
                                float dz = (float) Math.floor(size[2]);

                                JsonObject fakeUvObj = new JsonObject();
                                fakeUvObj.add("north", createFaceUVNode(uvX + dz, uvY + dz, dx, dy));
                                fakeUvObj.add("south", createFaceUVNode(uvX + dz + dx + dz, uvY + dz, dx, dy));
                                fakeUvObj.add("east", createFaceUVNode(uvX, uvY + dz, dz, dy));
                                fakeUvObj.add("west", createFaceUVNode(uvX + dz + dx, uvY + dz, dz, dy));
                                fakeUvObj.add("up", createFaceUVNode(uvX + dz, uvY, dx, dz));
                                fakeUvObj.add("down", createFaceUVNode(uvX + dz + dx, uvY + dz, dx, -dz));

                                bakeFaceToRaw(cube, fakeUvObj, "north", "north", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(0, 0, -1), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, fakeUvObj, "south", "south", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(0, 0, 1), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, fakeUvObj, "east", mirror ? "west" : "east", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(1, 0, 0), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, fakeUvObj, "west", mirror ? "east" : "west", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(-1, 0, 0), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, fakeUvObj, "up", "up", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(0, 1, 0), cubeBakeMat, cubeNormalMat);
                                bakeFaceToRaw(cube, fakeUvObj, "down", "down", mirror, cx, cy, cz, cw, ch, cd, geo.textureWidth, geo.textureHeight, new Vector3f(0, -1, 0), cubeBakeMat, cubeNormalMat);
                            }
                        }
                        bone.cubes.add(cube);
                    }
                }
                geo.bones.add(bone);
            }
        }
        return geo;
    }

    private void bakeFaceToRaw(RawYsmModel.RawCube cube, JsonObject uvObj, String faceType, String uvFaceName, boolean mirror, float x, float y, float z, float w, float h, float d, float tw, float th, Vector3f rawNormal, Matrix4f cubeBakeMat, Matrix3f cubeNormalMat) {
        if (!uvObj.has(uvFaceName)) return;
        JsonObject faceData = uvObj.getAsJsonObject(uvFaceName);
        float[] uv = getFloatArray(faceData, "uv", 2);
        float[] uvSize = getFloatArray(faceData, "uv_size", 2);

        float u0 = uv[0] / tw;
        float v0 = uv[1] / th;
        float u1 = (uv[0] + uvSize[0]) / tw;
        float v1 = (uv[1] + uvSize[1]) / th;

        if (!mirror) {
            float temp = u0; u0 = u1; u1 = temp;
        }

        RawYsmModel.RawFace face = new RawYsmModel.RawFace();
        Vector3f bakedNormal = new Vector3f(rawNormal).mul(cubeNormalMat).normalize();
        face.normal = new float[]{bakedNormal.x, bakedNormal.y, bakedNormal.z};

        float x1 = x / 16f, x2 = (x + w) / 16f;
        float y1 = y / 16f, y2 = (y + h) / 16f;
        float z1 = z / 16f, z2 = (z + d) / 16f;

        Vector3f p1 = new Vector3f(x1, y1, z1);
        Vector3f p2 = new Vector3f(x1, y1, z2);
        Vector3f p3 = new Vector3f(x1, y2, z1);
        Vector3f p4 = new Vector3f(x1, y2, z2);
        Vector3f p5 = new Vector3f(x2, y1, z1);
        Vector3f p6 = new Vector3f(x2, y1, z2);
        Vector3f p7 = new Vector3f(x2, y2, z1);
        Vector3f p8 = new Vector3f(x2, y2, z2);

        Vector3f[] positions = switch (faceType) {
            case "west" -> new Vector3f[]{p4, p3, p1, p2};
            case "east" -> new Vector3f[]{p7, p8, p6, p5};
            case "north" -> new Vector3f[]{p3, p7, p5, p1};
            case "south" -> new Vector3f[]{p8, p4, p2, p6};
            case "up" -> new Vector3f[]{p4, p8, p7, p3};
            case "down" -> new Vector3f[]{p1, p5, p6, p2};
            default -> null;
        };

        Vector4f tempPos = new Vector4f();
        for (int i = 0; i < 4; i++) {
            tempPos.set(positions[i].x(), positions[i].y(), positions[i].z(), 1.0f).mul(cubeBakeMat);
            face.positions[i] = new float[]{tempPos.x(), tempPos.y(), tempPos.z()};
        }

        face.u = new float[]{u0, u1, u1, u0};
        face.v = new float[]{v0, v0, v1, v1};
        cube.faces.add(face);
    }

    private JsonObject createFaceUVNode(float u, float v, float w, float h) {
        JsonObject node = new JsonObject();
        JsonArray uv = new JsonArray(); uv.add(u); uv.add(v);
        JsonArray size = new JsonArray(); size.add(w); size.add(h);
        node.add("uv", uv);
        node.add("uv_size", size);
        return node;
    }

    private RawYsmModel.RawAnimationFile parseAnimations(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        RawYsmModel.RawAnimationFile raf = new RawYsmModel.RawAnimationFile();

        if (root.has("animations")) {
            JsonObject anims = root.getAsJsonObject("animations");
            for (Map.Entry<String, JsonElement> entry : anims.entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                JsonObject aObj = entry.getValue().getAsJsonObject();
                RawYsmModel.RawAnimation anim = new RawYsmModel.RawAnimation();
                anim.name = entry.getKey();
                anim.length = (float) getDouble(aObj, "animation_length", Float.POSITIVE_INFINITY);

                if (aObj.has("loop")) {
                    String loopStr = aObj.get("loop").getAsString();
                    if ("true".equals(loopStr)) anim.loopMode = 1;
                    else if ("hold_on_last_frame".equals(loopStr)) anim.loopMode = 3;
                    else anim.loopMode = 0;
                } else {
                    anim.loopMode = 2;
                }

                if (aObj.has("blend_weight")) {
                    JsonElement bw = aObj.get("blend_weight");
                    if (bw.isJsonPrimitive() && bw.getAsJsonPrimitive().isNumber()) {
                        anim.blendWeight = bw.getAsFloat();
                    } else {
                        anim.blendWeight = bw.getAsString();
                    }
                }

                if (aObj.has("bones") && aObj.get("bones").isJsonObject()) {
                    JsonObject bonesObj = aObj.getAsJsonObject("bones");
                    for (Map.Entry<String, JsonElement> bEntry : bonesObj.entrySet()) {
                        if (!bEntry.getValue().isJsonObject()) continue;
                        JsonObject bObj = bEntry.getValue().getAsJsonObject();
                        RawYsmModel.RawBoneAnimation ba = new RawYsmModel.RawBoneAnimation();
                        ba.boneName = bEntry.getKey();

                        parseChannelToKeyframes(bObj, "rotation", ba.rotation);
                        parseChannelToKeyframes(bObj, "position", ba.position);
                        parseChannelToKeyframes(bObj, "scale", ba.scale);

                        anim.boneAnimations.add(ba);
                    }
                }

                if (aObj.has("timeline") && aObj.get("timeline").isJsonObject()) {
                    JsonObject tlObj = aObj.getAsJsonObject("timeline");
                    for (Map.Entry<String, JsonElement> tlEntry : tlObj.entrySet()) {
                        RawYsmModel.RawTimelineEvent tle = new RawYsmModel.RawTimelineEvent();
                        tle.timestamp = Float.parseFloat(tlEntry.getKey());
                        JsonElement val = tlEntry.getValue();
                        Iterable<JsonElement> arr = val.isJsonArray() ? val.getAsJsonArray() : Collections.singletonList(val);
                        for (JsonElement e : arr) tle.events.add(e.getAsString());
                        anim.timelineEvents.add(tle);
                    }
                }

                if (aObj.has("sound_effects") && aObj.get("sound_effects").isJsonObject()) {
                    JsonObject sfxObj = aObj.getAsJsonObject("sound_effects");
                    for (Map.Entry<String, JsonElement> sfxEntry : sfxObj.entrySet()) {
                        RawYsmModel.RawSoundEffect sfx = new RawYsmModel.RawSoundEffect();
                        sfx.timestamp = Float.parseFloat(sfxEntry.getKey());
                        sfx.effectName = getStr(sfxEntry.getValue().getAsJsonObject(), "effect", "");
                        anim.soundEffects.add(sfx);
                    }
                }

                raf.animations.put(anim.name, anim);
            }
        }
        return raf;
    }

    private void parseChannelToKeyframes(JsonObject bObj, String channel, List<RawYsmModel.RawKeyframe> targetList) {
        if (!bObj.has(channel)) return;
        JsonElement cElem = bObj.get(channel);

        if (!cElem.isJsonObject()) {
            RawYsmModel.RawKeyframe kf = new RawYsmModel.RawKeyframe();
            kf.timestamp = 0.0f;
            kf.interpolationMode = 0; // linear
            kf.hasPreData = false;
            kf.postData = jsonElementToMolangArray(cElem);
            targetList.add(kf);
            return;
        }

        JsonObject kfsObj = cElem.getAsJsonObject();
        List<Map.Entry<String, JsonElement>> sorted = new ArrayList<>(kfsObj.entrySet());
        sorted.sort(Comparator.comparingDouble(e -> Double.parseDouble(e.getKey())));

        for (Map.Entry<String, JsonElement> entry : sorted) {
            RawYsmModel.RawKeyframe kf = new RawYsmModel.RawKeyframe();
            kf.timestamp = Float.parseFloat(entry.getKey());
            kf.interpolationMode = 0;

            JsonElement valElem = entry.getValue();
            if (valElem.isJsonObject()) {
                JsonObject obj = valElem.getAsJsonObject();
                if (obj.has("lerp_mode")) {
                    String lm = obj.get("lerp_mode").getAsString();
                    if ("catmullrom".equals(lm)) kf.interpolationMode = 2;
                    else if ("step".equals(lm)) kf.interpolationMode = 1;
                } else
                    kf.interpolationMode = 1;

                if (obj.has("pre") && obj.has("post")) {
                    kf.hasPreData = true;
                    kf.preData = jsonElementToMolangArray(obj.get("pre"));
                    kf.postData = jsonElementToMolangArray(obj.get("post"));
                } else {
                    kf.hasPreData = false;
                    kf.postData = jsonElementToMolangArray(obj.has("post") ? obj.get("post") : obj.has("pre") ? obj.get("pre") : obj);
                }
            } else {
                kf.hasPreData = false;
                kf.postData = jsonElementToMolangArray(valElem);
            }
            targetList.add(kf);
        }
    }

    private Object[] jsonElementToMolangArray(JsonElement elem) {
        Object[] arr = new Object[]{0f, 0f, 0f};
        if (elem == null || elem.isJsonNull()) return arr;

        if (elem.isJsonArray()) {
            JsonArray jArr = elem.getAsJsonArray();
            for (int i = 0; i < Math.min(3, jArr.size()); i++) {
                JsonElement e = jArr.get(i);
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) arr[i] = e.getAsFloat();
                else arr[i] = e.getAsString();
            }
        } else {
            Object val;
            if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) val = elem.getAsFloat();
            else val = elem.getAsString();
            arr[0] = val; arr[1] = val; arr[2] = val;
        }
        return arr;
    }

    private void parseAnimationControllers(byte[] data, String fileHash, Map<String, RawYsmModel.RawAnimationController> targetMap) {
        String json = new String(data, StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        if (!root.has("animation_controllers")) return;
        JsonObject acs = root.getAsJsonObject("animation_controllers");

        for (Map.Entry<String, JsonElement> acEntry : acs.entrySet()) {
            if (!acEntry.getValue().isJsonObject()) continue;
            JsonObject acObj = acEntry.getValue().getAsJsonObject();

            RawYsmModel.RawAnimationController ac = new RawYsmModel.RawAnimationController();
            ac.animationName = acEntry.getKey();
            ac.initialState = getStr(acObj, "initial_state", "");

            ac.hash = fileHash;

            if (acObj.has("states") && acObj.get("states").isJsonObject()) {
                JsonObject statesObj = acObj.getAsJsonObject("states");
                for (Map.Entry<String, JsonElement> sEntry : statesObj.entrySet()) {
                    if (!sEntry.getValue().isJsonObject()) continue;
                    JsonObject sObj = sEntry.getValue().getAsJsonObject();

                    RawYsmModel.RawControllerState state = new RawYsmModel.RawControllerState();
                    state.name = sEntry.getKey();

                    if (sObj.has("animations") && sObj.get("animations").isJsonArray()) {
                        for (JsonElement ae : sObj.getAsJsonArray("animations")) {
                            if (ae.isJsonPrimitive()) {
                                state.animations.put(ae.getAsString(), "");
                            } else if (ae.isJsonObject()) {
                                for (Map.Entry<String, JsonElement> objEntry : ae.getAsJsonObject().entrySet()) {
                                    state.animations.put(objEntry.getKey(), objEntry.getValue().getAsString());
                                }
                            }
                        }
                    }

                    if (sObj.has("transitions") && sObj.get("transitions").isJsonArray()) {
                        for (JsonElement te : sObj.getAsJsonArray("transitions")) {
                            if (te.isJsonObject()) {
                                for (Map.Entry<String, JsonElement> objEntry : te.getAsJsonObject().entrySet()) {
                                    state.transitions.put(objEntry.getKey(), objEntry.getValue().getAsString());
                                }
                            }
                        }
                    }

                    if (sObj.has("on_entry") && sObj.get("on_entry").isJsonArray()) {
                        for (JsonElement oe : sObj.getAsJsonArray("on_entry")) state.onEntry.add(oe.getAsString());
                    }

                    if (sObj.has("on_exit") && sObj.get("on_exit").isJsonArray()) {
                        for (JsonElement oe : sObj.getAsJsonArray("on_exit")) state.onExit.add(oe.getAsString());
                    }

                    if (sObj.has("sound_effects") && sObj.get("sound_effects").isJsonArray()) {
                        for (JsonElement se : sObj.getAsJsonArray("sound_effects")) {
                            if (se.isJsonObject()) state.soundEffects.add(getStr(se.getAsJsonObject(), "effect", ""));
                            else if (se.isJsonPrimitive()) state.soundEffects.add(se.getAsString());
                        }
                    }

                    if (sObj.has("blend_transition")) {
                        JsonElement btElem = sObj.get("blend_transition");
                        if (btElem.isJsonPrimitive() && btElem.getAsJsonPrimitive().isNumber()) {
                            state.blendTransitionValue = btElem.getAsFloat();
                        } else if (btElem.isJsonObject()) {
                            for (Map.Entry<String, JsonElement> btEntry : btElem.getAsJsonObject().entrySet()) {
                                state.blendTransitions.put(Float.parseFloat(btEntry.getKey()), btEntry.getValue().getAsFloat());
                            }
                        }
                    }

                    ac.states.add(state);
                }
            }
            targetMap.put(ac.animationName, ac);
        }
    }

    private void parseGlobalResources() {
        try (Stream<Path> stream = Files.walk(rootPath)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String relativePath = rootPath.relativize(path).toString().replace('\\', '/');

                if (relativePath.startsWith("sounds/") || relativePath.endsWith(".ogg")) {
                    String soundName = extractFileName(relativePath);
                    byte[] data = readResource(relativePath);
                    if (data != null) {
                        String hash = sha256Hex(data);
                        model.soundFiles.put(soundName, new RawYsmModel.RawDataFile(hash, data));
                    }
                }
                else if (relativePath.startsWith("lang/") && relativePath.endsWith(".json")) {
                    String locale = relativePath.substring("lang/".length(), relativePath.length() - 5);
                    byte[] data = readResource(relativePath);
                    if (data != null) {
                        try {
                            String hash = sha256Hex(data);
                            String langJsonStr = new String(data, StandardCharsets.UTF_8);
                            JsonObject langJson = JsonParser.parseString(langJsonStr).getAsJsonObject();
                            Map<String, String> langMap = new LinkedHashMap<>();
                            for (Map.Entry<String, JsonElement> langEntry : langJson.entrySet()) {
                                if (langEntry.getValue().isJsonPrimitive()) {
                                    langMap.put(langEntry.getKey(), langEntry.getValue().getAsString());
                                }
                            }
                            model.languageFiles.put(locale, new RawYsmModel.RawLanguageFile(hash, langMap));
                        } catch (Exception ignored) {}
                    }
                }
                else if (relativePath.startsWith("functions/") && relativePath.endsWith(".molang")) {
                    String fnName = extractFileName(relativePath);
                    byte[] data = readResource(relativePath);
                    if (data != null) {
                        String hash = sha256Hex(data);
                        model.functionFiles.put(fnName, new RawYsmModel.RawDataFile(hash, data));
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("[YSM] Warning: Failed to scan global resources. " + e.getMessage());
        }
    }


    private record ImageMeta(int width, int height, int format) {}


    private static ImageMeta parseImageMeta(byte[] data, String path) {
        if (data == null || data.length < 8) {
            throw new RuntimeException("Invalid image data. File too small: " + path);
        }

        int format = detectFormat(data);
        if (format == 0) {
            throw new RuntimeException("Unsupported image format for: " + path);
        }

        if (format == 2 && data.length >= 24) {
            int w = ((data[16] & 0xFF) << 24) | ((data[17] & 0xFF) << 16) | ((data[18] & 0xFF) << 8) | (data[19] & 0xFF);
            int h = ((data[20] & 0xFF) << 24) | ((data[21] & 0xFF) << 16) | ((data[22] & 0xFF) << 8) | (data[23] & 0xFF);
            return new ImageMeta(w, h, format);
        }

        try {
            BufferedImage img = null;
            switch (format) {
                case 1 -> img = ImageIO.read(new ByteArrayInputStream(data)); // BMP
                case 3 -> img = new JpegDecoder().read(data);                 // JPEG
                case 4 -> img = new WebpDecoder().read(data);                 // WEBP
                case 5 -> img = new AvifDecoder().read(data);                 // AVIF
            }
            if (img != null) {
                return new ImageMeta(img.getWidth(), img.getHeight(), format);
            }
            throw new RuntimeException("Failed to decode image dimensions for: " + path);
        } catch (Exception e) {
            throw new RuntimeException("Error processing image: " + path, e);
        }
    }

    /** 1=BMP, 2=PNG, 3=JPEG, 4=WEBP, 5=AVIF, 0=Unknown */
    public static int detectFormat(byte[] data) {
        if (data.length >= 2 && data[0] == 0x42 && data[1] == 0x4D) return 1; // 'BM'
        if (data.length >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) return 2; // PNG
        if (data.length >= 2 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) return 3; // JPEG
        if (data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F' && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') return 4; // WEBP RIFF...WEBP
        if (data.length >= 12 && data[4] == 'f' && data[5] == 't' && data[6] == 'y' && data[7] == 'p') return 5; // AVIF ftyp
        return 0;
    }

    // ==============================================================

    public static int getAnimTypeFromKey(String key) {
        return switch (key) {
            case "main" -> 1;
            case "arm" -> 2;
            case "extra" -> 3;
            case "tac" -> 4;
            case "arrow" -> 5;
            case "carryon" -> 6;
            case "parcool" -> 7;
            case "swem" -> 8;
            case "slashblade" -> 9;
            case "tlm" -> 10;
            case "fp_arm" -> 11;
            case "immersive_melodies" -> 12;
            case "irons_spell_books" -> 13;
            default -> 0;
        };
    }

    public static String getAnimKeyFromType(int type) {
        return switch (type) {
            case 1 -> "main";
            case 2 -> "arm";
            case 3 -> "extra";
            case 4 -> "tac";
            case 5 -> "arrow";
            case 6 -> "carryon";
            case 7 -> "parcool";
            case 8 -> "swem";
            case 9 -> "slashblade";
            case 10 -> "tlm";
            case 11 -> "fp_arm";
            case 12 -> "immersive_melodies";
            case 13 -> "irons_spell_books";
            default -> "unknown";
        };
    }

    private static String getStr(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }

    private static boolean getBool(JsonObject obj, String key, boolean def) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : def;
    }

    private static double getDouble(JsonObject obj, String key, double def) {
        return obj.has(key) ? obj.get(key).getAsDouble() : def;
    }

    private static float[] getFloatArray(JsonObject obj, String key, int size) {
        float[] result = new float[size];
        if (obj.has(key)) {
            JsonArray arr = obj.getAsJsonArray(key);
            for (int i = 0; i < Math.min(arr.size(), size); i++) result[i] = arr.get(i).getAsFloat();
        }
        return result;
    }

    private static String extractFileName(String fullPath) {
        String name = fullPath;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) name = name.substring(lastSlash + 1);
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx >= 0) name = name.substring(0, dotIdx);
        return name;
    }

    private String calculateFinalFolderHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (Map.Entry<String, String> entry : readFilesMd5Map.entrySet()) {
                digest.update(entry.getKey().getBytes(StandardCharsets.UTF_8));
                digest.update(entry.getValue().getBytes(StandardCharsets.UTF_8));
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder(32);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String getFolderHash() {
        return finalFolderHash;
    }
}
