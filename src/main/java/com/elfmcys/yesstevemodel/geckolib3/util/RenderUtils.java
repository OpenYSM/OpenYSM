package com.elfmcys.yesstevemodel.geckolib3.util;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

public final class RenderUtils {
    public static void translateMatrixToBone(PoseStack poseStack, IBone bone) {
        poseStack.translate((-bone.getPositionX()) / 16f, bone.getPositionY() / 16f, bone.getPositionZ() / 16f);
    }

    public static void rotateMatrixAroundBone(PoseStack poseStack, IBone bone) {
        if (bone.getRotationZ() != 0.0F || bone.getRotationY() != 0.0F || bone.getRotationX() != 0.0F) {
            poseStack.mulPose(new Quaternionf().rotateZYX(bone.getRotationZ(), bone.getRotationY(), bone.getRotationX()));
        }
    }

    public static boolean scaleMatrixForBone(PoseStack poseStack, IBone bone) {
        float scaleX = bone.getScaleX();
        float scaleY = bone.getScaleY();
        float scaleZ = bone.getScaleZ();
        poseStack.scale(scaleX, scaleY, scaleZ);
        return scaleX == 0 && scaleY == 0 && scaleZ == 0;
    }

    public static void translateToPivotPoint(PoseStack poseStack, IBone bone) {
        poseStack.translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f);
    }

    public static void translateAwayFromPivotPoint(PoseStack poseStack, IBone bone) {
        poseStack.translate(-bone.getPivotX() / 16f, -bone.getPivotY() / 16f, -bone.getPivotZ() / 16f);
    }

    public static void translateAndRotateMatrixForBone(PoseStack poseStack, IBone bone) {
        translateToPivotPoint(poseStack, bone);
        rotateMatrixAroundBone(poseStack, bone);
    }

    public static boolean prepMatrixForBone(PoseStack poseStack, IBone bone) {
        translateMatrixToBone(poseStack, bone);
        translateToPivotPoint(poseStack, bone);
        rotateMatrixAroundBone(poseStack, bone);
        boolean scaleMatrixForBone = scaleMatrixForBone(poseStack, bone);
        translateAwayFromPivotPoint(poseStack, bone);
        return scaleMatrixForBone;
    }

    public static boolean prepMatrixForLocator(PoseStack poseStack, List<? extends IBone> locatorHierarchy) {
        boolean scaleCheck = false;
        for (int i = 0; i < locatorHierarchy.size() - 1; i++) {
            boolean result = RenderUtils.prepMatrixForBone(poseStack, locatorHierarchy.get(i));
            if (result) {
                scaleCheck = true;
            }
        }
        IBone lastBone = locatorHierarchy.get(locatorHierarchy.size() - 1);
        RenderUtils.translateMatrixToBone(poseStack, lastBone);
        RenderUtils.translateToPivotPoint(poseStack, lastBone);
        RenderUtils.rotateMatrixAroundBone(poseStack, lastBone);
        RenderUtils.scaleMatrixForBone(poseStack, lastBone);
        return scaleCheck;
    }

    public static Matrix4f invertAndMultiplyMatrices(Matrix4f baseMatrix, Matrix4f inputMatrix) {
        inputMatrix = new Matrix4f(inputMatrix);
        inputMatrix.invert();
        inputMatrix.mul(baseMatrix);
        return inputMatrix;
    }

    public static Matrix4f[] updateMatrices(List<GeoBone> bones, float[] boneParams, Matrix4f rootPose) {
        int boneCount = bones.size();
        Matrix4f[] poses = new Matrix4f[boneCount];

        for (int i = 0; i < boneCount; i++) {
            GeoBone bone = bones.get(i);

            Matrix4f currentMatrix = new Matrix4f(bone.parentIdx == -1 ? rootPose : poses[bone.parentIdx]);

            poses[i] = prepMatrixForBone(bones.get(i), currentMatrix, boneParams, i * 12);
        }
        return poses;
    }

    public static Matrix4f prepMatrixForBone(GeoBone bone, Matrix4f pose, float[] boneParams, int boneIdx) {
        final float rotX = boneParams[boneIdx], rotY = boneParams[boneIdx + 1], rotZ = boneParams[boneIdx + 2];
        final float posX = boneParams[boneIdx + 3], posY = boneParams[boneIdx + 4], posZ = boneParams[boneIdx + 5];
        final float scaleX = boneParams[boneIdx + 6], scaleY = boneParams[boneIdx + 7], scaleZ = boneParams[boneIdx + 8];
        final float pivotX = bone.getPivotX(), pivotY = bone.getPivotY(), pivotZ = bone.getPivotZ();

        pose.translate(-posX / 16f, posY / 16f, posZ / 16f);
        pose.translate(pivotX / 16f, pivotY / 16f, pivotZ / 16f);
        if (rotZ != 0.0F) pose.rotateZ(rotZ);
        if (rotY != 0.0F) pose.rotateY(rotY);
        if (rotX != 0.0F) pose.rotateX(rotX);
        if (scaleX != 1.0f || scaleY != 1.0f || scaleZ != 1.0f) {
            pose.scale(scaleX, scaleY, scaleZ);
        }
        pose.translate(-pivotX / 16f, -pivotY / 16f, -pivotZ / 16f);
        return pose;
    }
}