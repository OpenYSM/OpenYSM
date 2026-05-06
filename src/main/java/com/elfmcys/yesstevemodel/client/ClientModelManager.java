package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import com.elfmcys.yesstevemodel.client.model.ModelAssemblyFactory;
import com.elfmcys.yesstevemodel.client.gui.IGuiWidget;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SModelSyncPayload;
import com.elfmcys.yesstevemodel.resource.*;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import com.elfmcys.yesstevemodel.resource.models.ModelPackData;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import rip.ysm.security.YSMClientCache;
import rip.ysm.security.YsmCrypt;
import com.elfmcys.yesstevemodel.util.*;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.jetbrains.annotations.Nullable;

import rip.ysm.security.YSMByteBuf;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ClientModelManager {
    private static int syncStep = 1;
    private static byte[] key1;
    private static byte[] lastKey;
    private static byte[] serverKey;
    private static byte[] clientKey;
    private static String currentCacheFolderName;
    private static int pendingModelsCount;

    private static final Map<UUID, ServerModelContext> serverModels = new ConcurrentHashMap<>();

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();
    private static volatile ModelAssembly localModelContext;
    private static volatile Runnable pendingModelCallback;
    private static IResourceLocatable defaultTexture;
    private static volatile Connection serverConnection;

    private static volatile Map<String, ModelAssembly> modelAssemblyMap = Object2ReferenceMaps.emptyMap();
    private static volatile Map<String, ModelPackData> modelPackMap = new Object2ReferenceOpenHashMap<>();

    private static final ConcurrentLinkedQueue<Pair<ModelAssembly, String>> pendingModelQueue = new ConcurrentLinkedQueue<>();
    private static final WeakHashMap<IGuiWidget, Object> guiWidgets = new WeakHashMap<>();
    private static final SyncStatus syncState = new SyncStatus();

    public enum SyncState {
        WAITING, LOADING, IDLE, PREPARING, SYNCING
    }

    public static class ServerModelContext {
        public final UUID uuid;
        public final long hash1;
        public final long hash2;
        public final String modelId;
        public final boolean isAuth;
        public final int isCustomSkinModel;
        public final int version;

        public byte[] fileBuffer;
        public int totalSize;
        public int bytesReceived;

        public ServerModelContext(long hash1, long hash2, String modelId, boolean isAuth, int isCustomSkinModel, int version) {
            this.uuid = new UUID(hash1, hash2);
            this.hash1 = hash1;
            this.hash2 = hash2;
            this.modelId = modelId;
            this.isAuth = isAuth;
            this.isCustomSkinModel = isCustomSkinModel;
            this.version = version;
        }
    }

    public static void loadDefaultModel() {
        YesSteveModel.LOGGER.info("[YSM] Loading builtin default model...");
        try {
            String resourcePath = "/assets/yes_steve_model/builtin/default";
            URL resourceUrl = YesSteveModel.class.getResource(resourcePath);
            if (resourceUrl == null) {
                YesSteveModel.LOGGER.error("[YSM] Builtin default model not found in classpath: " + resourcePath);
                return;
            }
            URI uri = resourceUrl.toURI();
            Path defaultPath;
            FileSystem jarFs = null;
            if ("jar".equals(uri.getScheme())) {
                try {
                    jarFs = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException e) {
                    jarFs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }
                defaultPath = jarFs.getPath(resourcePath);
            } else {
                defaultPath = Paths.get(uri);
            }

            try( YSMFolderDeserializer deserializer = new YSMFolderDeserializer(defaultPath)) {
            RawYsmModel rawModel = deserializer.deserialize();

            ClientModelInfo  parsedBundle = YSMClientMapper.buildParsedBundle(rawModel, "default");


                onModelDataReceived(parsedBundle, "default", true, false);
                YesSteveModel.LOGGER.info("[YSM] Successfully pushed Default Model to render queue.");
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("[YSM] Failed to dispatch Default Model", e);
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("[YSM] Failed to load builtin default model", e);
        }
    }

    private static void processServerData(ByteBuffer data) {
        if (data == null) {
            resetClientState();
            return;
        }
        try {
            if (!data.hasRemaining() && data.position() > 0) {
                data.flip();
            }
            if (!data.hasRemaining()) return;

            byte[] packetBytes = new byte[data.remaining()];
            data.get(packetBytes);

            byte[] decrypted;
            if (syncStep == 1) {
                decrypted = YsmCrypt.decrypt(packetBytes, YsmCrypt.publicKey);
                System.out.println(Arrays.toString(decrypted));
                if (decrypted != null) handlePacket01(decrypted);
            } else if (syncStep == 2) {
                decrypted = YsmCrypt.decrypt(packetBytes, lastKey);
                if (decrypted != null) {
                    try (YSMByteBuf buf = new YSMByteBuf(Unpooled.wrappedBuffer(decrypted))){
                        handlePacket03(buf);
                    }
                }
            } else if (syncStep == 3) {
                decrypted = YsmCrypt.decrypt(packetBytes, key1);
                if (decrypted != null) {
                    try (YSMByteBuf buf = new YSMByteBuf(Unpooled.wrappedBuffer(decrypted))) {
                        handlePacket05(buf);
                    }
                }
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("[YSM] Sync Error at step " + syncStep, e);
        }
    }

    private static void handlePacket01(byte[] decryptedBuffer) throws Exception {
        key1 = new byte[56];
        System.arraycopy(decryptedBuffer, decryptedBuffer.length - 56, key1, 0, 56);
        syncStep = 2;

        YesSteveModel.LOGGER.info("[YSM] Exchanged Key1. Preparing to send Packet 02.");
        onSyncProgress(-1); // Preparing GUI stage

        int garbageLen = 16 + SECURE_RANDOM.nextInt(48);
        byte[] garbage = new byte[garbageLen];
        SECURE_RANDOM.nextBytes(garbage);

        try (YSMByteBuf outBuf = new YSMByteBuf(Unpooled.buffer())) {
            outBuf.writeGarbageHeader(garbageLen, garbage);
            outBuf.getRawBuf().writeByte(0x02);
            outBuf.getRawBuf().writeByte(0x00);

            YsmCrypt.EncryptedPacket result = YsmCrypt.encrypt(outBuf.toArray(), key1, true);
            lastKey = result.nextKey();

            sendModelFile(ByteBuffer.wrap(result.data()));
        }
    }

    private record ModelHash(long hash1, long hash2) {}
    private static final List<ModelHash> cachedModelHashes = new ArrayList<>();

    private static void handlePacket03(YSMByteBuf buf) throws Exception {
        buf.skipGarbageHeader();
        int type = buf.readVarInt(); // expect 3
        long l = buf.readVarLong();
        currentCacheFolderName = Long.toHexString(l);

        serverKey = new byte[56];
        buf.getRawBuf().readBytes(serverKey);

        clientKey = new byte[56];
        buf.getRawBuf().readBytes(clientKey);

        File cacheDir = ServerModelManager.CACHE_CLIENT.resolve(currentCacheFolderName).toFile();
        if (!cacheDir.exists()) cacheDir.mkdirs();

        Map<UUID, File> localCacheMap = YSMClientCache.buildCacheIndex(cacheDir, clientKey);
        List<ModelHash> modelsToRequest = new ArrayList<>();

        int unkSize = buf.readVarInt();
        onSyncProgress(unkSize);

        for (int i = 0; i < unkSize; i++) {
            long hash1 = buf.readVarLong();
            long hash2 = buf.readVarLong();
            ModelHash mHash = new ModelHash(hash1, hash2);
            cachedModelHashes.add(mHash);

            String modelId = buf.readString();
            boolean isAuth = buf.readVarInt() == 1;// isAuth
            int isCustomSkinModel = buf.readVarInt();// is misc/2_steve misc/1_alex
//            System.out.println("Received model hash: " + mHash + ", id: " + modelId + ", unk1: " + isAuth + ", unk2: " + isCustomSkinModel);
            int version = buf.readVarInt(); // 对于文件夹未加密的模型，为65535

            ServerModelContext ctx = new ServerModelContext(hash1, hash2, modelId, isAuth, isCustomSkinModel, version);
            serverModels.put(ctx.uuid, ctx);

            File cachedFile = localCacheMap.get(ctx.uuid);

            if (YSMClientCache.verifyFileContent(cachedFile, hash1, hash2)) {
                YesSteveModel.LOGGER.info("[YSM] Cache HIT & Validated: " + ctx.uuid);
                // 命中缓存
                byte[] fileBytes = Files.readAllBytes(cachedFile.toPath());
                byte[] decompressed = YsmCrypt.read(fileBytes, clientKey);
                parseAndLoadModel(decompressed, modelId, isAuth);
            } else {
                YesSteveModel.LOGGER.info("[YSM] Cache MISS or Invalid: " + ctx.uuid + " -> Requesting...");
                modelsToRequest.add(mHash);
            }
        }

        int unkSize2 = buf.readVarInt();
        List<ModelPackData> parsedPacks = new ArrayList<>();

        for (int i = 0; i < unkSize2; i++) {
            String folderPath = buf.readString();

            OuterFileTexture iconTexture = null;
            if (buf.readVarInt() != 0) {
                byte[] textureData = buf.readByteArray();
                int textureWidth = buf.readVarInt();
                int textureHeight = buf.readVarInt();
                int imageFormat = buf.readVarInt();
                int unkImageData = buf.readVarInt();

                iconTexture = new OuterFileTexture(textureData);
            }

            String folderName = "";
            String folderDesc = "";
            int hasYSMPackInfo = buf.readVarInt();
            if (hasYSMPackInfo != 0) {
                folderName = buf.readString();
                folderDesc = buf.readString();
            }

            Map<String, Map<String, String>> languageData = new HashMap<>();
            int languageSize = buf.readVarInt();
            for (int j = 0; j < languageSize; j++) {
                String languageType = buf.readString();
                int translateKeySize = buf.readVarInt();
                Map<String, String> translationMap = new HashMap<>();
                for (int i1 = 0; i1 < translateKeySize; i1++) {
                    translationMap.put(buf.readString(), buf.readString());
                }
                languageData.put(languageType, translationMap);
            }
            parsedPacks.add(new ModelPackData(folderPath, folderName, folderDesc, iconTexture, languageData));
        }

        if (!parsedPacks.isEmpty()) {
            onModelPacksReceived(parsedPacks.toArray(new ModelPackData[0]));
        }

        Set<String> validServerModelIds = new HashSet<>();
        for (ServerModelContext ctx : serverModels.values()) {
            validServerModelIds.add(ctx.modelId);
        }
        List<String> modelsToRemove = new ArrayList<>();

//        if (modelAssemblyMap != null) {
//            for (String loadedId : modelAssemblyMap.keySet()) {
//                if (!validServerModelIds.contains(loadedId) && !"default".equals(loadedId)) {
//                    modelsToRemove.add(loadedId);
//                }
//            }
//        }
//
//        if (!modelsToRemove.isEmpty()) {
//            onModelContextsUpdated(modelsToRemove.toArray(new String[0]), null, null, null);
//            YesSteveModel.LOGGER.info("[YSM] Cleaned up {} outdated models during sync.", modelsToRemove.size());
//        }

        syncStep = 3;
        pendingModelsCount = modelsToRequest.size();

        int garbageLen = 16 + SECURE_RANDOM.nextInt(48);
        byte[] garbage = new byte[garbageLen];
        SECURE_RANDOM.nextBytes(garbage);

        try (YSMByteBuf outBuf = new YSMByteBuf(Unpooled.buffer())) {
            outBuf.writeGarbageHeader(garbageLen, garbage);
            outBuf.getRawBuf().writeByte(0x04);

            outBuf.writeVarInt(modelsToRequest.size());
            for (ModelHash h : modelsToRequest) {
                outBuf.writeVarLong(h.hash1);
                outBuf.writeVarLong(h.hash2);
            }

            YsmCrypt.EncryptedPacket result = YsmCrypt.encrypt(outBuf.toArray(), key1, false);
            sendModelFile(ByteBuffer.wrap(result.data()));
        }

        if (pendingModelsCount == 0) {
            YesSteveModel.LOGGER.info("[YSM] All models loaded from local cache. Handshake complete!");
            onSyncComplete();
        }
    }

    private static void handlePacket05(YSMByteBuf buf) throws Exception {
        buf.skipGarbageHeader();
        int type = buf.readVarInt();
        if (type != 5) return;

        long hash1 = buf.readVarLong();
        long hash2 = buf.readVarLong();
        UUID uuid = new UUID(hash1, hash2);

        ServerModelContext ctx = serverModels.get(uuid);
        if (ctx == null) {
            YesSteveModel.LOGGER.warn("[YSM] Received unexpected file chunk for model: " + uuid);
            return;
        }

        int totalSize = buf.readVarInt();
        int chunkOffset = buf.readVarInt();
        int chunkLength = buf.readVarInt();

        // 首次接收时初始化缓冲区
        if (ctx.fileBuffer == null) {
            ctx.fileBuffer = new byte[totalSize];
            ctx.totalSize = totalSize;
            ctx.bytesReceived = 0;
        }

        buf.getRawBuf().readBytes(ctx.fileBuffer, chunkOffset, chunkLength);
        ctx.bytesReceived += chunkLength;

        if (ctx.bytesReceived >= totalSize) {
            String folder = currentCacheFolderName != null ? currentCacheFolderName : "default_cache";
            File cacheDir = ServerModelManager.CACHE_CLIENT.resolve(folder).toFile();
            if (!cacheDir.exists()) cacheDir.mkdirs();

            byte[] cachedFileData = YsmCrypt.transcodeServerDataToClientCache(ctx.fileBuffer, serverKey, clientKey, hash1, hash2);

            String legitFileName = YSMClientCache.generateCacheFileName(hash1, hash2, clientKey);
            File outFile = new File(cacheDir, legitFileName);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(cachedFileData);
            }
            ctx.fileBuffer = null;

            YesSteveModel.LOGGER.info("[YSM] Downloaded & Cached: " + outFile.getAbsolutePath());
            byte[] decompressed = YsmCrypt.read(cachedFileData, clientKey);

            parseAndLoadModel(decompressed, ctx.modelId, ctx.isAuth);

            pendingModelsCount--;
            if (pendingModelsCount <= 0) {
                YesSteveModel.LOGGER.info("[YSM] All missing models downloaded and loaded successfully!");
                onSyncComplete();
            }
        }
    }


    private static void parseAndLoadModel(byte[] decompressed, String modelId, boolean isAuth) {
        try {
//            if (true) return;
            // IR

            try (YSMBinaryDeserializer deserializer = new YSMBinaryDeserializer(decompressed, 32)) {
                RawYsmModel rawModel = deserializer.deserializeKeepOpen();
                YSMByteBuf reader = deserializer.getReader();

                // 读取版本号
                rawModel.footer.version = reader.readVarInt();// 65535 或 32

                rawModel.footer.unkInt1 = reader.readVarInt(); // 待分析
                if (rawModel.footer.unkInt1 != 0) {
                    rawModel.footer.rand = reader.readString();
                }

                rawModel.footer.time = reader.readVarLong();

                if (rawModel.footer.unkInt1 != 0) {
                    rawModel.footer.extra = reader.readString();
                    rawModel.footer.unkInt2 = reader.readVarInt();
                }

                // 组装到客户端模型
                ClientModelInfo parsedBundle = YSMClientMapper.buildParsedBundle(rawModel, modelId);
                onModelDataReceived(parsedBundle, modelId, false, isAuth);
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("[YSM] Failed to parse and load model: " + modelId, e);
        }
    }

    private static OrderedStringMap<String, OuterFileTexture> toOrderedTextureMap(Map<String, OuterFileTexture> textures) {
        if (textures == null || textures.isEmpty()) {
            return new OrderedStringMap<>(new String[0], new OuterFileTexture[0]);
        }
        return new OrderedStringMap<>(
                textures.keySet().toArray(new String[0]),
                textures.values().toArray(new OuterFileTexture[0])
        );
    }

    private static void resetClientState() {
        syncStep = 1;
        key1 = null;
        lastKey = null;
        serverKey = null;
        clientKey = null;
        currentCacheFolderName = null;
        pendingModelsCount = 0;
        cachedModelHashes.clear();

        serverModels.clear();

        Map<String, ModelAssembly> oldModels = modelAssemblyMap;
        if (oldModels != null && !oldModels.isEmpty()) {
            Minecraft.getInstance().execute(() -> {
                for (ModelAssembly model : oldModels.values()) {
                    if (model != null) {
                        for (AbstractTexture tex : model.getTextures()) {
                            UploadManager.removeTexture(tex);
                        }
                    }
                }
            });
        }

        Map<String, ModelPackData> oldPreviews = modelPackMap;
        if (oldPreviews != null && !oldPreviews.isEmpty()) {
            for (ModelPackData preview : oldPreviews.values()) {
                if (preview.getTexture() != null) {
                    ResourceLocation loc = FileTypeUtil.getPackIconLocation(preview.getPath());
                    Minecraft.getInstance().execute(() -> {
                        Minecraft.getInstance().getTextureManager().release(loc);
                    });
                }
            }
        }

        modelAssemblyMap = Object2ReferenceMaps.emptyMap();
        modelPackMap = new Object2ReferenceOpenHashMap<>();
//        localModelContext = null;
        pendingModelCallback = null;
        pendingModelQueue.clear();

        forEachGuiWidget(l -> {
            try { l.onSyncBegin(); }
            catch (Throwable t) { t.printStackTrace(); }
        });
    }

    public static SyncStatus getSyncStatus() {
        RenderSystem.assertOnGameThread();
        return syncState;
    }

    public static Map<String, ModelAssembly> getModelAssemblyMap() {
        return modelAssemblyMap;
    }

    public static Map<String, ModelPackData> getModelPackMap() {
        return modelPackMap;
    }

    public static Optional<ModelAssembly> getModelContext(String str) {
        return Optional.ofNullable(modelAssemblyMap.get(str));
    }

    public static ModelAssembly getLocalModelContext() {
        runPendingModelCallback();
        flushPendingModels();

        ModelAssembly model = localModelContext;
        if (model != null) return model;

        // 触发预加载
        loadDefaultModel();
        model = localModelContext;
        if (model != null) return model;

        Map<String, ModelAssembly> reg = modelAssemblyMap;
        if (reg != null && !reg.isEmpty()) {
            model = reg.get("default");
            if (model == null) {
                for (ModelAssembly v : reg.values()) {
                    if (v != null) { model = v; break; }
                }
            }
            if (model != null) {
                localModelContext = model;
                return model;
            }
        }
        return null;
    }

    public static ResourceLocation getDefaultTexture() {
        return defaultTexture.getResourceLocation().get();
    }

    public static <T extends IGuiWidget> T registerGuiWidget(T t) {
        guiWidgets.put(t, null);
        return t;
    }

    public static void unregisterGuiWidget(IGuiWidget guiWidget) {
        guiWidgets.remove(guiWidget, null);
    }

    private static void forEachGuiWidget(Consumer<IGuiWidget> consumer) {
        Iterator<IGuiWidget> it = guiWidgets.keySet().iterator();
        while (it.hasNext()) {
            try {
                consumer.accept(it.next());
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public static void resetSync() {
        processServerData(null);
        Minecraft.getInstance().execute(() -> {
            syncState.setState(SyncState.WAITING);
        });
    }

    private static void sendModelFile(ByteBuffer byteBuffer) {
        if (Minecraft.getInstance().player != null) {
            try {
                NetworkHandler.CHANNEL.sendToServer(new C2SModelSyncPayload(byteBuffer));
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        Connection connection = serverConnection;
        if (!connection.isConnected()) {
            return;
        }
        try {
            connection.send(NetworkHandler.CHANNEL.toVanillaPacket(new C2SModelSyncPayload(byteBuffer), NetworkDirection.PLAY_TO_SERVER));
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static void startSync(Connection connection, ByteBuffer byteBuffer) {
        serverConnection = connection;
        processServerData(byteBuffer);
    }

    public static void onSyncConnected() {
        if (Minecraft.getInstance().isLocalServer()) {
            syncState.setState(SyncState.LOADING);
        } else {
            syncState.setState(SyncState.IDLE);
        }
        forEachGuiWidget(IGuiWidget::onSyncBegin);
    }

    private static void onSyncProgress(int totalModels) {
        if (totalModels == -1) {
            Minecraft.getInstance().execute(() -> {
                syncState.setState(SyncState.PREPARING);
                forEachGuiWidget(IGuiWidget::onSyncError);
            });
        } else {
            Minecraft.getInstance().execute(() -> {
                if (totalModels > 0) {
                    syncState.startSyncing(totalModels);
                } else {
                    syncState.setState(SyncState.IDLE);
                }
                forEachGuiWidget(guiWidget -> guiWidget.onSyncProgress(totalModels, 0));
            });
        }
    }

    private static void onModelPacksReceived(ModelPackData[] packDataArr) {
        Object2ReferenceOpenHashMap<String, ModelPackData> newPackMap = new Object2ReferenceOpenHashMap<>();

        for (ModelPackData packData : packDataArr) {
            if (StringUtils.isBlank(packData.getName())) {
                packData = new ModelPackData(packData.getPath(), FileTypeUtil.getFinalPathSegment(packData.getPath()), packData.getDescription(), packData.getTexture(), packData.getTranslations());
            }
            newPackMap.put(packData.getPath(), packData);
            OuterFileTexture iconTexture = packData.getTexture();
            if (iconTexture != null) {
                ResourceLocation location2 = FileTypeUtil.getPackIconLocation(packData.getPath());
                Minecraft.getInstance().submit(() -> {
                    Minecraft.getInstance().textureManager.register(location2, iconTexture);
                });
            }
        }

        for (ModelPackData packData : modelPackMap.values()) {
            if (!newPackMap.containsKey(packData.getPath()) && packData.getTexture() != null) {
                ResourceLocation location = FileTypeUtil.getPackIconLocation(packData.getPath());
                Minecraft.getInstance().submit(() -> Minecraft.getInstance().textureManager.release(location));
            }
        }
        modelPackMap = newPackMap;
    }

    private static void onModelContextsUpdated(String[] removedModelIds, String[] previousModelIds, String[] updatedModelIds, boolean[] isModelReady) {
        Minecraft.getInstance().execute(() -> {
            Object2ReferenceOpenHashMap<String, ModelAssembly> map = new Object2ReferenceOpenHashMap<>(modelAssemblyMap);
            if (removedModelIds != null) {
                ArrayList<ModelAssembly> removed = new ArrayList<>(removedModelIds.length);
                for (String str : removedModelIds) {
                    ModelAssembly assembly = map.remove(str);
                    if (assembly != null) {
                        removed.add(assembly);
                    }
                }
                Minecraft.getInstance().execute(() -> {
                    for (ModelAssembly assembly : removed) {
                        for (AbstractTexture tex : assembly.getTextures())
                            UploadManager.removeTexture(tex);
                    }
                });
            }
            if (previousModelIds != null) {
                ModelAssembly[] modelAssemblies = new ModelAssembly[previousModelIds.length];
                for (int i = 0; i < previousModelIds.length; i++) {
                    modelAssemblies[i] = map.remove(previousModelIds[i]);
                }
                for (int i = 0; i < modelAssemblies.length; i++) {
                    ModelAssembly modelAssembly = modelAssemblies[i];
                    if (modelAssembly != null) {
                        modelAssembly.getTextureRegistry().setAuthModel(isModelReady[i]);
                        map.put(updatedModelIds[i], modelAssembly);
                    }
                }
            }
            modelAssemblyMap = map;
            if ((removedModelIds != null && removedModelIds.length > 0) || (previousModelIds != null && previousModelIds.length > 0)) {
                forEachGuiWidget(guiWidget -> {
                    guiWidget.onModelsLoaded(map);
                });
            }
        });
    }

    private static void onModelDataReceived(@Nullable ClientModelInfo parsedBundle, String modelId, boolean isPrimary, boolean isAuth) throws Exception {
        if (isPrimary) {
            pendingModelCallback = () -> {
                processModelData(parsedBundle, modelId, true, false);
            };
        } else {
            runPendingModelCallback();
            processModelData(parsedBundle, modelId, false, isAuth);
        }
    }

    public static void runPendingModelCallback() {
        Runnable runnable = pendingModelCallback;
        if (runnable != null) {
            synchronized (runnable) {
                Runnable runnable2 = pendingModelCallback;
                if (runnable2 != null) {
                    runnable2.run();
                    pendingModelCallback = null;
                }
            }
        }
    }

    public static void processModelData(@Nullable ClientModelInfo parsedBundle, String modelId, boolean isPrimary, boolean isAuth) {
        if (parsedBundle != null) {
            try {
                ModelAssembly runtimeModel = ModelAssemblyFactory.buildAssembly(parsedBundle, isPrimary, isAuth);
                pendingModelQueue.add(Pair.of(runtimeModel, modelId));
                if (isPrimary) {
                    localModelContext = runtimeModel;

                    Minecraft.getInstance().execute(() -> {
                        defaultTexture = UploadManager.getOrCreateLocatable(runtimeModel.getAnimationBundle().getTextures().getValueAt(0), true);
                    });
                    return;
                }
            } catch (Exception e) {
                if (isPrimary) throw e;
                YesSteveModel.LOGGER.error(
                        new StringFormattedMessage("Failed to process {}", modelId), e);
                return;
            }
        }
        Minecraft.getInstance().execute(() -> {
            if (syncState.currentState == SyncState.SYNCING) {
                syncState.syncedModels++;
                int loaded = syncState.syncedModels;
                if (loaded == syncState.totalModels) {
                    syncState.setState(SyncState.IDLE);
                }
                forEachGuiWidget(guiWidget -> {
                    guiWidget.onSyncProgress(syncState.getTotalModels(), loaded);
                });
            }
        });
    }

    private static void onSyncComplete() {
        syncStep = 1;
        serverModels.clear();
        cachedModelHashes.clear();

        Minecraft.getInstance().execute(() -> {
            syncState.setState(SyncState.IDLE);
            forEachGuiWidget(IGuiWidget::onSyncComplete);
        });
    }

    private static void onSyncError(@Nullable Object obj) {
        Minecraft.getInstance().execute(() -> {
            syncState.setState(SyncState.IDLE);
            forEachGuiWidget(guiWidget -> {
                guiWidget.onSyncMessage(obj == null ? null : (Component) obj);
            });
            if (obj instanceof Component component) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(component);
                }
                YesSteveModel.LOGGER.error(component.getString(256));
            }
        });
    }

    public static void flushPendingModels() {
        if (pendingModelQueue.isEmpty())
            return;

        Object2ReferenceOpenHashMap<String, ModelAssembly> object2ReferenceOpenHashMap = new Object2ReferenceOpenHashMap<>(modelAssemblyMap);
        while (true) {
            Pair<ModelAssembly, String> pairPoll = pendingModelQueue.poll();
            if (pairPoll != null) {
                object2ReferenceOpenHashMap.put(pairPoll.getRight(), pairPoll.getLeft());
            } else {
                modelAssemblyMap = object2ReferenceOpenHashMap;
                forEachGuiWidget(guiWidget -> guiWidget.onModelsUpdated(object2ReferenceOpenHashMap));
                return;
            }
        }
    }

    public static int getPendingModelCount() {
        return pendingModelQueue.size();
    }

    public static class SyncStatus {
        private SyncState currentState = SyncState.WAITING;

        private int totalModels = -1;

        private int syncedModels = -1;

        public SyncState getCurrentState() {
            return this.currentState;
        }

        public int getSyncedModels() {
            return this.syncedModels;
        }

        public int getTotalModels() {
            return this.totalModels;
        }

        public void setState(SyncState syncState) {
            this.currentState = syncState;
            this.totalModels = -1;
            this.syncedModels = -1;
        }

        public void startSyncing(int totalModels) {
            this.currentState = SyncState.SYNCING;
            this.totalModels = totalModels;
            this.syncedModels = 0;
        }
    }

    public static void exportAllCachedModels(@Nullable String extra, @Nullable Consumer<ExportResult> callback) {
        YSMThreadPool.submit(() -> {
            try {
                if (clientKey == null) {
                    if (callback != null) {
                        callback.accept(new ExportResult(false, Component.literal("未连接到服务器或尚未完成握手同步，无法获取客户端解密密钥。"), "", "", 0));
                    }
                    return;
                }

                String folder = currentCacheFolderName != null ? currentCacheFolderName : "default_cache";
                File cacheDir = ServerModelManager.CACHE_CLIENT.resolve(folder).toFile();

                if (!cacheDir.exists() || !cacheDir.isDirectory()) {
                    if (callback != null) {
                        callback.accept(new ExportResult(false, Component.literal("尚未生成任何缓存或缓存文件夹不存在: " + folder), "", "", 0));
                    }
                    return;
                }

                File[] files = cacheDir.listFiles();
                if (files == null || files.length == 0) {
                    if (callback != null) {
                        callback.accept(new ExportResult(false, Component.literal("缓存文件夹中没有任何模型可供导出。"), "", "", 0));
                    }
                    return;
                }

                int successCount = 0;
                for (File file : files) {
                    if (!file.isFile()) continue;

                    try {
                        byte[] fileBytes = Files.readAllBytes(file.toPath());
                        byte[] clearText = YsmCrypt.read(fileBytes, clientKey);

                        int coreDataLength;
                        String exportName = file.getName(); // Fallback name

                        try (YSMBinaryDeserializer deserializer = new YSMBinaryDeserializer(clearText, 32)) {
                            RawYsmModel rawModel = deserializer.deserializeKeepOpen();
                            // 获取底层流确切偏移量，规避假尾部的固定偏移
                            coreDataLength = deserializer.getReader().getRawBuf().readerIndex();

                            // 尝试使用模型自身的名称进行命名
                            if (rawModel.metadata != null && rawModel.metadata.name != null && !rawModel.metadata.name.trim().isEmpty()) {
                                exportName = rawModel.metadata.name.trim();
                            } else if (rawModel.properties != null && rawModel.properties.sha256 != null && !rawModel.properties.sha256.isEmpty()) {
                                exportName = rawModel.properties.sha256;
                            }
                        }

                        // 过滤掉所有不合法的文件名字符
                        exportName = exportName.replaceAll("[\\\\/:*?\"<>|]", "_");

                        try (YSMByteBuf outBuf = new YSMByteBuf(Unpooled.buffer())) {
                            outBuf.writeDword(32);

                            // 写入剔除了尾部验证信息的核心流数据
                            outBuf.getRawBuf().writeBytes(clearText, 0, coreDataLength);

                            // ======= 注入合法的导出级 Footer Metadata ========
                            outBuf.writeVarInt(32); // Version
                            outBuf.writeVarInt(1);  // Flag 开启加密标识位

                            byte[] randBytes = new byte[8];
                            SECURE_RANDOM.nextBytes(randBytes);
                            StringBuilder sb = new StringBuilder(16);
                            for (byte b : randBytes) {
                                sb.append(String.format("%02x", b));
                            }
                            outBuf.writeString(sb.toString()); // rand hash

                            outBuf.writeVarLong(java.time.Instant.now().getEpochSecond()); // time
                            outBuf.writeString(extra != null ? extra : ""); // extra info
                            outBuf.writeVarInt(0); // padding

                            byte[] rawBytes = new byte[outBuf.getRawBuf().readableBytes()];
                            outBuf.getRawBuf().readBytes(rawBytes);

                            byte[] finalEncrypted = YsmCrypt.encryptYsmFile(rawBytes);

                            Path exportPath = ServerModelManager.EXPORT.resolve(exportName + ".ysm");
                            Files.createDirectories(exportPath.getParent());
                            Files.write(exportPath, finalEncrypted);

                            successCount++;
                            YesSteveModel.LOGGER.info("[YSM] Successfully exported cached model to: " + exportPath);
                        }
                    } catch (Exception e) {
                        YesSteveModel.LOGGER.error("[YSM] Failed to export cached model: " + file.getName(), e);
                    }
                }

                if (callback != null) {
                    String displayPath = Paths.get("export").toString();
                    if (successCount > 0) {
                        callback.accept(new ExportResult(true, null, displayPath, "", 0));
                    } else {
                        callback.accept(new ExportResult(false, Component.literal("导出完成，但没有成功导出任何模型。可能是缓存已损坏。"), "", "", 0));
                    }
                }
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("[YSM] Error during batch export", e);
                if (callback != null) {
                    callback.accept(new ExportResult(false, Component.literal("批量导出过程发生严重错误: " + e.getMessage()), "", "", 0));
                }
            }
        });
    }
}
