package com.elfmcys.yesstevemodel.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuthModelsCapabilityProvider implements ICapabilitySerializable<ListTag> {

    public static Capability<AuthModelsCapability> AUTH_MODELS_CAP = CapabilityManager.get(new CapabilityToken<AuthModelsCapability>() {
    });

    private AuthModelsCapability capability = null;

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return AUTH_MODELS_CAP.orEmpty(capability, LazyOptional.of(this::getOrCreateCapability));
    }

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability) {
        return getCapability(capability, null);
    }

    @NotNull
    private AuthModelsCapability getOrCreateCapability() {
        if (this.capability == null) {
            this.capability = new AuthModelsCapability();
        }
        return this.capability;
    }

    public void deserializeNBT(ListTag listTag) {
        getOrCreateCapability().deserializeNBT(listTag);
    }

    public ListTag serializeNBT() {
        return getOrCreateCapability().serializeNBT();
    }
}