package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.client.animation.molang.TLMBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator;
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.MaidGomokuAI;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidGameRecordManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Function;

public class MaidBinding {
    public static void registerBindings(TLMBinding binding) {
        binding.livingEntityVar("is_begging", createMaidEvaluable((maid) -> maid.isBegging()));
        binding.livingEntityVar("is_sitting", createMaidEvaluable((maid) -> maid.isMaidInSittingPose()));
        binding.livingEntityVar("has_backpack", createMaidEvaluable((v0) -> v0.hasBackpack()));
        binding.livingEntityVar("favorability_point", createMaidEvaluable((maid) -> maid.getFavorability()));
        binding.livingEntityVar("favorability_level", createMaidEvaluable(maid -> maid.getFavorabilityManager().getLevel()));
        binding.livingEntityVar("task_id", createMaidEvaluable(maid -> maid.getTask().getUid()));
        binding.livingEntityVar("schedule", createMaidEvaluable(maid -> maid.getSchedule().name().toLowerCase(Locale.ENGLISH)));
        binding.livingEntityVar("activity", createMaidEvaluable(maid -> maid.getScheduleDetail().getName()));
        binding.livingEntityVar("gomoku_win_count", createMaidEvaluable(maid -> maid.getGameRecordManager().getGomokuWinCount()));
        binding.livingEntityVar("gomoku_rank", createMaidEvaluable(MaidGomokuAI::getRank));
        binding.livingEntityVar("game_statue", createMaidEvaluable(MaidBinding::getMaidTask));
        binding.livingEntityVar("backpack_type", createMaidEvaluable(maid -> maid.getMaidBackpackType().getId().toString()));
        binding.livingEntityVar("is_entity", createMaidEvaluable(maid -> maid.renderState == MaidRenderState.ENTITY));
        binding.livingEntityVar("is_statue", createMaidEvaluable(maid -> maid.renderState == MaidRenderState.STATUE));
        binding.livingEntityVar("is_garage_kit", createMaidEvaluable(maid -> maid.renderState == MaidRenderState.GARAGE_KIT));
        binding.livingEntityVar("show_item", createMaidEvaluable(MaidBinding::getMaidSchedule));
    }

    @NotNull
    private static IValueEvaluator<Object, IContext<LivingEntity>> createMaidEvaluable(Function<EntityMaid, Object> function) {
        return ctx -> {
            EntityMaid maid = (EntityMaid) ctx.entity();
            if (maid instanceof EntityMaid) {
                return function.apply(maid);
            }
            return 0;
        };
    }

    private static String getMaidTask(EntityMaid maid) {
        if (maid.getVehicle() instanceof EntitySit) {
            MaidGameRecordManager gameRecordManager = maid.getGameRecordManager();
            if (gameRecordManager.isWin()) {
                return "win";
            }
            if (gameRecordManager.isLost()) {
                return "lost";
            }
            return StringPool.EMPTY;
        }
        return StringPool.EMPTY;
    }

    private static String getMaidSchedule(EntityMaid entityMaid) {
        ResourceLocation key;
        ItemStack backpackShowItem = entityMaid.getBackpackShowItem();
        if (backpackShowItem.isEmpty() || (key = ForgeRegistries.ITEMS.getKey(backpackShowItem.getItem())) == null) {
            return StringPool.EMPTY;
        }
        return key.toString();
    }
}