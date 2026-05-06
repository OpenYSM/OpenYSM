package com.elfmcys.yesstevemodel.client.compat.simplehats;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class SimpleHatsHelper {

    private static final String MOD_ID = "simplehats";

    private static boolean IS_LOADED = false;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    @Nullable
    public static ItemStack getHatItem(LivingEntity livingEntity) {
        if (IS_LOADED) {
            return HatCuriosGetter.getHeadCurio(livingEntity);
        }
        return null;
    }
}