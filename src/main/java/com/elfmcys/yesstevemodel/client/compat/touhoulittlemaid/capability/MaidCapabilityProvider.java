package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.Direction;
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
public class MaidCapabilityProvider implements ICapabilityProvider {

    public static final Capability<MaidCapability> MAID_CAP = CapabilityManager.get(new CapabilityToken<MaidCapability>() {
    });

    private MaidCapability capability;

    private final EntityMaid maid;

    public MaidCapabilityProvider(EntityMaid entityMaid) {
        this.maid = entityMaid;
    }

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return getCapability(capability);
    }

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability) {
        return MAID_CAP.orEmpty(capability, LazyOptional.of(this::getOrCreateCapability));
    }

    private MaidCapability getOrCreateCapability() {
        if (this.capability == null) {
            this.capability = new MaidCapability(this.maid, true);
        }
        return this.capability;
    }
}