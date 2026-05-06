package com.elfmcys.yesstevemodel.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelInfoCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

    public static Capability<ModelInfoCapability> MODEL_INFO_CAP = CapabilityManager.get(new CapabilityToken<ModelInfoCapability>() {
    });

    private ModelInfoCapability capability = null;

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return MODEL_INFO_CAP.orEmpty(capability, LazyOptional.of(this::getOrCreateCapability));
    }

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability) {
        return getCapability(capability, null);
    }

    @NotNull
    private ModelInfoCapability getOrCreateCapability() {
        if (this.capability == null) {
            this.capability = new ModelInfoCapability();
        }
        return this.capability;
    }

    public void deserializeNBT(CompoundTag compoundTag) throws NumberFormatException {
        getOrCreateCapability().deserializeNBT(compoundTag);
    }

    public CompoundTag serializeNBT() {
        return getOrCreateCapability().serializeNBT();
    }
}