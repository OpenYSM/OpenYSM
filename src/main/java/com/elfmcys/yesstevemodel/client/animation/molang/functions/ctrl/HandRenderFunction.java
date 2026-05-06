package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction;
import com.elfmcys.yesstevemodel.client.animation.condition.InnerClassify;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class HandRenderFunction extends LivingEntityFunction {

    private static final String PREFIX_ITEM_ID = "$";

    private static final String PREFIX_ITEM_TAG = "#";

    private static final String TYPE_PREFIX = ":";

    private static final String EMPTY_ITEM = "empty";

    private static final int RESULT_FALSE = 0;

    private static final int RESULT_TRUE = 1;

    private final HandItemPredicate handItemPredicate;

    private interface HandItemPredicate {
        boolean test(LivingEntity livingEntity, InteractionHand interactionHand);
    }

    private HandRenderFunction(HandItemPredicate predicate) {
        this.handItemPredicate = predicate;
    }

    public static HandRenderFunction createAlways() {
        return new HandRenderFunction((entity, interactionHand) -> true);
    }

    public static HandRenderFunction createWhenSwinging() {
        return new HandRenderFunction((entity, interactionHand) -> entity.swinging && !entity.isSleeping());
    }

    public static HandRenderFunction createWhenUsing() {
        return new HandRenderFunction((entity, interactionHand) -> entity.isUsingItem() && !entity.isSleeping());
    }

    @Override
    public Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        EquipmentSlot slotType = MolangUtils.parseSlotType(context.entity(), arguments.getAsString(context, 0));
        if (slotType == null || slotType.isArmor()) {
            return 0;
        }
        String id = arguments.getAsString(context, 1);
        LivingEntity entity = context.entity().entity();
        if (StringUtils.isBlank(id)) {
            return 0;
        }
        ItemStack itemBySlot = entity.getItemBySlot(slotType);
        if (!this.handItemPredicate.test(entity, slotType == EquipmentSlot.OFFHAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)) {
            return 0;
        }
        if (itemBySlot.isEmpty() && id.equals("empty")) {
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
        if (id.startsWith(PREFIX_ITEM_TAG)) {
            ITagManager<Item> iTagManagerTags = ForgeRegistries.ITEMS.tags();
            if (iTagManagerTags == null) {
                return 0;
            }
            return itemBySlot.is(iTagManagerTags.createTagKey(ResourceLocation.parse(strSubstring))) ? 1 : 0;
        }
        if (id.startsWith(TYPE_PREFIX)) {
            String itemType = InnerClassify.getItemType(itemBySlot);
            if ((!StringUtils.isNotBlank(itemType) || !itemType.equals(strSubstring)) && !itemBySlot.getUseAnimation().name().toLowerCase(Locale.ENGLISH).equals(strSubstring)) {
                return 0;
            }
            return 1;
        }
        return 0;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 2 || size == 3;
    }
}