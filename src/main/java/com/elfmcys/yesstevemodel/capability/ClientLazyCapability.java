package com.elfmcys.yesstevemodel.capability;

import org.jetbrains.annotations.Nullable;

public class ClientLazyCapability {

    private final VehicleCapabilityProvider entityRenderProvider;

    @Nullable
    private final ProjectileCapabilityProvider projectileAnimProvider;

    public ClientLazyCapability(VehicleCapabilityProvider capabilityProvider, @Nullable ProjectileCapabilityProvider capabilityProvider2) {
        this.entityRenderProvider = capabilityProvider;
        this.projectileAnimProvider = capabilityProvider2;
    }

    public VehicleCapabilityProvider getEntityRenderProvider() {
        return this.entityRenderProvider;
    }

    @Nullable
    public ProjectileCapabilityProvider getProjectileAnimProvider() {
        return this.projectileAnimProvider;
    }
}