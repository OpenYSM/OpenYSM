package com.elfmcys.yesstevemodel.geckolib3.core.util;

import net.minecraft.util.Mth;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtil {
    private static final float DEGREES_TO_RADIANS = Mth.DEG_TO_RAD;
    private static final float RADIANS_TO_DEGREES = Mth.RAD_TO_DEG;

    public static final float PI_FROM_DEG = 3.1415927f;

    public static final float TWO_PI = (float) Math.toRadians(360.0d);

    public static final float PI = (float) Math.toRadians(180.0d);

    public static final Vector3f ZERO = new Vector3f(0.0f, 0.0f, 0.0f);

    public static final Vector3f ONE = new Vector3f(1.0f, 1.0f, 1.0f);

    public static Quaternionf eulerZYXToQuaternion(Vector3f angles) {
        return new Quaternionf().rotateZYX(angles.z, angles.y, angles.x);
    }

    public static void nlerpEulerAngles(float percentCompleted, Vector3f startEuler, Vector3f endEuler, Vector3f offsetEuler, Vector3f outEuler) {
        Vector3f tempEuler = new Vector3f(startEuler).add(offsetEuler);
        Quaternionf startQuat = eulerZYXToQuaternion(tempEuler);

        endEuler.add(offsetEuler, tempEuler);
        Quaternionf endQuat = eulerZYXToQuaternion(tempEuler);

        startQuat.nlerp(endQuat, percentCompleted, endQuat);

        getEulerAnglesZYX(endQuat, tempEuler);

        tempEuler.sub(offsetEuler, outEuler);
    }

    public static Vector3f getEulerAnglesZYX(Quaternionf quaternionf, Vector3f eulerAngles) {
        eulerAngles.x = Math.atan2((quaternionf.y * quaternionf.z) + (quaternionf.w * quaternionf.x), (0.5f - (quaternionf.x * quaternionf.x)) - (quaternionf.y * quaternionf.y));
        eulerAngles.y = Math.safeAsin((-2.0f) * ((quaternionf.x * quaternionf.z) - (quaternionf.w * quaternionf.y)));
        eulerAngles.z = Math.atan2((quaternionf.x * quaternionf.y) + (quaternionf.w * quaternionf.z), (0.5f - (quaternionf.y * quaternionf.y)) - (quaternionf.z * quaternionf.z));
        return eulerAngles;
    }

    public static Vector3f lerpValues(float percentCompleted, Vector3f begin, Vector3f end) {
        return new Vector3f(lerpValues(percentCompleted, begin.x(), end.x()),
                lerpValues(percentCompleted, begin.y(), end.y()),
                lerpValues(percentCompleted, begin.z(), end.z()));
    }

    public static void lerpValues(float percentCompleted, Vector3f begin, Vector3f end, Vector3f outResult) {
        outResult.set(lerpValues(percentCompleted, begin.x(), end.x()),
                lerpValues(percentCompleted, begin.y(), end.y()),
                lerpValues(percentCompleted, begin.z(), end.z()));
    }

    public static float lerpValues(float percentCompleted, float startValue, float endValue) {
        return startValue + percentCompleted * (endValue - startValue);
    }

    public static Vector3f catmullRom(float percentCompleted, Vector3f left, Vector3f begin, Vector3f end, Vector3f right) {
        return new Vector3f(catmullRom(percentCompleted, left.x(), begin.x(), end.x(), right.x()),
                catmullRom(percentCompleted, left.y(), begin.y(), end.y(), right.y()),
                catmullRom(percentCompleted, left.z(), begin.z(), end.z(), right.z()));
    }

    public static float catmullRom(float percent, float left, float begin, float end, float right) {
        float v0 = (end - left) * 0.5f;
        float v1 = (right - begin) * 0.5f;
        float t2 = percent * percent;
        float t3 = percent * t2;
        return (2 * begin - 2 * end + v0 + v1) * t3 + (-3 * begin + 3 * end - 2 * v0 - v1) * t2 + v0 * percent + begin;
    }

    public static float degreesToRadians(float degrees) {
        return degrees * DEGREES_TO_RADIANS;
    }

    public static float radiansToDegrees(float degrees) {
        return degrees * RADIANS_TO_DEGREES;
    }

    public static void normalizeAnglesInPlace(Vector3f source, Vector3f outResult) {
        outResult.set(normalizeAngle(source.x), normalizeAngle(source.y), normalizeAngle(source.z));
    }

    public static Vector3f normalizeAngles(Vector3f angles) {
        return new Vector3f(normalizeAngle(angles.x), normalizeAngle(angles.y), normalizeAngle(angles.z));
    }

    public static float normalizeAngle(float angle) {
        float f2 = angle % TWO_PI;
        if (f2 >= PI) {
            f2 -= TWO_PI;
        }
        if (f2 < (-PI)) {
            f2 += TWO_PI;
        }
        return f2;
    }

    public static Vector3f lerpAngles(Vector3f targetAngles, float t) {
        return new Vector3f(lerpAngle(targetAngles.x, t), lerpAngle(targetAngles.y, t), lerpAngle(targetAngles.z, t));
    }

    public static void lerpAnglesInPlace(Vector3f targetAngles, float t, Vector3f outResult) {
        outResult.set(lerpAngle(targetAngles.x, t), lerpAngle(targetAngles.y, t), lerpAngle(targetAngles.z, t));
    }

    public static float lerpAngle(float target, float t) {
        return 1.0f + ((target - 1.0f) * t);
    }
}
