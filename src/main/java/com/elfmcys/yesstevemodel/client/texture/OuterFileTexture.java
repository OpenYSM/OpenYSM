package com.elfmcys.yesstevemodel.client.texture;

import com.elfmcys.yesstevemodel.client.compat.oculus.ShadersTextureType;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public class OuterFileTexture extends AbstractTexture implements ITextureMap {
    private final byte[] data;

    private Map<ShadersTextureType, OuterFileTexture> suffixTextures = Reference2ReferenceMaps.emptyMap();

    public OuterFileTexture(byte[] data) {
        this.data = data;
    }

    @Override
    public void load(@NotNull ResourceManager resourceManager) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(this::doLoad);
        } else {
            doLoad();
        }
    }

    public void doLoad() {
        try {
            NativeImage imageIn = NativeImage.read(new ByteArrayInputStream(data));
            int width = imageIn.getWidth();
            int height = imageIn.getHeight();
            TextureUtil.prepareImage(this.getId(), 0, width, height);
            imageIn.upload(0, 0, 0, 0, 0, width, height, false, false, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSuffixTextures(Map<ShadersTextureType, OuterFileTexture> map) {
        this.suffixTextures = Reference2ReferenceMaps.unmodifiable(new Reference2ReferenceOpenHashMap<>(map));
    }

    public Map<ShadersTextureType, ? extends AbstractTexture> getSuffixTextures() {
        return this.suffixTextures;
    }
}