package com.elfmcys.yesstevemodel.client.compat.immersiveaircraft;

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.loading.LoadingModList;
import org.joml.Vector3f;

import java.util.Optional;

public class ImmersiveAirCraftCompat {

    private static final String MOD_ID = "immersive_aircraft";

    private static boolean IS_LOADED;

    public static void init() {
        try {
            IS_LOADED = LoadingModList.get().getModFileById(MOD_ID) != null;
        } catch (Throwable th) {
        }
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static Optional<Vector3f> getAircraftRotation(AnimationEvent<GeckoVehicleEntity> event) {
        if (IS_LOADED) {
            Entity entity = event.getAnimatable().getEntity();
            if (entity instanceof AircraftEntity aircraftEntity) {
                Vector3f vector3f = aircraftEntity.onGround() ? new Vector3f(0.0f, 0.0f, 0.0f) : aircraftEntity.getWindEffect();
                Vector3f vector3f2 = new Vector3f();
                MathUtil.getEulerAnglesZYX(com.mojang.math.Axis.XP.rotationDegrees(vector3f.z).rotateZ(MathUtil.degreesToRadians(vector3f.x)).rotateX(-MathUtil.degreesToRadians(aircraftEntity.getViewXRot(event.getFrameTime()))).rotateZ(-MathUtil.degreesToRadians(aircraftEntity.getRoll(event.getFrameTime()))), vector3f2);
                return Optional.of(vector3f2);
            }
        }
        return Optional.empty();
    }
}