package com.elfmcys.yesstevemodel.client.compat.bettercombat;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import com.elfmcys.yesstevemodel.util.VarHandleHelper;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.bettercombat.client.animation.AttackAnimationSubStack;
import net.minecraft.client.player.AbstractClientPlayer;

import java.lang.invoke.VarHandle;

public class BetterCombatBinding {

    private static VarHandle attackAnimationHandle;

    public static void initialize() {
        VarHandleHelper.findField(AbstractClientPlayer.class, "attackAnimation", AttackAnimationSubStack.class).ifPresent(varHandle -> {
            attackAnimationHandle = varHandle;
        });
        if (attackAnimationHandle == null) {
            return;
        }
        BetterCombatClientEvents.ATTACK_START.register(new BetterCombatAttackHandler());
    }

    public static void registerBindings(CtrlBinding binding) {
        binding.clientPlayerEntityVar("bcombat_attack_animation", BetterCombatBinding::getAttackAnimationName);
    }

    private static AttackAnimationSubStack getAttackAnimationStack(AbstractClientPlayer player) {
        return (AttackAnimationSubStack) attackAnimationHandle.get(player);
    }

    private static String getAttackAnimationName(IContext<AbstractClientPlayer> context) {
        KeyframeAnimationPlayer animation;
        AttackAnimationSubStack stack = getAttackAnimationStack(context.entity());
        if (stack == null || (animation = (KeyframeAnimationPlayer) stack.base.getAnimation()) == null || !animation.isActive() || !(animation instanceof KeyframeAnimationPlayer)) {
            return StringPool.EMPTY;
        }
        Object orDefault = animation.getData().extraData.getOrDefault("name", StringPool.EMPTY);
        return orDefault instanceof String ? (String) orDefault : StringPool.EMPTY;
    }
}