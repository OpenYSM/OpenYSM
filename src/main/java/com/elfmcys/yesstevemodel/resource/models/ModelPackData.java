package com.elfmcys.yesstevemodel.resource.models;

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 读取ysm_pack.json内容
 * https://ysm.cfpa.team/wiki/model-pack/#%E5%88%B6%E4%BD%9C%E6%A8%A1%E5%9E%8B%E5%8C%85
 */
public class ModelPackData {

    private final String path;

    private final String name;

    private final String description;

    @Nullable
    private final OuterFileTexture texture;

    @Nullable
    private final Map<String, Map<String, String>> translations;

    public ModelPackData(String path, String name, String description, @Nullable OuterFileTexture texture, @Nullable Map<String, Map<String, String>> translations) {
        this.path = path;
        this.name = name;
        this.description = description;
        this.texture = texture;
        this.translations = translations;
    }

    @NotNull
    public String getPath() {
        return this.path;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nullable
    public OuterFileTexture getTexture() {
        return this.texture;
    }

    @Nullable
    public Map<String, Map<String, String>> getTranslations() {
        return this.translations;
    }
}
