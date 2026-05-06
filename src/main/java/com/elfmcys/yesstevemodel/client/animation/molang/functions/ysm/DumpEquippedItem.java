package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.client.compat.cosmeticarmorreworked.CosmeticArmorHelper;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

public class DumpEquippedItem extends LivingEntityFunction {
    @Override
    public Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        EquipmentSlot slot;
        ResourceLocation key;
        Enchantment enchantment;
        if (!context.entity().isDebugMode() || (slot = MolangUtils.parseSlotType(context.entity(), arguments.getAsString(context, 0))) == null) {
            return null;
        }
        ItemStack stack = CosmeticArmorHelper.getArmorItem(context.entity().entity(), slot);
        if (stack.isEmpty() || (key = ForgeRegistries.ITEMS.getKey(stack.getItem())) == null) {
            return null;
        }
        context.entity().logWarningComponent(Component.literal("Display ").append(ComponentUtils.copyOnClickText(stack.getItem().getName(stack).getString(99))));
        context.entity().logWarningComponent(Component.literal("Name ").append(ComponentUtils.copyOnClickText(key.toString())));
        stack.getTags().forEach(tagKey -> {
            context.entity().logWarningComponent(Component.literal("Tag ").append(ComponentUtils.copyOnClickText(tagKey.location().toString())));
        });
        for (Tag tag : stack.getEnchantmentTags()) {
            if (tag instanceof CompoundTag compoundTag) {
                ResourceLocation resourceLocationTryParse = ResourceLocation.tryParse(compoundTag.getString("id"));
                if (resourceLocationTryParse != null && (enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resourceLocationTryParse)) != null) {
                    context.entity().logWarningComponent(Component.literal("Enchantment: display ").append(ComponentUtils.copyOnClickText(enchantment.getFullname(compoundTag.getInt("lvl")).getString(99))).append(Component.literal("  name ").append(ComponentUtils.copyOnClickText(resourceLocationTryParse.toString()))));
                }
            }
        }
        return null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}