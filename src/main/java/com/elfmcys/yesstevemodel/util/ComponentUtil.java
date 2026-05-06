package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.StringUtils;

public final class ComponentUtil {
    public static Component getDisplayName(ModelAssembly modelAssembly, String str) {
        MutableComponent mutableComponentLiteral;
        String selectedTexture = modelAssembly.getTextureRegistry().getSelectedTexture();
        if (StringUtils.isBlank(selectedTexture)) {
            mutableComponentLiteral = Component.literal(str);
        } else {
            mutableComponentLiteral = Component.literal(selectedTexture);
        }
        return mutableComponentLiteral;
    }
}