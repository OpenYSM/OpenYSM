package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.audio.AudioTrackData;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.util.List;
import java.util.Map;

public class ModelResourceBundle {

    private final Map<String, AudioTrackData> soundEffects;

    // setup@player_init.molang即可创建一个名为setup的函数，并订阅player_init事件
    // 这里是函数String是@前面的
    private final Object2ReferenceOpenHashMap<String, IValue> functions;

    // setup@player_init.molang即可创建一个名为setup的函数，并订阅player_init事件
    // 这里是事件@后面的
    // 如果有多个就加入list
    private final Object2ReferenceOpenHashMap<String, List<IValue>> events;

    private final Map<String, Map<String, String>> translations;

    public ModelResourceBundle(Map<String, AudioTrackData> soundEffects, Object2ReferenceOpenHashMap<String, IValue> functions, Object2ReferenceOpenHashMap<String, List<IValue>> events, Map<String, Map<String, String>> translations) {
        this.soundEffects = soundEffects;
        this.functions = functions;
        this.events = events;
        this.translations = translations;
    }

    public Map<String, AudioTrackData> getSoundEffects() {
        return this.soundEffects;
    }

    public Object2ReferenceOpenHashMap<String, IValue> getFunctions() {
        return this.functions;
    }

    public Object2ReferenceOpenHashMap<String, List<IValue>> getEvents() {
        return this.events;
    }

    public Map<String, Map<String, String>> getMetadata() {
        return this.translations;
    }
}