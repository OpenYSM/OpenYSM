package com.elfmcys.yesstevemodel.client.animation.condition;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class ConditionPassenger {

    private static final String EMPTY = "";

    private final ObjectOpenHashSet<ResourceLocation> idTest = new ObjectOpenHashSet<>();

    private final ReferenceArrayList<TagKey<EntityType<?>>> tagTest = new ReferenceArrayList<>();

    private final String idPre;
    private final String tagPre;

    public ConditionPassenger() {
        this.idPre = "passenger$";
        this.tagPre = "passenger#";
    }

    public void doTest(String name) {
        ITagManager<EntityType<?>> iTagManagerTags;
        int preSize = this.idPre.length();
        if (name.length() <= preSize) {
            return;
        }
        String strSubstring = name.substring(preSize);
        if (name.startsWith(this.idPre) && ResourceLocation.isValidResourceLocation(strSubstring)) {
            this.idTest.add(ResourceLocation.parse(strSubstring));
        }
        if (!name.startsWith(this.tagPre) || !ResourceLocation.isValidResourceLocation(strSubstring) || (iTagManagerTags = ForgeRegistries.ENTITY_TYPES.tags()) == null) {
            return;
        }
        this.tagTest.add(iTagManagerTags.createTagKey(ResourceLocation.parse(strSubstring)));
    }

    public String doTest(LivingEntity entity) {
        Entity firstPassenger = entity.getFirstPassenger();
        if (firstPassenger == null || !firstPassenger.isAlive()) {
            return EMPTY;
        }
        String result = doIdTest(firstPassenger);
        if (result.isEmpty()) {
            return doTagTest(firstPassenger);
        }
        return result;
    }

    private String doIdTest(Entity entity) {
        ResourceLocation key;
        if (!this.idTest.isEmpty() && (key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())) != null && this.idTest.contains(key)) {
            return this.idPre + key;
        }
        return EMPTY;
    }

    private String doTagTest(Entity entity) {
        if (this.tagTest.isEmpty() || ForgeRegistries.ENTITY_TYPES.tags() == null) {
            return EMPTY;
        }
        return this.tagTest.stream().filter(tagKey -> entity.getType().is(tagKey)).findFirst().map(tagKey2 -> this.tagPre + tagKey2.location()).orElse(EMPTY);
    }
}