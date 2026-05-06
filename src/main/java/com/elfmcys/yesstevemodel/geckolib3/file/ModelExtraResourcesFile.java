package com.elfmcys.yesstevemodel.geckolib3.file;

import com.elfmcys.yesstevemodel.audio.AudioTrackData;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;

import java.util.Map;

public class ModelExtraResourcesFile {

    // sounds文件夹
    private final Map<String, AudioTrackData> audioTracks;

    // functions文件夹 注意parse molang记得打上true去注释
    private final Map<String, IValue> functions;

    // lang文件夹
    private final Map<String, Map<String, String>> translations;

    public ModelExtraResourcesFile(Map<String, AudioTrackData> audioTracks, Map<String, IValue> functions, Map<String, Map<String, String>> translations) {
        this.audioTracks = audioTracks;
        this.functions = functions;
        this.translations = translations;
    }

    public Map<String, AudioTrackData> getAudioTracks() {
        return this.audioTracks;
    }

    public Map<String, IValue> getFunctions() {
        return this.functions;
    }

    public Map<String, Map<String, String>> getTranslations() {
        return this.translations;
    }
}
