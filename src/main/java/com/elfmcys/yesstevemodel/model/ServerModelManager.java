package com.elfmcys.yesstevemodel.model;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import com.elfmcys.yesstevemodel.client.ExportResult;
import com.elfmcys.yesstevemodel.model.format.*;
import com.elfmcys.yesstevemodel.resource.YSMBinaryDeserializer;
import com.elfmcys.yesstevemodel.resource.YSMBinarySerializer;
import com.elfmcys.yesstevemodel.resource.YSMClientMapper;
import com.elfmcys.yesstevemodel.resource.YSMFolderDeserializer;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import net.minecraft.network.chat.Component;
import rip.ysm.security.YsmCrypt;
import rip.ysm.security.YSMByteBuf;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.AuthModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.config.ServerConfig;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.S2CSyncAuthModelsPacket;
import com.elfmcys.yesstevemodel.network.message.S2CModelSyncPayload;
import com.elfmcys.yesstevemodel.util.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.floats.FloatReferencePair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

public final class ServerModelManager {
    /**
     * 配置相关文件夹
     */
    public static final Path FOLDER = Paths.get("config", YesSteveModel.MOD_ID);

    /**
     * 自定义模型所放置的文件夹
     */
    public static final Path BUILT = FOLDER.resolve("built");
    public static final Path CUSTOM = FOLDER.resolve("custom");
    public static final Path AUTH = FOLDER.resolve("auth");
    public static final Path EXPORT = FOLDER.resolve("export");

    /**
     * 生成缓存文件的文件夹
     */
    public static final Path CACHE = FOLDER.resolve("cache");
    public static final Path CACHE_SERVER_INDEX_FILE = CACHE.resolve("server_index");
    public static final Path CACHE_SERVER = CACHE.resolve("server");
    public static final Path CACHE_CLIENT = CACHE.resolve("client");

    /**
     * 模型名称 -> 模型额外信息缓存
     * 可以方便的通过此缓存，来判断客户端发来的 MD5 在不在服务端
     * 从而将服务器文件发送给玩家
     * 还可以获取其他服务端模型信息
     */
    private static Map<String, ServerModelData> CACHE_NAME_INFO = Maps.newHashMap();

    private static IntOpenHashSet modelHashSet = new IntOpenHashSet();

    /**
     * 放置授权模型名称
     */
    private static Set<String> AUTH_MODELS = Sets.newHashSet();

    private static final Map<UUID, PlayerSyncState> syncStates = new ConcurrentHashMap<>();
    private static final Map<String, ServerPackData> packs = new ConcurrentHashMap<>();
    private static final SecureRandom theRandom = new SecureRandom();
    public static byte[] serverKey;
    private static volatile boolean initialized = false;

    public static class ServerPackData {
        public String folderPath;
        public byte[] iconData;
        public int iconWidth, iconHeight, iconFormat;
        public String name;
        public String description;
        public Map<String, Map<String, String>> lang;
    }

    public static void reloadPacks() throws IOException {
        CACHE_NAME_INFO.clear();
        AUTH_MODELS.clear();

        createFolder(FOLDER);
        createFolder(BUILT);
        createFolder(CUSTOM);
        createFolder(AUTH);
        createFolder(EXPORT);

        createFolder(CACHE);
        createFolder(CACHE_SERVER);
        createFolder(CACHE_CLIENT);

        extractBuiltinModels();

        Files.writeString(BUILT.resolve("notice.txt"),
                "This directory is cleared every time the game starts!\n" +
                        "该目录会在每次游戏启动时清空！",
                StandardCharsets.UTF_8);

        Path blacklistFile = FOLDER.resolve("blacklist.txt");
        if (!Files.exists(blacklistFile)) {
            String content =
                    "# Yes Steve Model 模组 - 内置模型黑名单配置文件\n" +
                            "# Yes Steve Model Mod - Built-in Model Blacklist Configuration File\n" +
                            "\n" +
                            "# 功能说明：\n" +
                            "# 随着内置模型数量的增加，为了满足个性化定制需求，本模组提供了黑名单功能\n" +
                            "# 允许用户选择性地禁用不需要的内置模型，以节省存储空间和加载时间\n" +
                            "#\n" +
                            "# Feature Description:\n" +
                            "# As the number of built-in models increases, this mod provides blacklist functionality\n" +
                            "# to meet customization needs, allowing users to selectively disable unwanted built-in\n" +
                            "# models to save storage space and loading time.\n" +
                            "\n" +
                            "# 使用方法：\n" +
                            "# 1. 在游戏启动前编辑此文件\n" +
                            "# 2. 清空 <游戏目录>/config/yes_steve_model/builtin 文件夹中的已解压模型文件\n" +
                            "# 3. 重新启动游戏，模组将根据黑名单规则跳过指定模型的解压\n" +
                            "#\n" +
                            "# Usage Instructions:\n" +
                            "# 1. Edit this file before starting the game\n" +
                            "# 2. Clear extracted model files in <game_directory>/config/yes_steve_model/builtin folder\n" +
                            "# 3. Restart the game, the mod will skip extracting specified models based on blacklist rules\n" +
                            "\n" +
                            "# 注意事项：\n" +
                            "# - default 模型采用特殊加载机制，无法通过黑名单禁用\n" +
                            "# - 配置文件位置：<游戏目录>/config/yes_steve_model/blacklist.txt\n" +
                            "# - 以 # 开头的行被视为注释，不会被处理\n" +
                            "# - 每行一个规则，使用正则表达式匹配模型的完整解压路径\n" +
                            "#\n" +
                            "# Important Notes:\n" +
                            "# - The default model uses special loading mechanism and cannot be disabled via blacklist\n" +
                            "# - Config file location: <game_directory>/config/yes_steve_model/blacklist.txt\n" +
                            "# - Lines starting with # are comments and will not be processed\n" +
                            "# - One rule per line, using regular expressions to match the complete extraction path of models\n" +
                            "\n" +
                            "# 路径匹配规则：\n" +
                            "# 模组解压时会使用以下格式的路径进行正则表达式匹配：\n" +
                            "#\n" +
                            "# Path Matching Rules:\n" +
                            "# The mod will use the following path formats for regular expression matching during extraction:\n" +
                            "#\n" +
                            "# assets/yes_steve_model/builtin/wine_fox/01_taisho_maid/animations/arrow.animation.json\n" +
                            "# assets/yes_steve_model/builtin/wine_fox/01_taisho_maid/avatar/nico.png\n" +
                            "# assets/yes_steve_model/builtin/misc/2_steve/ysm.json\n" +
                            "\n" +
                            "# 配置示例：\n" +
                            "# 重要提示：下面的示例都以 # 开头，这表示它们目前是注释状态，不会生效\n" +
                            "# 如果你想要启用某个规则，请删除该行开头的 # 号和空格\n" +
                            "#\n" +
                            "# Configuration Examples:\n" +
                            "# Important Notice: All examples below start with #, meaning they are currently commented out and inactive\n" +
                            "# To enable a rule, delete the # symbol and space at the beginning of that line\n" +
                            "\n" +
                            "# 示例1：禁用所有酒狐系列模型 | Example 1: Disable all Wine Fox series models\n" +
                            "# assets/yes_steve_model/builtin/wine_fox/.*\n" +
                            "\n" +
                            "# 示例2：禁用杂项模型文件夹下的所有模型 | Example 2: Disable all models in misc folder\n" +
                            "# assets/yes_steve_model/builtin/misc/.*\n" +
                            "\n" +
                            "# 示例3：禁用特定的大正女仆酒狐模型 | Example 3: Disable specific Taisho Maid Wine Fox model\n" +
                            "# assets/yes_steve_model/builtin/wine_fox/01_taisho_maid/.*\n" +
                            "\n" +
                            "# 示例4：禁用所有内置模型 | Example 4: Disable all built-in models\n" +
                            "# .*";
            Files.writeString(blacklistFile, content, StandardCharsets.UTF_8);
        }
        processBlacklist(blacklistFile);

        Path serverIndex = CACHE_SERVER_INDEX_FILE;
        byte[] serverKeyBytes;

        if (Files.exists(serverIndex)) {
            try {
                String jsonStr = Files.readString(serverIndex, StandardCharsets.UTF_8);
                JsonObject jsonElement = JsonParser.parseString(jsonStr).getAsJsonObject();

                if (jsonElement.get("server_key") != null && jsonElement.get("server_key").getAsJsonPrimitive().isString()) {
                    serverKeyBytes = Base64.getDecoder().decode(jsonElement.get("server_key").getAsString());
                    if (serverKeyBytes.length != 56) {
                        throw new IllegalStateException("ServerKey length must be 56 bytes, but got " + serverKeyBytes.length);
                    }
                } else {
                    serverKeyBytes = new byte[56];
                    new SecureRandom().nextBytes(serverKeyBytes);
                    jsonElement.addProperty("server_key", Base64.getEncoder().encodeToString(serverKeyBytes));
                    Files.writeString(serverIndex, jsonElement.toString(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                serverKeyBytes = new byte[56];
                new SecureRandom().nextBytes(serverKeyBytes);
                JsonObject jsonElement = new JsonObject();
                jsonElement.addProperty("server_key", Base64.getEncoder().encodeToString(serverKeyBytes));
                Files.writeString(serverIndex, jsonElement.toString(), StandardCharsets.UTF_8);
            }
        } else {
            serverKeyBytes = new byte[56];
            new SecureRandom().nextBytes(serverKeyBytes);
            JsonObject jsonElement = new JsonObject();
            jsonElement.addProperty("server_key", Base64.getEncoder().encodeToString(serverKeyBytes));
            Files.writeString(serverIndex, jsonElement.toString(), StandardCharsets.UTF_8);
        }

        serverKey = serverKeyBytes;
        nativeLoadModels(null);
    }

    private static void extractBuiltinModels() {
        if (Files.isDirectory(BUILT)) {
            try (var s = Files.walk(BUILT)) {
                s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                    if (!p.equals(BUILT)) try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            } catch (IOException ignored) {}
        }
        try {
            IModFile modFile = ModList.get().getModFileById(YesSteveModel.MOD_ID).getFile();
            Path modRoot = modFile.getSecureJar().getRootPath();
            Path assetsBuiltin = modRoot.resolve("assets/yes_steve_model/builtin");

            if (!Files.isDirectory(assetsBuiltin)) return;

            try (Stream<Path> walker = Files.walk(assetsBuiltin)) {
                walker.forEach(src -> {
                    try {
                        Path relative = assetsBuiltin.relativize(src);
                        Path dest = ServerModelManager.BUILT.resolve(relative.toString());
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.createDirectories(dest.getParent());
                            try (InputStream in = Files.newInputStream(src)) {
                                Files.copy(in, dest);
                            }
                        }
                    } catch (IOException e) {
                        YesSteveModel.LOGGER.warn("Failed to extract builtin: " + src.getFileName(), e);
                    }
                });
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to extract builtin models", e);
        }
    }

    private static void processBlacklist(Path blacklistFile) {
        List<Pattern> rules = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(blacklistFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    rules.add(Pattern.compile(line));
                } catch (PatternSyntaxException ignored) {
                }
            }
        } catch (IOException e) {
            return;
        }

        if (rules.isEmpty() || !Files.isDirectory(BUILT)) return;

        try (DirectoryStream<Path> groups = Files.newDirectoryStream(BUILT)) {
            for (Path group : groups) {
                if (!Files.isDirectory(group)) continue;
                try (DirectoryStream<Path> models = Files.newDirectoryStream(group)) {
                    for (Path model : models) {
                        if (!Files.isDirectory(model)) continue;
                        String matchPath = "assets/yes_steve_model/builtin/" + group.getFileName() + "/" + model.getFileName() + "/";
                        for (Pattern rule : rules) {
                            if (rule.matcher(matchPath).find()) {
                                deleteRecursively(model);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            Files.deleteIfExists(dir);
            return;
        }
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
            for (Path entry : entries) {
                deleteRecursively(entry);
            }
        }
        Files.deleteIfExists(dir);
    }

    private static void createFolder(Path path) {
        File folder = path.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class PlayerSyncState {
        byte[] clientKey = new byte[56];
        byte[] key1;
        byte[] clientNextKey;
        int step = 0;
        List<ServerModelData> allowedModels = new ArrayList<>();

        // TODO: 未来可基于UUID持久化，这里目前每次加入生成固定clientKey
        PlayerSyncState() {new Random(114514).nextBytes(clientKey);}
    }

    public static void nativeSendModelData(UUID uuid, @Nullable ByteBuffer data) {
        if (data != null && !data.hasRemaining() && data.position() > 0) {
            data.flip();
        }

        if (data == null || data.remaining() == 0) {
            syncStates.remove(uuid);
            return;
        }

        PlayerSyncState state = syncStates.get(uuid);
        if (state == null) return;

        try {
            byte[] packetBytes = new byte[data.remaining()];
            data.get(packetBytes);
            System.out.println("Server Handle packet, step=" + state.step + ", length=" + packetBytes.length);

            if (state.step == 1) {
                // 等待Pong
                byte[] decrypted = YsmCrypt.decrypt(packetBytes, state.key1);
                if (decrypted == null || decrypted.length < 56) return;

                // 客戶端生成的密鑰
                state.clientNextKey = Arrays.copyOfRange(decrypted, decrypted.length - 56, decrypted.length);
                byte[] payload = Arrays.copyOfRange(decrypted, 0, decrypted.length - 56);

                try (YSMByteBuf buf = new YSMByteBuf(Unpooled.wrappedBuffer(payload))) {
                    buf.skipGarbageHeader();
                    if (buf.getRawBuf().readByte() != 0x02) return;
                }

                // 發送可用模型
                state.step = 2;
                sendPacket03(uuid, state);
            } else if (state.step == 2) {
                byte[] decrypted = YsmCrypt.decrypt(packetBytes, state.key1);
                if (decrypted == null) return;

                try (YSMByteBuf buf = new YSMByteBuf(Unpooled.wrappedBuffer(decrypted))) {
                    buf.skipGarbageHeader();
                    if (buf.getRawBuf().readByte() != 0x04) return;

                    int numRequests = buf.readVarInt();
                    List<long[]> requestedHashes = new ArrayList<>();
                    for (int i = 0; i < numRequests; i++) {
                        requestedHashes.add(new long[]{buf.readVarLong(), buf.readVarLong()});
                    }
                    state.step = 3;
                    sendPacket05(uuid, state, requestedHashes);
                }
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("[YSM] Server sync error for " + uuid, e);
        }
    }

    public static boolean nativeLoadModels(Object callback) {
        try {
            Map<String, ServerModelData> loadedModels = new LinkedHashMap<>();
            Set<String> authIds = new HashSet<>();
            Set<String> validCacheFiles = new HashSet<>();

            packs.clear();
            scanDirectoryPacks(BUILT);
            scanDirectoryPacks(CUSTOM);
            scanDirectoryPacks(AUTH);

            scanDirectoryModels(BUILT, CACHE_SERVER, loadedModels, authIds, validCacheFiles, false);
            scanDirectoryModels(CUSTOM, CACHE_SERVER, loadedModels, authIds, validCacheFiles, false);
            scanDirectoryModels(AUTH, CACHE_SERVER, loadedModels, authIds, validCacheFiles, true);
            try (Stream<Path> stream = Files.list(CACHE_SERVER)) {
                stream.forEach(file -> {
                    if (!validCacheFiles.contains(file.getFileName().toString())) {
                        try { Files.deleteIfExists(file); } catch (Exception ignored) {}
                    }
                });
            } catch (Exception ignored) {}

            ModelLoadResult result = new ModelLoadResult(true, null, loadedModels, authIds.toArray(new String[0]));
            AUTH_MODELS = authIds;

            onModelLoadComplete(result, callback);
            return true;
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("[YSM] Model loading failed", e);
            return false;
        }
    }

    private static void scanDirectoryModels(Path baseDir, Path cacheDir, Map<String, ServerModelData> loaded, Set<String> authIds, Set<String> validCaches, boolean isAuth) {
        if (baseDir == null || !Files.isDirectory(baseDir)) return;
        try (Stream<Path> stream = Files.walk(baseDir)) {
            stream.forEach(path -> {
                String fileName = path.getFileName().toString();
                try {
                    if (fileName.equals("ysm.json")) {
                        Path modelDir = path.getParent();
                        // 提取相對路徑作為ID
                        String modelId = baseDir.relativize(modelDir).toString().replace('\\', '/');

                        try (YSMFolderDeserializer deserializer = new YSMFolderDeserializer(modelDir)) {
                            RawYsmModel rawModel = deserializer.deserialize();
                            ServerModelData data = processAndCacheModel(modelId, rawModel, cacheDir, isAuth, validCaches);
                            if (data != null) {
                                loaded.put(modelId, data);
                                if (isAuth) authIds.add(modelId);
                            }
                        }
                    }
                    else if (fileName.endsWith(".ysm")) {
                        String relativePath = baseDir.relativize(path).toString().replace('\\', '/');
                        String modelId = /*relativePath.substring(0, relativePath.length() - 4)*/relativePath;
                        byte[] raw = Files.readAllBytes(path);
                        byte[] decrypted = YsmCrypt.decryptYsmFile(raw);
                        try (YSMBinaryDeserializer deserializer = new YSMBinaryDeserializer(decrypted)) {
                            RawYsmModel rawModel = deserializer.deserializeKeepOpen();
                            deserializer.parseYSMFooter(rawModel); //只用于gui展示数据
                            ServerModelData data = processAndCacheModel(modelId, rawModel, cacheDir, isAuth, validCaches);
                            if (data != null) {
                                loaded.put(modelId, data);
                                if (isAuth) authIds.add(modelId);
                            }
                        }
                    }
                } catch (Exception e) {
                    YesSteveModel.LOGGER.error("Failed to load model at: " + path, e);
                }
            });
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to walk directory: " + baseDir, e);
        }
    }

    private static void scanDirectoryPacks(Path baseDir) {
        if (baseDir == null || !Files.isDirectory(baseDir)) return;
        try (var stream = Files.walk(baseDir, 1)) {
            stream.filter(Files::isDirectory).forEach(path -> {
                if (path.equals(baseDir)) return;
                Path packJson = path.resolve("ysm-pack.json");
                if (Files.exists(packJson)) {
                    try {
                        ServerPackData packData = new ServerPackData();
                        packData.folderPath = baseDir.toFile().toURI().relativize(path.toFile().toURI()).getPath();

                        String jsonStr = Files.readString(packJson, StandardCharsets.UTF_8);
                        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                        if (json.has("name")) packData.name = json.get("name").getAsString();
                        if (json.has("description")) packData.description = json.get("description").getAsString();

                        if (json.has("lang") && json.get("lang").isJsonObject()) {
                            packData.lang = new HashMap<>();
                            JsonObject langObj = json.getAsJsonObject("lang");
                            for (Map.Entry<String, JsonElement> entry : langObj.entrySet()) {
                                if (entry.getValue().isJsonObject()) {
                                    Map<String, String> translations = new HashMap<>();
                                    for (Map.Entry<String, JsonElement> transEntry : entry.getValue().getAsJsonObject().entrySet()) {
                                        translations.put(transEntry.getKey(), transEntry.getValue().getAsString());
                                    }
                                    packData.lang.put(entry.getKey(), translations);
                                }
                            }
                        }

                        Path packPng = path.resolve("ysm-pack.png");
                        if (Files.exists(packPng)) {
                            byte[] data = Files.readAllBytes(packPng);
                            int[] dims = getPngDimensions(data);
                            packData.iconData = data;
                            packData.iconWidth = dims[0];
                            packData.iconHeight = dims[1];
                            packData.iconFormat = 2; // 2=PNG
                        }
                        packs.put(packData.folderPath, packData);
                    } catch (Exception e) {
                        YesSteveModel.LOGGER.error("Failed to load pack metadata: " + packJson, e);
                    }
                }
            });
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to walk directory for packs: " + baseDir, e);
        }
    }

    private static int[] getPngDimensions(byte[] data) {
        if (data == null || data.length < 24) return new int[]{0, 0};
        if ((data[0] & 0xFF) != 0x89 || data[1] != 0x50 || data[2] != 0x4E || data[3] != 0x47) return new int[]{0, 0};
        int width = ((data[16] & 0xFF) << 24) | ((data[17] & 0xFF) << 16) | ((data[18] & 0xFF) << 8) | (data[19] & 0xFF);
        int height = ((data[20] & 0xFF) << 24) | ((data[21] & 0xFF) << 16) | ((data[22] & 0xFF) << 8) | (data[23] & 0xFF);
        return new int[]{width, height};
    }

    private static ServerModelData processAndCacheModel(String modelId, RawYsmModel model, Path serverCacheDir, boolean isAuth, Set<String> validCacheFiles) {
        String sha256 = model.properties.sha256;
        if (sha256 == null || sha256.isEmpty()) return null;

        try {
            long[] hashes = YsmCrypt.calculateModelHashes(sha256, serverKey);
            String cacheFileName = String.format("%016x%016x", hashes[0], hashes[1]);
            Path cacheFile = serverCacheDir.resolve(cacheFileName);
            if (!serverCacheDir.toFile().isDirectory()) {
                Files.createDirectories(serverCacheDir);
            }
            boolean needsUpdate = true;
            if (Files.exists(cacheFile)) {
                byte[] existingData = Files.readAllBytes(cacheFile);
                if (YsmCrypt.verifyServerCache(existingData, hashes[0], hashes[1])) {
                    needsUpdate = false;
                }
            }
            if (needsUpdate) {
                try (YSMByteBuf serialized = YSMBinarySerializer.serialize(model, 32,true)) {
                    byte[] rawBytes = new byte[serialized.getRawBuf().readableBytes()];
                    serialized.getRawBuf().readBytes(rawBytes);

                    byte[] encryptedCache = YsmCrypt.encryptServerCache(rawBytes, serverKey, hashes[0], hashes[1]);
                    Files.write(cacheFile, encryptedCache);
                }
            }
            validCacheFiles.add(cacheFileName);

            boolean isCustomSkinModel = "misc/2_steve".equals(modelId) || "misc/1_alex".equals(modelId); // 对没错就是写死的

            return mapToDataClass(modelId, model, isAuth, isCustomSkinModel);
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to process and cache model: " + modelId, e);
            return null;
        }
    }


    private static ServerModelData mapToDataClass(String modelId, RawYsmModel raw, boolean isAuth, boolean isCustomSkinModel) {
        ServerModelInfo serverModelInfo = YSMClientMapper.buildModelInfo(raw);
        // Animations
        Map<String, String[]> animMap = new HashMap<>();
        for (Map.Entry<String, RawYsmModel.RawAnimationFile> e : raw.mainEntity.animationFiles.entrySet()) {
            animMap.put(e.getKey(), e.getValue().animations.keySet().toArray(new String[0]));
        }
        String[] texArr = raw.mainEntity.textures.keySet().toArray(new String[0]);
        ServerAnimationInfo animInfo = new ServerAnimationInfo(animMap, texArr);

        // Sub Entities
        Object[] projectiles = raw.projectiles.values().stream().map(v -> v.matchIds != null ? v.matchIds : new String[]{v.identifier}).toArray();
        Object[] vehicles = raw.vehicles.values().stream().map(v -> v.matchIds != null ? v.matchIds : new String[]{v.identifier}).toArray();
        return new ServerModelData(modelId, animInfo, projectiles, vehicles, serverModelInfo, isCustomSkinModel, isAuth);
    }

    public static void nativeSyncModels(UUID[] uuids, String[] playerNames, String[] modelIds, Object callback) {
        YSMThreadPool.submit(() -> {
            try {
                MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
                if (currentServer == null) return;

                for (UUID uuid : uuids) {
                    PlayerSyncState state = syncStates.computeIfAbsent(uuid, k -> new PlayerSyncState());
                    state.allowedModels.clear();
                    state.allowedModels.addAll(CACHE_NAME_INFO.values());
                    state.step = 1;

                    // HandshakePing
//                    byte[] garbage = new byte[16 + SECURE_RANDOM_S.nextInt(48)];
//                    SECURE_RANDOM_S.nextBytes(garbage);
//                    byte[] payload = new byte[2 + garbage.length + 1];
//                    payload[0] = (byte)(garbage.length & 0xFF);
//                    payload[1] = (byte)((garbage.length >> 8) & 0xFF);
//                    System.arraycopy(garbage, 0, payload, 2, garbage.length);
//                    payload[2 + garbage.length] = 0x01;
//
//                    var result = YsmCrypt.encrypt(payload, K0_SERVER, true);
//                    state.key1 = result.nextKey();
//
//                    sendModelData(uuid, ByteBuffer.wrap(result.data()), new PendingTransfer());
                    int garbageLen = 16 + theRandom.nextInt(48);
                    byte[] garbage = new byte[garbageLen];
                    theRandom.nextBytes(garbage);

                    try (YSMByteBuf outBuf = new YSMByteBuf(Unpooled.buffer())) {
                        outBuf.writeGarbageHeader(garbageLen, garbage);
                        outBuf.writeByte((byte) 0x01);
                        YsmCrypt.EncryptedPacket result = YsmCrypt.encrypt(outBuf.toArray(), YsmCrypt.publicKey, true);
                        state.key1 = result.nextKey();

                        sendModelData(uuid, ByteBuffer.wrap(result.data()), new PendingTransfer());
                    }
                }
//                if (callback != null) onAuthDataReceived(null, callback);
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("[YSM] Sync initiation failed", e);
            }
        });
    }

    private static void sendPacket03(UUID uuid, PlayerSyncState state) {
        int garbageLen = 16 + theRandom.nextInt(48);
        byte[] garbage = new byte[garbageLen];
        theRandom.nextBytes(garbage);

        try (YSMByteBuf outBuf = new YSMByteBuf(Unpooled.buffer())) {
            outBuf.writeGarbageHeader(garbageLen, garbage);

            outBuf.writeVarInt(3); // Type
            outBuf.writeVarLong(0L); // 這個決定了cache資料夾的名稱

            outBuf.getRawBuf().writeBytes(serverKey);
            outBuf.getRawBuf().writeBytes(state.clientKey);

            outBuf.writeVarInt(state.allowedModels.size());
            for (ServerModelData model : state.allowedModels) {
                String sha256 = model.getLoadedModelData().getModelHash();
                long[] hashes = YsmCrypt.calculateModelHashes(sha256, serverKey);
                outBuf.writeVarLong(hashes[0]);
                outBuf.writeVarLong(hashes[1]);
                outBuf.writeString(model.getModelId());
                outBuf.writeVarInt(model.isCustomSkinModel() ? 1 : 0);
                outBuf.writeVarInt(model.isAuth() ? 1 : 0);
                outBuf.writeVarInt(32); // format
            }

            outBuf.writeVarInt(packs.size());
            for (ServerPackData pack : packs.values()) {
                outBuf.writeString(pack.folderPath);

                // 寫入圖標資訊
                if (pack.iconData != null) {
                    outBuf.writeVarInt(1);
                    outBuf.writeByteArray(pack.iconData);
                    outBuf.writeVarInt(pack.iconWidth);
                    outBuf.writeVarInt(pack.iconHeight);
                    outBuf.writeVarInt(pack.iconFormat);
                    outBuf.writeVarInt(1); // unkImageData
                } else {
                    outBuf.writeVarInt(0);
                }

                // 寫入基礎資訊
                if (pack.name != null || pack.description != null) {
                    outBuf.writeVarInt(1);
                    outBuf.writeString(pack.name != null ? pack.name : "");
                    outBuf.writeString(pack.description != null ? pack.description : "");
                } else {
                    outBuf.writeVarInt(0);
                }

                // 寫入語言本地化
                if (pack.lang != null && !pack.lang.isEmpty()) {
                    outBuf.writeVarInt(pack.lang.size());
                    for (Map.Entry<String, Map<String, String>> langEntry : pack.lang.entrySet()) {
                        outBuf.writeString(langEntry.getKey());
                        outBuf.writeVarInt(langEntry.getValue().size());
                        for (Map.Entry<String, String> kv : langEntry.getValue().entrySet()) {
                            outBuf.writeString(kv.getKey());
                            outBuf.writeString(kv.getValue());
                        }
                    }
                } else {
                    outBuf.writeVarInt(0);
                }
            }

            outBuf.writeVarInt(0);  // \0

            YsmCrypt.EncryptedPacket result = YsmCrypt.encrypt(outBuf.toArray(), state.clientNextKey, false);
            sendModelData(uuid, ByteBuffer.wrap(result.data()), new PendingTransfer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendPacket05(UUID uuid, PlayerSyncState state, List<long[]> requestedHashes) {
        YSMThreadPool.submit(() -> {
            try {
                PendingTransfer transfer = new PendingTransfer();

                for (long[] hashes : requestedHashes) {
                    long hash1 = hashes[0];
                    long hash2 = hashes[1];
                    String fileName = String.format("%016x%016x", hash1, hash2);
                    Path file = ServerModelManager.CACHE_SERVER.resolve(fileName);

                    if (!Files.exists(file)) continue;

                    byte[] fileData = Files.readAllBytes(file);
                    int totalSize = fileData.length;
                    int chunkSize = 32000;
                    int offset = 0;

                    while (offset < totalSize) {
                        int length = Math.min(chunkSize, totalSize - offset);

                        int garbageLen = 16 + theRandom.nextInt(48);
                        byte[] garbage = new byte[garbageLen];
                        theRandom.nextBytes(garbage);

                        try (YSMByteBuf outBuf = new YSMByteBuf(Unpooled.buffer())) {
                            outBuf.writeGarbageHeader(garbageLen, garbage);
                            outBuf.writeVarInt(5); // Type
                            outBuf.writeVarLong(hash1);
                            outBuf.writeVarLong(hash2);
                            outBuf.writeVarInt(totalSize);
                            outBuf.writeVarInt(offset);
                            outBuf.writeVarInt(length);
                            outBuf.getRawBuf().writeBytes(fileData, offset, length);
                            YsmCrypt.EncryptedPacket result = YsmCrypt.encrypt(outBuf.toArray(), state.key1, false);

                            // Stream chunks
                            boolean success = sendModelData(uuid, ByteBuffer.wrap(result.data()), transfer);
                            if (success) {
                                offset += length;
                            } else {
                                try { Thread.sleep(5); } catch (InterruptedException e) {}
                            }
                        }
                    }
                }
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Failed to send model chunks to " + uuid, e);
            }
        });
    }

    public static void nativeExportModel(String modelID, @Nullable String extra, @Nullable Consumer<ExportResult> callback) {
        YSMThreadPool.submit(() -> {
            try {
                ServerModelData modelData = CACHE_NAME_INFO.get(modelID);
                if (modelData == null) {
                    if (callback != null) {
                        callback.accept(new ExportResult(false, (Component) YSMNativeHelper.createTranslatableComponent("commands.yes_steve_model.export.failure",new Object[]{": " + modelID + "\n Model not found"}), "", "", 0));
                    }
                    return;
                }

                String sha256 = modelData.getLoadedModelData().getModelHash();
                long[] hashes = YsmCrypt.calculateModelHashes(sha256, serverKey);
                String cacheFileName = String.format("%016x%016x", hashes[0], hashes[1]);
                Path cacheFile = CACHE_SERVER.resolve(cacheFileName);

                if (!Files.exists(cacheFile)) {
                    if (callback != null) {
                        callback.accept(new ExportResult(false, Component.literal("Cache file missing for: " + modelID), "", "", 0));
                    }
                    return;
                }

                byte[] cacheData = Files.readAllBytes(cacheFile);
                byte[] clearText = YsmCrypt.read(cacheData, serverKey);

                int coreDataLength;
                try (YSMBinaryDeserializer deserializer = new YSMBinaryDeserializer(clearText, 32)) {
                    deserializer.deserializeKeepOpen();
                    coreDataLength = deserializer.getReader().getOffset();
                }

                try (YSMByteBuf outBuf = new YSMByteBuf(Unpooled.buffer())) {
                    outBuf.writeDword(32);
                    outBuf.getRawBuf().writeBytes(clearText, 0, coreDataLength);
                    outBuf.writeVarInt(32); // version
                    outBuf.writeVarInt(1);
                    byte[] randBytes = new byte[8];
                    theRandom.nextBytes(randBytes);
                    StringBuilder sb = new StringBuilder(16);
                    for (byte b : randBytes) {
                        sb.append(String.format("%02x", b));
                    }
                    outBuf.writeString(sb.toString());
                    outBuf.writeVarLong(java.time.Instant.now().getEpochSecond());
                    outBuf.writeString(extra != null ? extra : "");
                    outBuf.writeVarInt(0);
                    byte[] rawBytes = new byte[outBuf.getRawBuf().readableBytes()];
                    outBuf.getRawBuf().readBytes(rawBytes);
                    byte[] finalEncrypted = YsmCrypt.encryptYsmFile(rawBytes);
                    Path exportPath = EXPORT.resolve(modelID + ".ysm");
                    Files.createDirectories(exportPath.getParent());
                    Files.write(exportPath, finalEncrypted);
                    if (callback != null) {
                        String displayPath = Paths.get("export", modelID + ".ysm").toString();
                        callback.accept(new ExportResult(true, null, displayPath, "", 0));
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.accept(new ExportResult(false, Component.literal("Export failed: " + e.getMessage()), "", "", 0));
                }
            }
        });
    }

    public static Optional<ServerModelData> getModelDefinition(String str) {
        return Optional.ofNullable(CACHE_NAME_INFO.get(str));
    }

    public static Map<String, ServerModelData> getServerModelInfo() {
        return CACHE_NAME_INFO;
    }

    public static Set<String> getAuthModels() {
        return AUTH_MODELS;
    }

    public static void requestPlayerAuth(ServerPlayer serverPlayer, @Nullable Consumer<UUIDComponentData> consumer) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        currentServer.execute(() -> {
            List<ServerPlayer> players = currentServer.getPlayerList().getPlayers();
            ArrayList<FloatReferencePair<ServerPlayer>> arrayList = new ArrayList<>();
            for (ServerPlayer serverPlayer2 : players) {
                if (serverPlayer2.level().dimensionType() == serverPlayer.level().dimensionType()) {
                    arrayList.add(FloatReferencePair.of(serverPlayer2.distanceTo(serverPlayer), serverPlayer2));
                }
            }
            arrayList.sort((a, b) -> Float.compare(a.firstFloat(), b.firstFloat()));
            nativeSyncModels(new UUID[]{serverPlayer.getUUID()}, new String[]{serverPlayer.getGameProfile().getName()}, collectPlayerModelIds(arrayList.stream().map(it.unimi.dsi.fastutil.Pair::second).toList()), consumer);
        });
    }

    public static boolean loadModels(@Nullable Consumer<ModelLoadResult> consumer, @Nullable Consumer<UUIDComponentData> consumer2) {
        Consumer<ModelLoadResult> action = modelLoadResult -> {
            if (consumer != null) {
                consumer.accept(modelLoadResult);
            }
            MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
            if (currentServer == null) {
                return;
            }
            currentServer.execute(() -> {
                List<ServerPlayer> players = currentServer.getPlayerList().getPlayers();
                for (ServerPlayer value : players) {
                    validatePlayerModel(value);
                }
                nativeSyncModels(players.stream().filter(NetworkHandler::isPlayerConnected).map((player) -> player.getUUID()).toArray(i -> new UUID[i]), players.stream().filter(NetworkHandler::isPlayerConnected).map(serverPlayer -> serverPlayer.getGameProfile().getName()).toArray(i2 -> new String[i2]), collectPlayerModelIds(players), consumer2);
            });
        };
        return nativeLoadModels(action);
    }

    private static String[] collectPlayerModelIds(Collection<ServerPlayer> collection) {
        return collection.stream().filter(NetworkHandler::isPlayerConnected).map(serverPlayer -> serverPlayer.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).map(ModelInfoCapability::getModelId)).filter(Optional::isPresent).map(Optional::get).distinct().toArray(String[]::new);
    }

    private static void onModelLoadComplete(ModelLoadResult modelLoadResult, @Nullable Object obj) {
        Consumer<ModelLoadResult> consumer = (Consumer<ModelLoadResult>) obj;
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        initialized = true;
        if (currentServer != null) {
            currentServer.execute(() -> {
                if (modelLoadResult.isSuccess()) {
                    IntOpenHashSet intOpenHashSet = new IntOpenHashSet(modelLoadResult.getModelDefinitions().size());
                    for (ServerModelData data : modelLoadResult.getModelDefinitions().values()) {
                        intOpenHashSet.add(data.getLoadedModelData().getHashId());
                    }
                    CACHE_NAME_INFO = modelLoadResult.getModelDefinitions();
                    modelHashSet = intOpenHashSet;
                    AUTH_MODELS = modelLoadResult.getAuthModelIds();
                }
                if (consumer != null) {
                    YSMThreadPool.submit(() -> consumer.accept(modelLoadResult));
                }
            });
            return;
        }
        if (modelLoadResult.isSuccess()) {
            CACHE_NAME_INFO = modelLoadResult.getModelDefinitions();
            AUTH_MODELS = modelLoadResult.getAuthModelIds();
        }
        if (consumer != null) {
            consumer.accept(modelLoadResult);
        }
    }

    public static void syncModelToPlayer(UUID uuid) {
        nativeSendModelData(uuid, null);
    }

    private static Connection getPlayerConnection(UUID uuid) {
        ServerPlayer player;
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null || (player = currentServer.getPlayerList().getPlayer(uuid)) == null) {
            return null;
        }
        ServerGamePacketListenerImpl serverGamePacketListenerImpl = player.connection;
        if (!serverGamePacketListenerImpl.isAcceptingMessages() || !serverGamePacketListenerImpl.getClass().equals(ServerGamePacketListenerImpl.class)) {
            return null;
        }
        return serverGamePacketListenerImpl.connection;
    }

    private static boolean sendModelData(UUID uuid, ByteBuffer byteBuffer, PendingTransfer pendingTransfer) {
        Connection connection = getPlayerConnection(uuid);
        if (connection != null) {
            return sendPacketReliably(connection, NetworkHandler.CHANNEL.toVanillaPacket(new S2CModelSyncPayload(byteBuffer), NetworkDirection.PLAY_TO_CLIENT), pendingTransfer);
        }
        return false;
    }

    private static Object createModelPacket(ByteBuffer byteBuffer) {
        return NetworkHandler.CHANNEL.toVanillaPacket(new S2CModelSyncPayload(byteBuffer), NetworkDirection.PLAY_TO_CLIENT);
    }

    private static boolean sendPacketToPlayer(UUID uuid, Object obj, PendingTransfer pendingTransfer) {
        Connection connection = getPlayerConnection(uuid);
        if (connection != null) {
            return sendPacketReliably(connection, obj, pendingTransfer);
        }
        return false;
    }

    private static boolean sendPacketReliably(Connection connection, Object obj, PendingTransfer pendingTransfer) {
        if (!pendingTransfer.hasStarted) {
            pendingTransfer.hasStarted = true;
            pendingTransfer.pendingBytes = connection.channel().unsafe().outboundBuffer().totalPendingWriteBytes() + 65536;
        }

        final AtomicInteger atomicInteger = new AtomicInteger(0);
        while (connection.isConnected()) {
            if (connection.channel().unsafe().outboundBuffer().size() > pendingTransfer.pendingBytes) {
                if (!YSMThreadPool.awaitTermination(10)) {
                    return false;
                }
            } else {
                try {
                    connection.send((Packet<?>) obj, new PacketSendListener() {
                        public void onSuccess() {
                            atomicInteger.set(1);
                            PacketSendListener.super.onSuccess();
                        }

                        @Nullable
                        public Packet<?> onFailure() {
                            atomicInteger.set(-1);
                            return null;
                        }
                    });
                    while (atomicInteger.get() == 0) {
                        if (!YSMThreadPool.awaitTermination(5)) {
                            return false;
                        }
                    }
                    if (atomicInteger.get() == 1) {
                        return true;
                    }
                    if (!YSMThreadPool.awaitTermination(100)) {
                        return false;
                    }
                    atomicInteger.set(0);
                } catch (Throwable th) {
                    th.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public static Pair<String, String> getDefaultModelConfig() {
        String defaultModelId = ServerConfig.DEFAULT_MODEL_ID.get();
        String defaultTexture = ServerConfig.DEFAULT_MODEL_TEXTURE.get();
        if (defaultTexture.toLowerCase().endsWith(".png") && defaultTexture.length() > 4) {
            defaultTexture = defaultTexture.substring(0, defaultTexture.length() - 4);
        }
        if (!initialized) {
            return Pair.of(defaultModelId, defaultTexture);
        }
        ServerModelData modelData = CACHE_NAME_INFO.get(defaultModelId);
        if (modelData == null) {
            return Pair.of("default", "default");
        }
        if (!modelData.getModelInfo().getTextures().contains(defaultTexture)) {
            if (modelData.getModelInfo().getTextures().contains(modelData.getLoadedModelData().getModelProperties().getDefaultTexture())) {
                defaultTexture = modelData.getLoadedModelData().getModelProperties().getDefaultTexture();
            } else {
                defaultTexture = modelData.getModelInfo().getTextures().get(0);
            }
        }
        return Pair.of(defaultModelId, defaultTexture);
    }

    private static void onAuthDataReceived(UUIDComponentData uuidComponentData, @Nullable Object obj) {
        Consumer consumer = (Consumer) obj;
        if (consumer != null) {
            consumer.accept(uuidComponentData);
        }
    }

    public static void validatePlayerModel(ServerPlayer serverPlayer) {
        if (!CACHE_NAME_INFO.isEmpty()) {
            serverPlayer.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelInfoCap -> {
                serverPlayer.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(authModelsCap -> {
                    if (authModelsCap.getAuthModels().removeIf(str -> !CACHE_NAME_INFO.containsKey(str))) {
                        NetworkHandler.sendToClientPlayer(new S2CSyncAuthModelsPacket(authModelsCap.getAuthModels()), serverPlayer);
                    }
                    String modelId = modelInfoCap.getModelId();
                    if (!getServerModelInfo().containsKey(modelId) || ((AUTH_MODELS.contains(modelId) && !authModelsCap.containsModel(modelInfoCap.getModelId())) || !CACHE_NAME_INFO.get(modelId).getModelInfo().getTextures().contains(modelInfoCap.getSelectTexture()))) {
                        modelInfoCap.resetToDefault();
                    }
                    modelInfoCap.retainAnimationKeys(modelHashSet);
                });
            });
        }
    }

    private static class PendingTransfer {
        public long pendingBytes;

        public boolean hasStarted = false;

        private PendingTransfer() {
        }
    }
}
