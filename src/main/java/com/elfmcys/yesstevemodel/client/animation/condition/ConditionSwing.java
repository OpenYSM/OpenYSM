package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.EquipmentUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ConditionSwing {

    private static final String EMPTY = "";

    private final int preSize;

    private final String idPre;

    private final String tagPre;

    private final String extraPre;

    private final ObjectOpenHashSet<ResourceLocation> idTest = new ObjectOpenHashSet<>();

    private final ReferenceArrayList<TagKey<Item>> tagTest = new ReferenceArrayList<>();

    private final ObjectOpenHashSet<UseAnim> extraTes = new ObjectOpenHashSet<>();

    private final ObjectOpenHashSet<String> innerTest = new ObjectOpenHashSet<>();

    public ConditionSwing(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.idPre = "swing$";
            this.tagPre = "swing#";
            this.extraPre = "swing:";
            this.preSize = 6;
            return;
        }
        this.idPre = "swing_offhand$";
        this.tagPre = "swing_offhand#";
        this.extraPre = "swing_offhand:";
        this.preSize = 14;
    }

    public void addTest(String name) {
        if (name.length() <= this.preSize) {
            return;
        }
        String strSubstring = name.substring(this.preSize);
        if (name.startsWith(this.idPre) && ResourceLocation.isValidResourceLocation(strSubstring)) {
            this.idTest.add(ResourceLocation.parse(strSubstring));
        }
        if (name.startsWith(this.tagPre) && ResourceLocation.isValidResourceLocation(strSubstring)) {
            ITagManager<Item> iTagManagerTags = ForgeRegistries.ITEMS.tags();
            if (iTagManagerTags == null) {
                return;
            }
            this.tagTest.add(iTagManagerTags.createTagKey(ResourceLocation.parse(strSubstring)));
        }
        if (!name.startsWith(this.extraPre) || strSubstring.equals(UseAnim.NONE.name().toLowerCase(Locale.US))) {
            return;
        }
        Optional<UseAnim> optional = EquipmentUtil.getUseAnimByName(strSubstring);
        Objects.requireNonNull(this.extraTes);
        optional.ifPresent(extraTes::add);
        this.innerTest.add(name);
    }

    public String doTest(LivingEntity entity, InteractionHand hand) {
        if (entity.getItemInHand(hand).isEmpty()) {
            return EMPTY;
        }
        String result = doIdTest(entity, hand);
        if (result.isEmpty()) {
            result = doTagTest(entity, hand);
            if (result.isEmpty()) {
                return doExtraTest(entity, hand);
            }
            return result;
        }
        return result;
    }

    private String doIdTest(LivingEntity livingEntity, InteractionHand interactionHand) {
        if (this.idTest.isEmpty()) {
            return EMPTY;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(livingEntity.getItemInHand(interactionHand).getItem());
        if (key != null && this.idTest.contains(key)) {
            return this.idPre + key;
        }
        return EMPTY;
    }

    private String doTagTest(LivingEntity livingEntity, InteractionHand interactionHand) {
        if (this.tagTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = livingEntity.getItemInHand(interactionHand);
        if (ForgeRegistries.ITEMS.tags() == null) {
            return EMPTY;
        }
        Stream<TagKey<Item>> stream = this.tagTest.stream();
        Objects.requireNonNull(itemInHand);
        return stream.filter(itemInHand::is).findFirst().map(tagKey -> this.tagPre + tagKey.location()).orElse(EMPTY);
    }

    private String doExtraTest(LivingEntity entity, InteractionHand hand) {
        if (this.extraTes.isEmpty() && this.innerTest.isEmpty()) {
            return EMPTY;
        }
        String innerName = InnerClassify.doClassifyTest(this.extraPre, entity, hand);
        if (StringUtils.isNotBlank(innerName) && this.innerTest.contains(innerName)) {
            return innerName;
        }
        UseAnim anim = entity.getItemInHand(hand).getUseAnimation();
        if (this.extraTes.contains(anim)) {
            return this.extraPre + anim.name().toLowerCase(Locale.US);
        }
        return EMPTY;
    }
}
