package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.client.compat.oculus.OculusCompat;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class CameraUtil {
    public static int getCameraType(IContext<? extends Entity> IContext) {
        if (IContext.entity() == Minecraft.getInstance().player && ModelPreviewRenderer.isFirstPerson()) {
            return IContext.mc().options.getCameraType().ordinal();
        }
        return CameraType.THIRD_PERSON_FRONT.ordinal();
    }

    public static boolean isFirstPerson(AnimatableEntity<? extends Entity> animatableEntity) {
        return animatableEntity.getEntity() == Minecraft.getInstance().player && ModelPreviewRenderer.isFirstPerson() && !OculusCompat.isPBRActive() && Minecraft.getInstance().options.getCameraType().ordinal() == CameraType.FIRST_PERSON.ordinal();
    }

    public static boolean isThirdPerson(IContext<? extends Entity> IContext) {
        return isThirdPersonModel(IContext.geoInstance());
    }

    public static boolean isThirdPersonModel(AnimatableEntity<?> model) {
        return (model instanceof IPreviewAnimatable) || ModelPreviewRenderer.isPreview();
    }
}