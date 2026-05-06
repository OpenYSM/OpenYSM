package com.elfmcys.yesstevemodel.client.compat.gun.tacz;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TacBinding {
    public static void registerFunctions(CtrlBinding binding) {
        binding.livingEntityVar("tac_hold_gun", ctx -> IGun.mainhandHoldGun(ctx.entity()));
        binding.livingEntityVar("tac_gun_type", TacBinding::getMainHandGunId);
        binding.livingEntityVar("tac_gun_id", TacBinding::getOffHandGunId);
        binding.livingEntityVar("tac_is_fire", ctx -> IGunOperator.fromLivingEntity(ctx.entity()).getSynShootCoolDown() > 0);
        binding.livingEntityVar("tac_is_aim", ctx -> IGunOperator.fromLivingEntity(ctx.entity()).getSynAimingProgress() > 0.0f);
        binding.livingEntityVar("tac_is_reload", ctx -> IGunOperator.fromLivingEntity(ctx.entity()).getSynReloadState().getCountDown() > 0);
        binding.livingEntityVar("tac_is_melee", ctx -> IGunOperator.fromLivingEntity(ctx.entity()).getSynMeleeCoolDown() > 0);
        binding.livingEntityVar("tac_is_draw", ctx -> IGunOperator.fromLivingEntity(ctx.entity()).getSynDrawCoolDown() > 0);
        binding.livingEntityVar("tac_fire_mode", ctx -> {
            FireMode mainHandFireMode = IGun.getMainHandFireMode(ctx.entity());
            return mainHandFireMode != FireMode.UNKNOWN ? mainHandFireMode.name() : StringPool.EMPTY;
        });
    }

    private static String getMainHandGunId(IContext<LivingEntity> context) {
        ItemStack mainHandItem = context.entity().getMainHandItem();
        IGun iGunOrNull = IGun.getIGunOrNull(mainHandItem);
        if (iGunOrNull == null) {
            return StringPool.EMPTY;
        }
        Optional<CommonGunIndex> commonGunIndex = TimelessAPI.getCommonGunIndex(iGunOrNull.getGunId(mainHandItem));
        if (commonGunIndex.isEmpty()) {
            return StringPool.EMPTY;
        }
        return commonGunIndex.get().getType();
    }

    private static String getOffHandGunId(IContext<LivingEntity> context) {
        ItemStack mainHandItem = context.entity().getMainHandItem();
        IGun iGunOrNull = IGun.getIGunOrNull(mainHandItem);
        if (iGunOrNull == null) {
            return StringPool.EMPTY;
        }
        return iGunOrNull.getGunId(mainHandItem).toString();
    }
}