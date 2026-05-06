package com.elfmcys.yesstevemodel.geckolib3.util;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;

public class MolangUtils {

    private static final HashMap<String, EquipmentSlot> SLOT_MAP = new HashMap<>();

    static {
        SLOT_MAP.put("chest", EquipmentSlot.CHEST);
        SLOT_MAP.put("feet", EquipmentSlot.FEET);
        SLOT_MAP.put("head", EquipmentSlot.HEAD);
        SLOT_MAP.put("legs", EquipmentSlot.LEGS);
        SLOT_MAP.put("mainhand", EquipmentSlot.MAINHAND);
        SLOT_MAP.put("offhand", EquipmentSlot.OFFHAND);
    }

    public static float normalizeTime(long timestamp) {
        return ((float) (timestamp + 6000L) / 24000) % 1;
    }

    @Nullable
    public static BlockState getRelativeBlockState(ExecutionContext<IContext<Entity>> context, Function.ArgumentCollection args) {
        return getRelativeBlockStateAt(context, args, 0);
    }

    @Nullable
    public static BlockState getRelativeBlockStateAt(ExecutionContext<IContext<Entity>> context, Function.ArgumentCollection args, int i) {
        double deltaX = args.getAsDouble(context, i);
        double deltaY = args.getAsDouble(context, i + 1);
        double deltaZ = args.getAsDouble(context, i + 2);
        if (Math.abs(deltaX) > 5.0d || Math.abs(deltaY) > 5.0d || Math.abs(deltaZ) > 5.0d) {
            return null;
        }
        Entity entity = context.entity().entity();
        return entity.level().getBlockState(new BlockPos((int) Math.round((entity.getX() + deltaX) - 0.5d), (int) Math.round((entity.getY() + deltaY) - 0.5d), (int) Math.round((entity.getZ() + deltaZ) - 0.5d)));
    }

    @Nullable
    public static EquipmentSlot parseSlotType(IContext<?> context, String value) {
        if (value == null) {
            return null;
        }
        EquipmentSlot equipmentSlot = SLOT_MAP.get(value.toLowerCase(Locale.ENGLISH));
        if (equipmentSlot == null) {
            context.logWarning("Illegal slot type: %s.", value);
        }
        return equipmentSlot;
    }
}