package com.elfmcys.yesstevemodel.geckolib3.file;

import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.util.Map;

public class AnimationFile {
    private final Map<String, Animation> animations;

    public AnimationFile(Map<String, Animation> map) {
        this.animations = Object2ReferenceMaps.unmodifiable(new Object2ReferenceOpenHashMap<>(map));
    }

    public Map<String, Animation> getAnimations() {
        return this.animations;
    }
}