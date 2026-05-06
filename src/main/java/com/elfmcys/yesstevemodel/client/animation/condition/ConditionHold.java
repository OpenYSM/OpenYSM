package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.EquipmentUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
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

public class ConditionHold {

    private static final String EMPTY_MAINHAND = "hold_mainhand:empty";

    private static final String EMPTY_OFFHAND = "hold_offhand:empty";

    private static final String EMPTY = "";

    private final int preSize;

    private final String idPre;

    private final String tagPre;

    private final String extraPre;

    private final ObjectOpenHashSet<ResourceLocation> idTest = new ObjectOpenHashSet<>();

    private final ReferenceArrayList<TagKey<Item>> tagTest = new ReferenceArrayList<>();

    private final ReferenceOpenHashSet<UseAnim> extraTes = new ReferenceOpenHashSet<>();

    private final ObjectOpenHashSet<String> innerTest = new ObjectOpenHashSet<>();

    public ConditionHold(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.idPre = "hold_mainhand$";
            this.tagPre = "hold_mainhand#";
            this.extraPre = "hold_mainhand:";
            this.preSize = 14;
        } else {
            this.idPre = "hold_offhand$";
            this.tagPre = "hold_offhand#";
            this.extraPre = "hold_offhand:";
            this.preSize = 13;
        }
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
            return hand == InteractionHand.MAIN_HAND ? EMPTY_MAINHAND : EMPTY_OFFHAND;
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
        return stream.filter(itemInHand::is).findFirst().map(tagKey -> this.tagPre + tagKey.location()).orElse("");
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