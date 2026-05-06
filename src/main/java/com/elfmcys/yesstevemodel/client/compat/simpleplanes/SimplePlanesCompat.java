package com.elfmcys.yesstevemodel.client.compat.simpleplanes;

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import net.minecraftforge.fml.loading.LoadingModList;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.przemyk.simpleplanes.entities.PlaneEntity;

import java.util.Optional;

public class SimplePlanesCompat {

    private static final String MOD_ID = "simpleplanes";

    private static boolean IS_LOADED = false;

    public static void init() {
        try {
            IS_LOADED = LoadingModList.get().getModFileById(MOD_ID) != null;
        } catch (Throwable th) {
        }
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static Optional<Vector3f> getSimplePlanesRotation(AnimationEvent<GeckoVehicleEntity> event) {
        if (IS_LOADED) {
            Object entity = event.getAnimatable().getEntity();
            if (entity instanceof PlaneEntity planeEntity) {
                Quaternionf quaternionfLerpQ = xyz.przemyk.simpleplanes.misc.MathUtil.lerpQ(event.getFrameTime(), planeEntity.getQ_Prev(), planeEntity.getQ_Client());
                quaternionfLerpQ.premul(com.mojang.math.Axis.YP.rotation(-MathUtil.degreesToRadians(planeEntity.getViewYRot(event.getFrameTime()))));
                float timeSinceHit = planeEntity.getTimeSinceHit() - event.getFrameTime();
                if (timeSinceHit > 0.0f) {
                    quaternionfLerpQ.rotateZ(Math.sin(planeEntity.tickCount + event.getFrameTime()) * Math.clamp(timeSinceHit / 10.0f, -30.0f, 30.0f));
                }
                Vector3f vector3f = new Vector3f();
                MathUtil.getEulerAnglesZYX(quaternionfLerpQ, vector3f);
                vector3f.x = -vector3f.x;
                return Optional.of(vector3f);
            }
        }
        return Optional.empty();
    }
}