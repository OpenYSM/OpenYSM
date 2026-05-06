package com.elfmcys.yesstevemodel.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ProjectileCapabilityProvider implements ICapabilityProvider {

    public static Capability<ProjectileCapability> PROJECTILE_CAP = CapabilityManager.get(new CapabilityToken<ProjectileCapability>() {
    });

    private ProjectileCapability capability;

    private Projectile projectile;

    public ProjectileCapabilityProvider(Projectile projectile) {
        this.projectile = projectile;
    }

    public ProjectileCapability getOrCreateCapability() {
        if (this.capability == null) {
            this.capability = new ProjectileCapability(this.projectile);
            this.projectile = null;
        }
        return this.capability;
    }

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return PROJECTILE_CAP.orEmpty(capability, this.capability == null ? LazyOptional.empty() : LazyOptional.of(() -> this.capability));
    }
}