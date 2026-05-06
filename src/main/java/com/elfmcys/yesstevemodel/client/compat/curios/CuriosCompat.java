package com.elfmcys.yesstevemodel.client.compat.curios;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.LoadingModList;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

public class CuriosCompat {

    private static final String MOD_ID = "curios";

    private static boolean IS_LOADED;

    public static void init() {
        IS_LOADED = LoadingModList.get().getModFileById(MOD_ID) != null;
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static boolean hasItemInSlot(LivingEntity livingEntity, String str, ReferenceOpenHashSet<Item> set) {
        return IS_LOADED && CuriosApi.getCuriosInventory(livingEntity).map(handler -> handler).flatMap(handler -> handler.getStacksHandler(str)).map(handler -> CuriosBinding.findInSlot(handler, itemStack -> set.isEmpty() || set.contains(itemStack.getItem())) != null).orElse(false);
    }

    public static boolean hasTaggedItemInSlot(LivingEntity livingEntity, String str, List<TagKey<Item>> list) {
        return IS_LOADED && CuriosApi.getCuriosInventory(livingEntity).map(handler -> handler).flatMap(handler -> handler.getStacksHandler(str)).map(handler -> CuriosBinding.findInSlot(handler, itemStack -> {
            for (TagKey<Item> itemTagKey : list) {
                if (itemStack.is(itemTagKey)) {
                    return true;
                }
            }
            return false;
        }) != null).orElse(false);
    }

    public static boolean hasNoTaggedItemInSlot(LivingEntity entity, String str, List<TagKey<Item>> list) {
        return IS_LOADED && CuriosApi.getCuriosInventory(entity).map(handler -> handler).flatMap(handler -> handler.getStacksHandler(str)).map(handler -> CuriosBinding.findInSlot(handler, itemStack -> {
            for (TagKey<Item> itemTagKey : list) {
                if (!itemStack.is(itemTagKey)) {
                    return false;
                }
            }
            return true;
        }) != null).orElse(false);
    }

    public static void registerCuriosItems(ContextBinding binding) {
        if (IS_LOADED) {
            CuriosBinding.registerBindings(binding);
        } else {
            registerDummyBindings(binding);
        }
    }

    private static void registerDummyBindings(ContextBinding binding) {
        binding.function("has_any_curios", Function.NOOP);
        binding.function("has_any_curios_with_all_tags", Function.NOOP);
        binding.function("has_any_curios_with_any_tag", Function.NOOP);
        binding.livingEntityVar("dump_curios", context -> {
            context.logWarning("Curios not installed.");
            return null;
        });
    }
}