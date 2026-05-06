package com.elfmcys.yesstevemodel.capability;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.nbt.CompoundTag;

public class VehicleModelCapability {

    private String ownerModelId = "default";

    private boolean initialized = false;

    private Object2FloatOpenHashMap<String> molangVars = new Object2FloatOpenHashMap<>();

    public void setModel(String str, Object2FloatOpenHashMap<String> object2FloatOpenHashMap) {
        this.ownerModelId = str;
        this.initialized = true;
        this.molangVars = object2FloatOpenHashMap;
    }

    public void copyFrom(VehicleModelCapability other) {
        this.ownerModelId = other.ownerModelId;
        this.initialized = other.initialized;
        this.molangVars = other.molangVars;
    }

    public String getOwnerModelId() {
        return this.ownerModelId;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public Object2FloatOpenHashMap<String> getMolangVars() {
        return this.molangVars;
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("owner_model_id", this.ownerModelId);
        compoundTag.putBoolean("initialized", this.initialized);
        CompoundTag compoundTag2 = new CompoundTag();
        this.molangVars.object2FloatEntrySet().fastForEach(entry -> {
            compoundTag2.putFloat(entry.getKey(), entry.getFloatValue());
        });
        compoundTag.put("molang_vars_server_bound", compoundTag2);
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag compoundTag) {
        this.ownerModelId = compoundTag.getString("owner_model_id");
        this.initialized = compoundTag.getBoolean("initialized");
        this.molangVars.clear();
        CompoundTag compound = compoundTag.getCompound("molang_vars_server_bound");
        for (String str : compound.getAllKeys()) {
            this.molangVars.put(str, compound.getFloat(str));
        }
    }
}