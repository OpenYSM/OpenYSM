package com.elfmcys.yesstevemodel.client.compat.ironsspellbooks;

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public class SpellbooksCompat {

    private static final String MOD_ID = "irons_spellbooks";

    private static boolean IS_LOADED = false;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static void registerBindings(CtrlBinding binding) {
        if (IS_LOADED) {
            SpellbookBinding.registerBindings(binding);
        } else {
            registerDummyBindings(binding);
        }
    }

    @Nullable
    public static PlayState resolvePlayState(AnimationEvent<LivingAnimatable<?>> event, LivingEntity entity) {
        if (IS_LOADED) {
            return SpellbookBinding.determinePlayState(event, entity);
        }
        return null;
    }

    private static void registerDummyBindings(CtrlBinding binding) {
        binding.clientPlayerEntityVar("iss_animation", ctx -> StringPool.EMPTY);
    }
}