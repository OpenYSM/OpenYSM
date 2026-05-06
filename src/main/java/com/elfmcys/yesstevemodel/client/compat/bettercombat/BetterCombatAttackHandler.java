package com.elfmcys.yesstevemodel.client.compat.bettercombat;

import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SSwingArmPacket;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

public class BetterCombatAttackHandler implements BetterCombatClientEvents.PlayerAttackStart {
    public void onPlayerAttackStart(LocalPlayer localPlayer, AttackHand attackHand) {
        localPlayer.swingingArm = attackHand.isOffHand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        localPlayer.swingTime = -1;
        localPlayer.swinging = true;
        NetworkHandler.sendToServer(new C2SSwingArmPacket(localPlayer.swingingArm));
    }
}