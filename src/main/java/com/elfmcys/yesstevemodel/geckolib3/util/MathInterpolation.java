package com.elfmcys.yesstevemodel.geckolib3.util;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class MathInterpolation {
    public static double getYawInterpolation(IContext<Entity> context) {
        Entity entity = context.entity();
        float frameTime = context.animationEvent().getFrameTime();
        Vec3 positionDelta = context.geoInstance().getPositionTracker().getPositionDelta();
        double d = positionDelta.x;
        double d2 = positionDelta.z;
        if (Math.sqrt((d * d) + (d2 * d2)) < 1.0E-4d) {
            return 0.0d;
        }
        return Mth.cos(MathUtil.degreesToRadians(Mth.wrapDegrees(MathUtil.radiansToDegrees((float) Mth.atan2(d2, d)) - (90.0f - Mth.wrapDegrees(-entity.getViewYRot(frameTime))))));
    }

    public static double getPitchInterpolation(IContext<Entity> context) {
        Entity entityMo327xaffeef43 = context.entity();
        float frameTime = context.animationEvent().getFrameTime();
        Vec3 positionDelta = context.geoInstance().getPositionTracker().getPositionDelta();
        double d = positionDelta.x;
        double d2 = positionDelta.z;
        if (Math.sqrt((d * d) + (d2 * d2)) < 1.0E-4d) {
            return 0.0d;
        }
        return Mth.sin(MathUtil.degreesToRadians(Mth.wrapDegrees(MathUtil.radiansToDegrees((float) Mth.atan2(d2, d)) - (90.0f - Mth.wrapDegrees(-entityMo327xaffeef43.getViewYRot(frameTime))))));
    }
}