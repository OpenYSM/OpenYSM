package com.elfmcys.yesstevemodel.client.compat.elytraslot;

import com.illusivesoulworks.elytraslot.platform.Services;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class ElytraSlotCompat {

    private static final String MOD_ID = "elytraslot";

    private static boolean IS_LOADED;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static ItemStack getElytraItem(LivingEntity livingEntity) {
        return Services.ELYTRA.getEquipped(livingEntity);
    }
}