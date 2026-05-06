package com.elfmcys.yesstevemodel.client.compat.cosmeticarmorreworked;

import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

public class CosmeticArmorCompat {

    private static final String MOD_ID = "cosmeticarmorreworked";

    private static boolean IS_LOADED;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static Optional<ItemStack> getCosmeticArmor(Player player, EquipmentSlot equipmentSlot) {
        if (!equipmentSlot.isArmor()) {
            return Optional.empty();
        }
        CAStacksBase cAStacksClient = CosArmorAPI.getCAStacksClient(player.getUUID());
        if (cAStacksClient.isSkinArmor(equipmentSlot.getIndex())) {
            return Optional.of(ItemStack.EMPTY);
        }
        ItemStack stackInSlot = cAStacksClient.getStackInSlot(equipmentSlot.getIndex());
        return stackInSlot.isEmpty() ? Optional.empty() : Optional.of(stackInSlot);
    }
}