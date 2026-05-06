package com.elfmcys.yesstevemodel.client.compat.cosmeticarmorreworked;

import com.elfmcys.yesstevemodel.client.compat.elytraslot.ElytraSlotCompat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class CosmeticArmorHelper {
    public static ItemStack getArmorItem(LivingEntity entity, EquipmentSlot slot) {
        if (slot.isArmor() && (entity instanceof Player player)) {
            if (CosmeticArmorCompat.isLoaded()) {
                Optional<ItemStack> optional = CosmeticArmorCompat.getCosmeticArmor(player, slot);
                if (optional.isPresent()) {
                    return optional.get();
                }
            }
        }
        return entity.getItemBySlot(slot);
    }

    public static ItemStack getElytraItem(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            if (CosmeticArmorCompat.isLoaded()) {
                Optional<ItemStack> optional = CosmeticArmorCompat.getCosmeticArmor(player, EquipmentSlot.CHEST);
                if (optional.isPresent() && optional.get().getItem() == Items.ELYTRA) {
                    return optional.get();
                }
            }
        }
        if (ElytraSlotCompat.isLoaded()) {
            ItemStack stack = ElytraSlotCompat.getElytraItem(livingEntity);
            if (stack.getItem() == Items.ELYTRA) {
                return stack;
            }
        }
        ItemStack itemBySlot = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (itemBySlot.getItem() == Items.ELYTRA) {
            return itemBySlot;
        }
        return ItemStack.EMPTY;
    }
}