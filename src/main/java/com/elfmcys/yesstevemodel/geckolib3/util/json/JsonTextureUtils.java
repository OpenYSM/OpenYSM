package com.elfmcys.yesstevemodel.geckolib3.util.json;

import com.elfmcys.yesstevemodel.client.compat.oculus.ShadersTextureType;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * error.yes_steve_model.decode_texture
 * 加载失败字段
 */
public class JsonTextureUtils {
    /**
     * 给player用的
     */
    public static OrderedStringMap<String, OuterFileTexture> getTextures(Map<String, byte[]> resource, JsonElement element) {
        if (element == null) return null;

        if (!element.isJsonArray()) return null;

        List<String> keys = Lists.newArrayList();
        List<OuterFileTexture> values = Lists.newArrayList();

        for (JsonElement rawTexture : element.getAsJsonArray()) {
            Pair<String, OuterFileTexture> texture = getTexture(resource, rawTexture);

            if (texture == null) continue;

            keys.add(texture.getKey());
            values.add(texture.getValue());
        }

        return new OrderedStringMap<>(keys.toArray(new String[0]), values.toArray(new OuterFileTexture[0]));
    }

    public static Pair<String, OuterFileTexture> getTexture(Map<String, byte[]> resource, @Nullable JsonElement element) {
        if (element == null) return null;

        if (element.isJsonObject()) {
            JsonObject jsonObj = element.getAsJsonObject();

            if (jsonObj.has("uv")) {
                String uvPath = jsonObj.get("uv").getAsString();
                String name = extractTextureName(uvPath);
                OuterFileTexture texture = new OuterFileTexture(resource.get(uvPath));
                Map<ShadersTextureType, OuterFileTexture> fbo = new HashMap<>();
                if (jsonObj.has("normal")) {
                    String normalPath = jsonObj.get("normal").getAsString();
                    fbo.put(ShadersTextureType.NORMAL, new OuterFileTexture(resource.get(normalPath)));
                }
                if (jsonObj.has("specular")) {
                    String specularPath = jsonObj.get("specular").getAsString();
                    fbo.put(ShadersTextureType.SPECULAR, new OuterFileTexture(resource.get(specularPath)));
                }
                texture.setSuffixTextures(fbo);

                return Pair.of(name, texture);
            }
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String texPath = element.getAsString();
            String name = extractTextureName(texPath);
            return Pair.of(name, new OuterFileTexture(resource.get(texPath)));
        }

        return null;
    }

    private static String extractTextureName(String path) {
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastDot <= lastSlash || lastDot == -1) {
            // no extension found, use the whole filename
            return path.substring(lastSlash + 1);
        }
        return path.substring(lastSlash + 1, lastDot);
    }
}
