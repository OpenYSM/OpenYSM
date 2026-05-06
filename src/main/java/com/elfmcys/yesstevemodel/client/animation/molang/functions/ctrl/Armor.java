package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.StringUtils;

public class Armor extends LivingEntityFunction {

    private static final String PREFIX_ITEM_ID = "$";

    private static final String PREFIX_ITEM_TAG = "#";

    private static final String EMPTY_ITEM = "empty";

    public static Armor create() {
        return new Armor();
    }

    @Override
    public Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        ITagManager iTagManagerTags;
        EquipmentSlot slotType = MolangUtils.parseSlotType(context.entity(), arguments.getAsString(context, 0));
        if (slotType == null || !slotType.isArmor()) {
            return null;
        }
        String id = arguments.getAsString(context, 1);
        LivingEntity entity = context.entity().entity();
        if (StringUtils.isBlank(id)) {
            return 0;
        }
        ItemStack itemBySlot = entity.getItemBySlot(slotType);
        if (itemBySlot.isEmpty() && id.equals(EMPTY_ITEM)) {
            return 1;
        }
        String strSubstring = id.substring(1);
        if (id.startsWith(PREFIX_ITEM_ID)) {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(itemBySlot.getItem());
            if (key == null) {
                return 0;
            }
            return strSubstring.equals(key.toString()) ? 1 : 0;
        }
        if (id.startsWith(PREFIX_ITEM_TAG) && (iTagManagerTags = ForgeRegistries.ITEMS.tags()) != null) {
            return itemBySlot.is(iTagManagerTags.createTagKey(ResourceLocation.parse(strSubstring))) ? 1 : 0;
        }
        return 0;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 2 || size == 3;
    }
}