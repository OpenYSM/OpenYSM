package com.elfmcys.yesstevemodel.client.texture;

import com.elfmcys.yesstevemodel.client.compat.oculus.ShadersTextureType;
import net.minecraft.client.renderer.texture.AbstractTexture;

import java.util.Map;

public interface ITextureMap {
    Map<ShadersTextureType, ? extends AbstractTexture> getSuffixTextures();
}