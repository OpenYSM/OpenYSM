package com.elfmcys.yesstevemodel.client.compat.playeranimator;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.fml.ModList;

public class PlayerAnimatorCompat {

    private static final String MOD_ID = "playeranimator";

    private static boolean IS_LOADED = false;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static boolean isPlayerAnimated(AbstractClientPlayer abstractClientPlayer) {
        return IS_LOADED && PlayerAnimationAccess.getPlayerAnimLayer(abstractClientPlayer).getFirstPersonMode(0.0f) == FirstPersonMode.THIRD_PERSON_MODEL;
    }
}