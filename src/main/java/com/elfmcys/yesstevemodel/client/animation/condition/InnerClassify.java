package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import com.elfmcys.yesstevemodel.client.compat.slashblade.SlashBladeCompat;
import com.elfmcys.yesstevemodel.util.ItemTagsConstants;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

public class InnerClassify {

    private static final String EMPTY = "";

    public static String doClassifyTest(String str, LivingEntity livingEntity, InteractionHand interactionHand) {
        String itemType = getItemType(livingEntity.getItemInHand(interactionHand));
        if (!itemType.equals("")) {
            return str + itemType;
        }
        return "";
    }

    public static String getItemType(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (SlashBladeCompat.isSlashBladeItem(itemStack)) {
            return "slashblade";
        }
        if ((item instanceof SwordItem) || itemStack.is(ItemTagsConstants.SWORDS)) {
            return "sword";
        }
        if (TouhouLittleMaidCompat.isMaidItem(item)) {
            return "gohei";
        }
        if ((item instanceof AxeItem) || itemStack.is(ItemTagsConstants.AXES)) {
            return "axe";
        }
        if ((item instanceof PickaxeItem) || itemStack.is(ItemTagsConstants.PICKAXES)) {
            return "pickaxe";
        }
        if ((item instanceof ShovelItem) || itemStack.is(ItemTagsConstants.SHOVELS)) {
            return "shovel";
        }
        if ((item instanceof HoeItem) || itemStack.is(ItemTagsConstants.HOES)) {
            return "hoe";
        }
        if ((item instanceof ShieldItem) || itemStack.is(ItemTagsConstants.SHIELDS)) {
            return "shield";
        }
        if ((item instanceof CrossbowItem) || itemStack.is(ItemTagsConstants.CROSSBOWS)) {
            return "crossbow";
        }
        if ((item instanceof BowItem) || itemStack.is(ItemTagsConstants.BOWS)) {
            return "bow";
        }
        if ((item instanceof FishingRodItem) || itemStack.is(ItemTagsConstants.FISHING_RODS)) {
            return "fishing_rod";
        }
        if ((item instanceof TridentItem) || itemStack.is(ItemTagsConstants.TRIDENTS)) {
            return "spear";
        }
        if ((item instanceof ThrowablePotionItem) || itemStack.is(ItemTagsConstants.THROWABLE_POTION)) {
            return "throwable_potion";
        }
        return "";
    }
}