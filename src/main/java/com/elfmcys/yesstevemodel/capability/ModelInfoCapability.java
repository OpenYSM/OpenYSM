package com.elfmcys.yesstevemodel.capability;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.sync.PlayerStateSynchronizer;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.network.message.S2CSetModelAndTexturePacket;
import com.elfmcys.yesstevemodel.network.message.FeedbackData;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class ModelInfoCapability {

    private String modelId;

    private String selectTexture;

    private boolean mandatory;

    private Int2ReferenceOpenHashMap<Object2FloatOpenHashMap<String>> molangStorage;

    private PlayerStateSynchronizer animSync;

    private boolean disabled;

    private boolean dirty;

    private final Queue<Consumer<Object2FloatOpenHashMap<String>>> pendingCallbacks;

    public ModelInfoCapability() {
        Pair<String, String> defaultModelConfig = ServerModelManager.getDefaultModelConfig();
        this.modelId = defaultModelConfig.getLeft();
        this.selectTexture = defaultModelConfig.getRight();
        this.molangStorage = new Int2ReferenceOpenHashMap<>();
        this.animSync = new PlayerStateSynchronizer();
        this.pendingCallbacks = Queues.newArrayDeque();
        this.disabled = false;
    }

    public void setModelAndTexture(String str, String str2) {
        if (this.modelId.equals(str) && this.selectTexture.equals(str2)) {
            return;
        }
        this.modelId = str;
        this.selectTexture = str2;
        markDirty();
    }

    public void resetToDefault() {
        Pair<String, String> pair = ServerModelManager.getDefaultModelConfig();
        setModelAndTexture(pair.getLeft(), pair.getRight());
    }

    public void copyFrom(ModelInfoCapability source) {
        this.molangStorage = source.molangStorage;
        this.modelId = source.modelId;
        this.selectTexture = source.selectTexture;
        this.mandatory = source.mandatory;
        this.animSync = source.animSync;
        this.pendingCallbacks.addAll(source.pendingCallbacks);
        this.disabled = source.disabled;
        source.pendingCallbacks.clear();
        markDirty();
    }

    public String getModelId() {
        return this.modelId;
    }

    public String getSelectTexture() {
        return this.selectTexture;
    }

    public void setSelectTexture(String str) {
        this.selectTexture = str;
        markDirty();
    }

    public void setDisabled(boolean z) {
        if (this.disabled != z) {
            this.disabled = z;
            markDirty();
        }
    }

    public void playAnimation(ServerPlayer serverPlayer, String str) {
        this.animSync.syncModelSwitch(serverPlayer, !this.dirty, str);
    }

    public void stopAnimation(ServerPlayer serverPlayer) {
        this.animSync.syncModelSwitch(serverPlayer, !this.dirty, StringPool.EMPTY);
    }

    public Optional<S2CSetModelAndTexturePacket> createSyncMessage(ServerPlayer serverPlayer, boolean z) {
        if ("default".equals(this.modelId)) {
            while (this.pendingCallbacks.poll() != null) {
                // The builtin default model has no server-side model definition/hash bucket.
            }
            return Optional.of(new S2CSetModelAndTexturePacket(serverPlayer.getId(), this.modelId, this.selectTexture, this.disabled, this.animSync.buildFullSyncMessage(serverPlayer, z)));
        }
        return ServerModelManager.getModelDefinition(this.modelId).map(it -> {
            Object2FloatOpenHashMap<String> object2FloatOpenHashMap = this.molangStorage.computeIfAbsent(it.getLoadedModelData().getHashId(), i -> new Object2FloatOpenHashMap<>(0));
            while (true) {
                Consumer<Object2FloatOpenHashMap<String>> consumerPoll = this.pendingCallbacks.poll();
                if (consumerPoll != null) {
                    consumerPoll.accept(object2FloatOpenHashMap);
                } else {
                    return new S2CSetModelAndTexturePacket(serverPlayer.getId(), this.modelId, this.selectTexture, this.disabled, this.animSync.buildFullSyncMessage(serverPlayer, z).setMolangVars(it.getLoadedModelData().getHashId(), object2FloatOpenHashMap));
                }
            }
        });
    }

    public void withMolangVars(Consumer<Object2FloatOpenHashMap<String>> consumer) {
        ServerModelManager.getModelDefinition(this.modelId).ifPresentOrElse(value -> {
            consumer.accept(this.molangStorage.computeIfAbsent(value.getLoadedModelData().getHashId(), i -> {
                return new Object2FloatOpenHashMap(0);
            }));
        }, () -> {
            this.pendingCallbacks.add(consumer);
        });
    }

    public Optional<Object2FloatOpenHashMap<String>> getMolangVars() {
        return ServerModelManager.getModelDefinition(this.modelId).map(serverModelData -> {
            return (Object2FloatOpenHashMap) this.molangStorage.computeIfAbsent(serverModelData.getLoadedModelData().getHashId(), i -> {
                return new Object2FloatOpenHashMap(0);
            });
        });
    }

    public void applyFeedback(ServerPlayer serverPlayer, FeedbackData feedbackData) {
        this.molangStorage.compute(feedbackData.entityId(), (num, object2FloatOpenHashMap) -> {
            if (object2FloatOpenHashMap != null) {
                object2FloatOpenHashMap.putAll(feedbackData.stringValues());
                return object2FloatOpenHashMap;
            }
            return new Object2FloatOpenHashMap(feedbackData.stringValues());
        });
        this.animSync.syncMolangVars(serverPlayer, !this.dirty, feedbackData.entityId(), feedbackData.stringValues());
    }

    public void retainAnimationKeys(IntSet intSet) {
        ObjectIterator objectIteratorFastIterator = this.molangStorage.int2ReferenceEntrySet().fastIterator();
        while (objectIteratorFastIterator.hasNext()) {
            if (!intSet.contains(((Int2ReferenceMap.Entry) objectIteratorFastIterator.next()).getIntKey())) {
                objectIteratorFastIterator.remove();
            }
        }
    }

    public PlayerStateSynchronizer getAnimSync() {
        return this.animSync;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public void setMandatory(boolean z) {
        if (this.mandatory != z) {
            this.mandatory = z;
            markDirty();
        }
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("model_id", this.modelId);
        compoundTag.putString("select_texture", this.selectTexture);
        compoundTag.putBoolean("mandatory", this.mandatory);
        compoundTag.putBoolean("disabled", this.disabled);
        CompoundTag compoundTag2 = new CompoundTag();
        this.molangStorage.int2ReferenceEntrySet().fastForEach(entry -> {
            CompoundTag compoundTag3 = new CompoundTag();
            entry.getValue().object2FloatEntrySet().fastForEach(entry2 -> {
                compoundTag3.putFloat(entry2.getKey(), entry2.getFloatValue());
            });
            compoundTag2.put(String.valueOf(entry.getIntKey()), compoundTag3);
        });
        compoundTag.put("molang_storage", compoundTag2);
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag compoundTag) throws NumberFormatException {
        this.modelId = compoundTag.getString("model_id");
        this.selectTexture = compoundTag.getString("select_texture");
        if (this.selectTexture.length() > 4 && this.selectTexture.toLowerCase().endsWith(".png")) {
            this.selectTexture = this.selectTexture.substring(0, this.selectTexture.length() - 4);
        }
        this.mandatory = compoundTag.getBoolean("mandatory");
        this.disabled = compoundTag.getBoolean("disabled");
        this.molangStorage.clear();
        CompoundTag compound = compoundTag.getCompound("molang_storage");
        for (String str : compound.getAllKeys()) {
            CompoundTag compound2 = compound.getCompound(str);
            int i = Integer.parseInt(str);
            Set<String> allKeys = compound2.getAllKeys();
            Object2FloatOpenHashMap object2FloatOpenHashMap = this.molangStorage.computeIfAbsent(i, i2 -> {
                return new Object2FloatOpenHashMap(allKeys.size());
            });
            for (String str2 : allKeys) {
                object2FloatOpenHashMap.put(str2, compound2.getFloat(str2));
            }
        }
    }
}
