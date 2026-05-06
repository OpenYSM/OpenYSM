package com.elfmcys.yesstevemodel.capability;

import com.google.common.collect.Sets;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.Iterator;
import java.util.Set;

public class StarModelsCapability {

    private Set<String> starModels = Sets.newHashSet();

    public void addModel(String str) {
        this.starModels.add(str);
    }

    public void copyFrom(StarModelsCapability other) {
        this.starModels = other.starModels;
    }

    public void removeModel(String str) {
        this.starModels.remove(str);
    }

    public boolean containsModel(String str) {
        return this.starModels.contains(str);
    }

    public Set<String> getStarModels() {
        return this.starModels;
    }

    public void setStarModels(Set<String> set) {
        this.starModels = set;
    }

    public void clear() {
        this.starModels.clear();
    }

    public ListTag serializeNBT() {
        ListTag listTag = new ListTag();
        Iterator<String> it = this.starModels.iterator();
        while (it.hasNext()) {
            listTag.add(StringTag.valueOf(it.next()));
        }
        return listTag;
    }

    public void deserializeNBT(ListTag listTag) {
        this.starModels.clear();
        Iterator it = listTag.iterator();
        while (it.hasNext()) {
            this.starModels.add(((Tag) it.next()).getAsString());
        }
    }
}