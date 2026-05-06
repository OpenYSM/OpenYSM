package com.elfmcys.yesstevemodel.util;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.UseAnim;
import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EquipmentUtil {

    private static final Object2ReferenceOpenHashMap<String, EquipmentSlot> SLOT_BY_NAME = new Object2ReferenceOpenHashMap<>((Map) Arrays.stream(EquipmentSlot.values()).collect(Collectors.toMap(equipmentSlot -> equipmentSlot.getName().toLowerCase(Locale.US), equipmentSlot2 -> equipmentSlot2)));

    public static Optional<UseAnim> getUseAnimByName(String str) {
        return Optional.ofNullable(EnumUtils.getEnum(UseAnim.class, str.toUpperCase(Locale.US)));
    }

    public static Optional<EquipmentSlot> getEquipmentSlotByName(String str) {
        return Optional.ofNullable(SLOT_BY_NAME.get(str));
    }
}