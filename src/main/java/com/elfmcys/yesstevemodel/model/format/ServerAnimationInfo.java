package com.elfmcys.yesstevemodel.model.format;

import it.unimi.dsi.fastutil.objects.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerAnimationInfo {

    // 动画的文件前缀 + 里面所有的动画名字
    private final Map<String, Set<String>> animations;

    // 模型的材质名字
    private final List<String> textures;

    public ServerAnimationInfo(Map<String, String[]> animations, String[] textures) {
        this.animations = Object2ObjectMaps.unmodifiable(new Object2ObjectOpenHashMap<>(animations.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ObjectSets.unmodifiable(ObjectOpenHashSet.of(entry.getValue()))))));
        this.textures = ObjectLists.unmodifiable(ObjectArrayList.of(textures));
    }

    public Map<String, Set<String>> getAnimations() {
        return this.animations;
    }

    public List<String> getTextures() {
        return this.textures;
    }
}