package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.NativeLibLoader;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.compat.oculus.OculusCompat;
import com.elfmcys.yesstevemodel.client.compat.optifine.OptiFineDetector;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Locale;

public class NativeModelRenderer {
    private static final Matrix4f projectionModelViewMatrix = new Matrix4f();
    private static boolean nativeRendererDisabled = false;
    private static final NativeMode nativeMode = NativeMode.parse(System.getProperty("ysm.nativeRenderer", "auto"));
    private static final boolean nativeStats = Boolean.getBoolean("ysm.nativeRenderer.stats");
    private static final long nativeStatsIntervalNanos = Long.getLong("ysm.nativeRenderer.statsIntervalSec", 5L) * 1_000_000_000L;
    private static final boolean nativeDebug = Boolean.getBoolean("ysm.nativeRenderer.debug");
    private static long nativeCalls;
    private static long javaCalls;
    private static long nativeNanos;
    private static long javaNanos;
    private static long nativeFallbacks;
    private static long skippedNoLib;
    private static long skippedDisabled;
    private static long skippedNoCache;
    private static long lastStatsLogNanos = System.nanoTime();

    private enum NativeMode {
        AUTO,
        FORCE,
        OFF;

        private static NativeMode parse(String value) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "force", "on", "native" -> FORCE;
                case "off", "java", "false" -> OFF;
                default -> AUTO;
            };
        }
    }

    static {
        if (nativeDebug || nativeStats || nativeMode != NativeMode.AUTO) {
            YesSteveModel.LOGGER.info(
                    "[YSM native renderer] configured mode={}, stats={}, debug={}, statsIntervalSec={}",
                    nativeMode,
                    nativeStats,
                    nativeDebug,
                    nativeStatsIntervalNanos / 1_000_000_000L
            );
        }
    }

    public static void renderMesh(VertexConsumer buffer, PoseStack.Pose pose, GeoModel model, float[] boneParams, float[] stateBuffer, int textureIndex, int renderPartMask, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        OculusCompat.updatePBRState();
        boolean isCompatMode = OptiFineDetector.isOptifinePresent() || GeneralConfig.USE_COMPATIBILITY_RENDERER.get();
        RenderSystem.getProjectionMatrix().mul(RenderSystem.getModelViewMatrix(), projectionModelViewMatrix);
        boolean isPreview = ModelPreviewRenderer.isPreview() || ModelPreviewRenderer.isExtraPlayer();

        boolean hasNativeLib = NativeLibLoader.isLoaded();
        boolean hasNativeCache = model.nativeModelHandle != 0 && model.vertexOutBuffer != null;

        if (nativeMode != NativeMode.OFF && !nativeRendererDisabled && hasNativeLib && hasNativeCache) {
            try {
                long startNanos = nativeStats ? System.nanoTime() : 0L;
                nativeRenderModel(
                        buffer,
                        pose,
                        projectionModelViewMatrix,
                        isCompatMode,
                        model,
                        boneParams,
                        stateBuffer,
                        textureIndex,
                        renderPartMask,
                        packedLight,
                        packedOverlay,
                        red, green, blue, alpha,
                        isPreview
                );
                recordNativeRender(startNanos);
                return;
            } catch (Throwable throwable) {
                nativeFallbacks++;
                YesSteveModel.LOGGER.error("Native YSM renderer failed", throwable);
                if (nativeMode == NativeMode.FORCE) {
                    throw new RuntimeException("Forced native YSM renderer failed", throwable);
                }
                nativeRendererDisabled = true;
            }
        }

        recordNativeSkip(hasNativeLib, hasNativeCache);
        if (nativeMode == NativeMode.FORCE) {
            throw new IllegalStateException("Forced native YSM renderer is unavailable: loaded=" + hasNativeLib
                    + ", disabled=" + nativeRendererDisabled
                    + ", handle=" + model.nativeModelHandle
                    + ", outBuffer=" + (model.vertexOutBuffer != null)
                    + ", loaderError=" + NativeLibLoader.getErrorMessage());
        }

        long startNanos = nativeStats ? System.nanoTime() : 0L;
        renderModel(
                buffer,
                pose,
                projectionModelViewMatrix,
                isCompatMode,
                model,
                boneParams,
                stateBuffer,
                textureIndex,
                renderPartMask,
                packedLight,
                packedOverlay,
                red, green, blue, alpha,
                isPreview
        );
        recordJavaRender(startNanos);
    }

    private static void recordNativeSkip(boolean hasNativeLib, boolean hasNativeCache) {
        if (!nativeStats && !nativeDebug) return;
        if (nativeMode == NativeMode.OFF) {
            skippedDisabled++;
        } else if (nativeRendererDisabled) {
            skippedDisabled++;
        } else if (!hasNativeLib) {
            skippedNoLib++;
        } else if (!hasNativeCache) {
            skippedNoCache++;
        }
        logStatsIfNeeded();
    }

    private static void recordNativeRender(long startNanos) {
        if (!nativeStats) return;
        nativeCalls++;
        nativeNanos += System.nanoTime() - startNanos;
        logStatsIfNeeded();
    }

    private static void recordJavaRender(long startNanos) {
        if (!nativeStats) return;
        javaCalls++;
        javaNanos += System.nanoTime() - startNanos;
        logStatsIfNeeded();
    }

    private static void logStatsIfNeeded() {
        long now = System.nanoTime();
        if (now - lastStatsLogNanos < nativeStatsIntervalNanos) return;
        lastStatsLogNanos = now;
        double nativeAvgUs = nativeCalls == 0 ? 0.0 : nativeNanos / (nativeCalls * 1000.0);
        double javaAvgUs = javaCalls == 0 ? 0.0 : javaNanos / (javaCalls * 1000.0);
        YesSteveModel.LOGGER.info(
                "[YSM native renderer] mode={}, nativeCalls={}, javaCalls={}, fallbacks={}, skippedNoLib={}, skippedDisabled={}, skippedNoCache={}, nativeAvgUs={}, javaAvgUs={}",
                nativeMode,
                nativeCalls,
                javaCalls,
                nativeFallbacks,
                skippedNoLib,
                skippedDisabled,
                skippedNoCache,
                String.format(Locale.ROOT, "%.3f", nativeAvgUs),
                String.format(Locale.ROOT, "%.3f", javaAvgUs)
        );
    }

    public static void renderModel(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            Matrix4f projectionModelViewMatrix,
            boolean isCompatMode,
            GeoModel mesh,
            float[] boneParams,
            float[] stateBuffer,
            int textureIndex, int renderPartMask,
            int packedLight, int packedOverlay,
            float r, float g, float b, float a,
            boolean isPreview) {

        if (mesh.bakedBones == null || mesh.bakedBones.isEmpty()) return;

        // TODO: 修復GC壓力
        Matrix4f rootPoseMat = pose.pose();
        Matrix3f rootNormalMC = pose.normal();
        Matrix4f projMat = RenderSystem.getProjectionMatrix();

        Matrix4f identityMat = new Matrix4f();
        Matrix4f globalBoneMat = new Matrix4f();
        Matrix4f projBoneMat = new Matrix4f();
        Matrix3f localNormalMat = new Matrix3f();
        Matrix3f globalNormalMat = new Matrix3f();

        Vector4f p1 = new Vector4f();
        Vector4f p2 = new Vector4f();
        Vector4f p3 = new Vector4f();
        Vector4f tempPos = new Vector4f();
        Vector3f tempNorm = new Vector3f();
        Matrix4f[] boneLocalTransforms = new Matrix4f[mesh.bakedBones.size()];
        boolean[] boneVisible = new boolean[mesh.bakedBones.size()];

        for (int i = 0; i < mesh.bakedBones.size(); i++) {
            calculateBoneMatrix(i, mesh.bakedBones, boneParams, boneLocalTransforms, boneVisible, identityMat);
        }

        for (int i = 0; i < mesh.bakedBones.size(); i++) {
            // 如果骨骼被標記為不可見直接跳過當前骨骼
            if (!boneVisible[i]) {
                continue;
            }

            GeoModel.BakedBone bone = mesh.bakedBones.get(i);
            if (renderPartMask != 0 && bone.partMask != renderPartMask && bone.partMask != 3) {
                continue;
            }

            Matrix4f localBoneMat = boneLocalTransforms[i];
            globalBoneMat.set(rootPoseMat).mul(localBoneMat);
            projBoneMat.set(projMat).mul(globalBoneMat);

            // 法線全域矩陣
            localBoneMat.normal(localNormalMat);
            globalNormalMat.set(rootNormalMC).mul(localNormalMat);

            int currentPackedLight = bone.glow ? LightTexture.pack(15, 15) : packedLight;

            for (GeoModel.BakedCube cube : bone.cubes) {
                for (GeoModel.BakedQuad quad : cube.quads) {
                    p1.set(quad.positions[0].x(), quad.positions[0].y(), quad.positions[0].z(), 1.0f).mul(projBoneMat);
                    p2.set(quad.positions[1].x(), quad.positions[1].y(), quad.positions[1].z(), 1.0f).mul(projBoneMat);
                    p3.set(quad.positions[2].x(), quad.positions[2].y(), quad.positions[2].z(), 1.0f).mul(projBoneMat);
                    float det = p1.x() * (p2.y() * p3.w() - p3.y() * p2.w()) - p2.x() * (p1.y() * p3.w() - p3.y() * p1.w()) + p3.x() * (p1.y() * p2.w() - p2.y() * p1.w());
                    if (det <= 0.0f && cube.cullable) {
                        continue;
                    }
                    tempNorm.set(quad.normal).mul(globalNormalMat).normalize();
                    for (int v = 0; v < 4; v++) {
                        tempPos.set(quad.positions[v].x(), quad.positions[v].y(), quad.positions[v].z(), 1.0f).mul(globalBoneMat);
                        vertexConsumer.vertex(tempPos.x(), tempPos.y(), tempPos.z(), r, g, b, a, quad.uvs[v].x(), quad.uvs[v].y(), packedOverlay, currentPackedLight, tempNorm.x(), tempNorm.y(), tempNorm.z());
                    }
                }
            }
        }
    }

    private static Matrix4f calculateBoneMatrix(int idx, java.util.List<GeoModel.BakedBone> bones, float[] boneParams, Matrix4f[] cache, boolean[] visibleCache, Matrix4f rootPose) {
        if (cache[idx] != null) return cache[idx];

        GeoModel.BakedBone bone = bones.get(idx);
        Matrix4f parentMatrix = rootPose;
        boolean isVisible = true;

        if (bone.parentIdx != -1) {
            parentMatrix = calculateBoneMatrix(bone.parentIdx, bones, boneParams, cache, visibleCache, rootPose);
            // 如果父骨骼不可見，子骨骼必然跟著不可見
            if (!visibleCache[bone.parentIdx]) {
                isVisible = false;
            }
        }

        Matrix4f localMat = new Matrix4f(parentMatrix);

        int pOffset = idx * 12;
        float animRx = boneParams[pOffset];
        float animRy = boneParams[pOffset + 1];
        float animRz = boneParams[pOffset + 2];
        float animTx = boneParams[pOffset + 3];
        float animTy = boneParams[pOffset + 4];
        float animTz = boneParams[pOffset + 5];
        float animSx = boneParams[pOffset + 6];
        float animSy = boneParams[pOffset + 7];
        float animSz = boneParams[pOffset + 8];

        float unk1 = boneParams[pOffset + 9];
        float unk2 = boneParams[pOffset + 10];
        float unk3 = boneParams[pOffset + 11];

        if (unk1 != 0.0F && unk2 != 0.0F && unk3 != 0.0F) {
            //"".hashCode();
        }

        if (animSx == 0.0f && animSy == 0.0f && animSz == 0.0f) {
            isVisible = false;
        }

        localMat.translate(
                (bone.pivotX - animTx) * 0.0625f,
                (bone.pivotY + animTy) * 0.0625f,
                (bone.pivotZ + animTz) * 0.0625f
        );
        localMat.rotateZ(animRz);
        localMat.rotateY(animRy);
        localMat.rotateX(animRx);

        if (bone.name.equals("gun")) {
            //"".hashCode();
        }

        if (animSx != 1.0f || animSy != 1.0f || animSz != 1.0f) {
            localMat.scale(animSx, animSy, animSz);
        }

        localMat.translate(-bone.pivotX / 16f, -bone.pivotY / 16f, -bone.pivotZ / 16f);

        cache[idx] = localMat;
        visibleCache[idx] = isVisible; // 保存當前骨骼的可見性
        return localMat;
    }

    private static final ByteBuffer matrixTransferBuffer = ByteBuffer.allocateDirect(64 * 4).order(ByteOrder.nativeOrder());
    private static final FloatBuffer matrixTransferFloatBuffer = matrixTransferBuffer.asFloatBuffer();

    private static int currentAnimBufferCapacityBytes = 512 * 12 * 4;
    private static ByteBuffer animTransferBuffer = ByteBuffer.allocateDirect(currentAnimBufferCapacityBytes).order(ByteOrder.nativeOrder());
    private static FloatBuffer animTransferFloatBuffer = animTransferBuffer.asFloatBuffer();

    public static void nativeRenderModel(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            Matrix4f projectionModelViewMatrix,
            boolean isCompatMode,
            GeoModel mesh,
            float[] boneVertex,
            float[] stateBuffer,
            int textureIndex, int renderPartMask,
            int packedLight, int packedOverlay,
            float r, float g, float b, float a,
            boolean isPreview) {

        if (mesh.nativeModelHandle == 0 || mesh.vertexOutBuffer == null) return;

        Matrix4f projMat = RenderSystem.getProjectionMatrix();
        ByteBuffer outBuffer = mesh.vertexOutBuffer;
        boolean useDirectMemoryTransfer = !isCompatMode && (vertexConsumer instanceof BufferBuilder);

        matrixTransferFloatBuffer.clear();
        pose.pose().get(0, matrixTransferFloatBuffer);
        pose.normal().get(16, matrixTransferFloatBuffer);
        projMat.get(32, matrixTransferFloatBuffer);

        int requiredBytes = boneVertex.length * 4;
        if (currentAnimBufferCapacityBytes < requiredBytes) {
            currentAnimBufferCapacityBytes = Math.max(currentAnimBufferCapacityBytes * 2, requiredBytes);
            animTransferBuffer = ByteBuffer.allocateDirect(currentAnimBufferCapacityBytes).order(ByteOrder.nativeOrder());
            animTransferFloatBuffer = animTransferBuffer.asFloatBuffer();
        }
        animTransferFloatBuffer.clear();
        animTransferFloatBuffer.put(boneVertex);

        int vertexCount = GeoModel.nComputeModelVertices(mesh.nativeModelHandle, outBuffer, matrixTransferBuffer, animTransferBuffer, renderPartMask, packedLight, packedOverlay, r, g, b, a, useDirectMemoryTransfer);

        if (vertexCount == 0) return;

        if (useDirectMemoryTransfer) {
            BufferBuilder builder = (BufferBuilder) vertexConsumer;
            outBuffer.position(0);
            outBuffer.limit(vertexCount * 36);
            builder.putBulkData(outBuffer);
            outBuffer.clear();
        } else {
            long address = MemoryUtil.memAddress(mesh.vertexOutBuffer);
            for (int i = 0; i < vertexCount; i++) {
                long ptr = address + (i * 14L * 4L);
                float vx = MemoryUtil.memGetFloat(ptr);
                float vy = MemoryUtil.memGetFloat(ptr + 4);
                float vz = MemoryUtil.memGetFloat(ptr + 8);
                float vr = MemoryUtil.memGetFloat(ptr + 12);
                float vg = MemoryUtil.memGetFloat(ptr + 16);
                float vb = MemoryUtil.memGetFloat(ptr + 20);
                float va = MemoryUtil.memGetFloat(ptr + 24);
                float u = MemoryUtil.memGetFloat(ptr + 28);
                float v = MemoryUtil.memGetFloat(ptr + 32);
                int overlay = MemoryUtil.memGetInt(ptr + 36);
                int light = MemoryUtil.memGetInt(ptr + 40);
                float nx = MemoryUtil.memGetFloat(ptr + 44);
                float ny = MemoryUtil.memGetFloat(ptr + 48);
                float nz = MemoryUtil.memGetFloat(ptr + 52);
                vertexConsumer.vertex(vx, vy, vz, vr, vg, vb, va, u, v, overlay, light, nx, ny, nz);
            }
        }

    }
}
