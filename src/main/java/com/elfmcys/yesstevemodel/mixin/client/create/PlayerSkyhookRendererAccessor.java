package com.elfmcys.yesstevemodel.mixin.client.create;

import com.simibubi.create.foundation.render.PlayerSkyhookRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import java.util.UUID;

@Mixin({PlayerSkyhookRenderer.class})
public interface PlayerSkyhookRendererAccessor {
    @Accessor(value = "hangingPlayers", remap = false)
    static Set<UUID> hangingPlayers() {
        throw new AssertionError();
    }
}