package com.elfmcys.yesstevemodel.client.compat.curios;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.curios.HasAnyCuriosWithAllTags;
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.curios.HasAnyCurios;
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.curios.HasAnyCuriosWithAnyTag;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.function.Predicate;

public class CuriosBinding {
    @Nullable
    public static ItemStack findFirstMatching(ICuriosItemHandler handler, Predicate<ItemStack> predicate) {
        for (ICurioStacksHandler iCurioStacksHandler : handler.getCurios().values()) {
            ItemStack found = findInSlot(iCurioStacksHandler, predicate);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Nullable
    public static ItemStack findInSlot(ICurioStacksHandler handler, Predicate<ItemStack> predicate) {
        ItemStack stackInSlot;
        NonNullList<Boolean> renders = handler.getRenders();
        IDynamicStackHandler cosmeticStacks = handler.hasCosmetic() ? handler.getCosmeticStacks() : null;
        IDynamicStackHandler stacks = handler.getStacks();
        for (int i = 0; i < stacks.getSlots() && i < renders.size(); i++) {
            if (renders.get(i)) {
                if (cosmeticStacks != null && (stackInSlot = cosmeticStacks.getStackInSlot(i)) != null && !stackInSlot.isEmpty()) {
                    if (predicate.test(stackInSlot)) {
                        return stackInSlot;
                    }
                } else {
                    ItemStack stackInSlot2 = stacks.getStackInSlot(i);
                    if (!stackInSlot2.isEmpty() && predicate.test(stackInSlot2)) {
                        return stackInSlot2;
                    }
                }
            }
        }
        return null;
    }

    public static void registerBindings(ContextBinding binding) {
        binding.function("has_any_curios", new HasAnyCurios());
        binding.function("has_any_curios_with_all_tags", new HasAnyCuriosWithAllTags());
        binding.function("has_any_curios_with_any_tag", new HasAnyCuriosWithAnyTag());
        binding.livingEntityVar("dump_curios", CuriosBinding::dumpCurios);
    }

    private static Object dumpCurios(IContext<? extends LivingEntity> context) {
        if (!context.isDebugMode()) {
            return null;
        }
        CuriosApi.getCuriosInventory(context.entity()).ifPresent(handler -> {
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                context.logWarningComponent(Component.literal("-------- Type ").append(ComponentUtils.copyOnClickText(entry.getKey())).append(" --------"));
                context.logWarning(StringPool.EMPTY);
                findInSlot(entry.getValue(), stack -> {
                    context.logWarningComponent(Component.literal("Display ").append(ComponentUtils.copyOnClickText(stack.getHoverName().getString(99))));
                    Holder<Item> itemHolder = stack.getItemHolder();
                    itemHolder.unwrapKey().ifPresent(resourceKey -> {
                        context.logWarningComponent(Component.literal("Name ").append(ComponentUtils.copyOnClickText(resourceKey.location().toString())));
                    });
                    itemHolder.tags().forEach(tagKey -> {
                        context.logWarningComponent(Component.literal("Tag ").append(ComponentUtils.copyOnClickText(tagKey.location().toString())));
                    });
                    context.logWarning(StringPool.EMPTY);
                    return false;
                });
            }
        });
        return null;
    }
}