package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.client.compat.cosmeticarmorreworked.CosmeticArmorHelper;
import com.elfmcys.yesstevemodel.util.EquipmentUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConditionArmor {

    private static final Pattern ID_PRE_REG = Pattern.compile("^(.+?)\\$(.*?)$");
    private static final Pattern TAG_PRE_REG = Pattern.compile("^(.+?)#(.*?)$");
    private static final String EMPTY = "";

    private final Reference2ReferenceOpenHashMap<EquipmentSlot, ObjectOpenHashSet<ResourceLocation>> idTest = new Reference2ReferenceOpenHashMap<>();

    private final Reference2ReferenceOpenHashMap<EquipmentSlot, ReferenceArrayList<TagKey<Item>>> tagTest = new Reference2ReferenceOpenHashMap<>();

    public void addTest(String str) {
        EquipmentSlot slot;
        ITagManager<Item> iTagManagerTags;
        Matcher matcher = ID_PRE_REG.matcher(str);
        if (matcher.find()) {
            EquipmentSlot slot2 = getType(matcher.group(1));
            if (slot2 == null) {
                return;
            }
            String strGroup = matcher.group(2);
            if (!ResourceLocation.isValidResourceLocation(strGroup)) {
                return;
            } else {
                this.idTest.computeIfAbsent(slot2, obj -> new ObjectOpenHashSet<>()).add(ResourceLocation.parse(strGroup));
            }
        }
        Matcher matcher2 = TAG_PRE_REG.matcher(str);
        if (!matcher2.find() || (slot = getType(matcher2.group(1))) == null) {
            return;
        }
        String strGroup2 = matcher2.group(2);
        if (!ResourceLocation.isValidResourceLocation(strGroup2) || (iTagManagerTags = ForgeRegistries.ITEMS.tags()) == null) {
            return;
        }
        this.tagTest.computeIfAbsent(slot, obj2 -> new ReferenceArrayList<>()).add(iTagManagerTags.createTagKey(ResourceLocation.parse(strGroup2)));
    }

    public String doTest(LivingEntity entity, EquipmentSlot slot) {
        if (CosmeticArmorHelper.getArmorItem(entity, slot).isEmpty()) {
            return EMPTY;
        }
        String result = doIdTest(entity, slot);
        if (result.isEmpty()) {
            return doTagTest(entity, slot);
        }
        return result;
    }

    private String doIdTest(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        if (this.idTest.isEmpty() || !this.idTest.containsKey(equipmentSlot) || this.idTest.get(equipmentSlot).isEmpty()) {
            return EMPTY;
        }
        Set<ResourceLocation> set = this.idTest.get(equipmentSlot);
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(CosmeticArmorHelper.getArmorItem(livingEntity, equipmentSlot).getItem());
        if (key != null && set.contains(key)) {
            return equipmentSlot.getName() + "$" + key;
        }
        return EMPTY;
    }

    private String doTagTest(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        if (this.tagTest.isEmpty() || !this.tagTest.containsKey(equipmentSlot) || this.tagTest.get(equipmentSlot).isEmpty()) {
            return EMPTY;
        }
        List<TagKey<Item>> list = this.tagTest.get(equipmentSlot);
        ItemStack stack = CosmeticArmorHelper.getArmorItem(livingEntity, equipmentSlot);
        if (ForgeRegistries.ITEMS.tags() == null) {
            return EMPTY;
        }
        Stream<TagKey<Item>> stream = list.stream();
        Objects.requireNonNull(stack);
        return stream.filter(stack::is).findFirst().map(tagKey -> equipmentSlot.getName() + "#" + tagKey.location()).orElse("");
    }

    public boolean hasFilter(EquipmentSlot equipmentSlot) {
        return this.tagTest.containsKey(equipmentSlot) || this.idTest.containsKey(equipmentSlot);
    }

    @Nullable
    public static EquipmentSlot getType(String type) {
        return EquipmentUtil.getEquipmentSlotByName(type).orElse(null);
    }
}