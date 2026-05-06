package com.elfmcys.yesstevemodel.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientLazyCapabilityProvider implements ICapabilityProvider {

    public static Capability<ClientLazyCapability> CLIENT_LAZY_CAP = CapabilityManager.get(new CapabilityToken<ClientLazyCapability>() {
    });

    private final ClientLazyCapability capability;

    public ClientLazyCapabilityProvider(VehicleCapabilityProvider capabilityProvider, @Nullable ProjectileCapabilityProvider capabilityProvider2) {
        this.capability = new ClientLazyCapability(capabilityProvider, capabilityProvider2);
    }

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return CLIENT_LAZY_CAP.orEmpty(capability, LazyOptional.of(() -> this.capability));
    }
}