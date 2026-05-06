package com.elfmcys.yesstevemodel.client.compat.simplehats;

import com.elfmcys.yesstevemodel.client.compat.curios.CuriosBinding;
import fonnymunkey.simplehats.common.item.HatItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

public class HatCuriosGetter {

    private static final String SLOT_HEAD = "head";

    @Nullable
    public static ItemStack getHeadCurio(LivingEntity livingEntity) {
        return CuriosApi.getCuriosInventory(livingEntity).map(curiosItemHandler -> curiosItemHandler).flatMap(curiosItemHandler -> curiosItemHandler.getStacksHandler(SLOT_HEAD)).map(stacksHandler -> CuriosBinding.findInSlot(stacksHandler, itemStack -> itemStack.getItem() instanceof HatItem)).orElse(null);
    }
}