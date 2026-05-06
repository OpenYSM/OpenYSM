package com.elfmcys.yesstevemodel.client.compat.create;

import com.elfmcys.yesstevemodel.mixin.client.create.PlayerSkyhookRendererAccessor;
import net.minecraft.world.entity.player.Player;

public class SkyHookHelper {
    public static boolean isPlayerOnSkyHook(Player player) {
        return PlayerSkyhookRendererAccessor.hangingPlayers().contains(player.getUUID());
    }
}