package com.elfmcys.yesstevemodel.client.compat.swem;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class SWEMCompat {

    private static final String MOD_ID = "swem";

    private static boolean IS_LOADED;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    @Nullable
    public static String getHorseGaitName(LivingEntity livingEntity) {
        if (isLoaded()) {
            return SWEMHorseUtils.getGaitName(livingEntity);
        }
        return null;
    }

    public static void registerControllerFunctions(CtrlBinding ctrlBinding) {
        if (isLoaded()) {
            SwemBinding.registerBindings(ctrlBinding);
        } else {
            registerSWEMFunctions(ctrlBinding);
        }
    }

    private static void registerSWEMFunctions(CtrlBinding ctrlBinding) {
        ctrlBinding.livingEntityVar("swem_is_ride", it -> {
            return false;
        });
        ctrlBinding.livingEntityVar("swem_state", it2 -> {
            return StringPool.EMPTY;
        });
    }
}