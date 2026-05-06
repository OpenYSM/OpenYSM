package com.elfmcys.yesstevemodel.client.compat.slashblade;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Map;

public class SlashBladeComboHelper {

    private static final Map<String, String> comboNameAliases = Maps.newHashMap();

    static {
        comboNameAliases.put("slashblade:combo_a4_ex", "slashblade:combo_a4ex");
    }

    public static String getComboState(ISlashBladeState slashBladeState, long j, LivingEntity livingEntity) {
        ResourceLocation comboSeq = slashBladeState.getComboSeq();
        ComboState comboState = (ComboState) ((IForgeRegistry) ComboStateRegistry.REGISTRY.get()).getValue(comboSeq);
        if (comboState == null) {
            return StringPool.EMPTY;
        }
        int timeoutMS = comboState.getTimeoutMS();
        if ("slashblade:standby".equals(comboSeq.toString())) {
            timeoutMS -= 553;
        }
        if (j <= timeoutMS) {
            String comboName = normalizeComboName(comboSeq.toString());
            if ("slashblade:judgement_cut".equals(comboName) && !livingEntity.onGround()) {
                comboName = "slashblade:judgement_cut_slash_air";
            }
            if ("slashblade:judgement_cut_slash_just2".equals(comboName) && !livingEntity.onGround()) {
                comboName = "slashblade:judgement_cut_slash_air_just2";
            }
            return comboName;
        }
        return StringPool.EMPTY;
    }

    private static String normalizeComboName(String str) {
        if (comboNameAliases.containsKey(str)) {
            return comboNameAliases.get(str);
        }
        return str;
    }
}