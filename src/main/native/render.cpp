#if defined(__GNUC__) && (defined(__x86_64__) || defined(_M_X64) || defined(__i386__) || defined(_M_IX86))
#pragma GCC optimize("O3,unroll-loops")
#pragma GCC target("sse4.1,fma")
#endif

#include <immintrin.h>
#include <vector>
#include <cmath>
#include <cstring>
#include <functional>
#include "jni.h"
#include <cstdint>

#if defined(__GNUC__) || defined(__clang__)
#define FAST_SINCOS(x, s, c) sincosf((x), (s), (c))
#else
inline void ms_sincosf(float x, float *s, float *c) {
    *s = std::sin(x);
    *c = std::cos(x);
}
#define FAST_SINCOS(x, s, c) ms_sincosf((x), (s), (c))
#endif

#if defined(__FMA__)
#define MADD_PS(a, b, c) _mm_fmadd_ps((a), (b), (c))
#else
#define MADD_PS(a, b, c) _mm_add_ps(_mm_mul_ps((a), (b)), (c))
#endif

#pragma pack(push, 1)
struct PackedVertex {
    float x, y, z;
    uint8_t r, g, b, a;
    float u, v;
    int overlay;
    int light;
    int8_t nx, ny, nz;
    uint8_t padding;
};
#pragma pack(pop)
struct FastQuad {
    int boneIdx;
    bool cullable;
    __m128 x, y, z;
    __m128 u, v;
    float nx, ny, nz;
};

struct NativeBone {
    int parentIdx;
    int partMask;
    bool glow;
    float pivotX, pivotY, pivotZ;
};

struct alignas(16) PrecomputedBoneMats {
    alignas(16) float gb[16];
    __m128 gn_c0, gn_c1, gn_c2;
    int currentLight;
};

struct alignas(16) Mat4 {
    float m[16];
    Mat4() { identity(); }
    Mat4(const float *data) { std::memcpy(m, data, 16 * sizeof(float)); }

    inline void identity() {
        std::memset(m, 0, 16 * sizeof(float));
        m[0] = m[5] = m[10] = m[15] = 1.0f;
    }

    inline void mul(const Mat4 &right) {
        __m128 l0 = _mm_load_ps(&m[0]);
        __m128 l1 = _mm_load_ps(&m[4]);
        __m128 l2 = _mm_load_ps(&m[8]);
        __m128 l3 = _mm_load_ps(&m[12]);

        auto mac = [&](const float *r_col) {
            __m128 v = _mm_loadu_ps(r_col);
            __m128 res = _mm_mul_ps(l0, _mm_shuffle_ps(v, v, _MM_SHUFFLE(0, 0, 0, 0)));
            res = MADD_PS(l1, _mm_shuffle_ps(v, v, _MM_SHUFFLE(1, 1, 1, 1)), res);
            res = MADD_PS(l2, _mm_shuffle_ps(v, v, _MM_SHUFFLE(2, 2, 2, 2)), res);
            res = MADD_PS(l3, _mm_shuffle_ps(v, v, _MM_SHUFFLE(3, 3, 3, 3)), res);
            return res;
        };
        __m128 r0 = mac(&right.m[0]);
        __m128 r1 = mac(&right.m[4]);
        __m128 r2 = mac(&right.m[8]);
        __m128 r3 = mac(&right.m[12]);
        _mm_store_ps(&m[0], r0);
        _mm_store_ps(&m[4], r1);
        _mm_store_ps(&m[8], r2);
        _mm_store_ps(&m[12], r3);
    }

    inline Mat4 normalMatrix4x4() const {
        Mat4 res;
        res.m[0] = m[0];
        res.m[1] = m[1];
        res.m[2] = m[2];
        res.m[4] = m[4];
        res.m[5] = m[5];
        res.m[6] = m[6];
        res.m[8] = m[8];
        res.m[9] = m[9];
        res.m[10] = m[10];
        return res;
    }
};

struct NativeModel {
    std::vector<NativeBone> bones;
    std::vector<FastQuad> fastQuads;
    std::vector<int> evalOrder;
    std::vector<Mat4> cacheLocalTransforms;
    std::vector<char> cacheVisible;
    std::vector<PrecomputedBoneMats> cachePrecompMats;
};

JNIEXPORT jlong JNICALL Java_com_elfmcys_yesstevemodel_geckolib3_geo_render_built_GeoModel_nInitModelCache(
    JNIEnv *env, jclass clazz, jobject buffer) {
    char *data = (char *) env->GetDirectBufferAddress(buffer);
    if (!data) return 0;

    NativeModel *model = new NativeModel();
    int offset = 0;

    auto readInt = [&]() {
        int v;
        std::memcpy(&v, data + offset, 4);
        offset += 4;
        return v;
    };
    auto readFloat = [&]() {
        float v;
        std::memcpy(&v, data + offset, 4);
        offset += 4;
        return v;
    };
    auto readByte = [&]() {
        char v = data[offset];
        offset += 1;
        return v;
    };

    int boneCount = readInt();
    model->bones.resize(boneCount);
    model->cacheLocalTransforms.resize(boneCount);
    model->cacheVisible.resize(boneCount);
    model->cachePrecompMats.resize(boneCount);
    model->fastQuads.reserve(boneCount * 20);

    for (int i = 0; i < boneCount; ++i) {
        NativeBone &bone = model->bones[i];
        bone.parentIdx = readInt();
        bone.partMask = readInt();
        bone.glow = readByte() != 0;
        bone.pivotX = readFloat();
        bone.pivotY = readFloat();
        bone.pivotZ = readFloat();

        int cubeCount = readInt();
        for (int j = 0; j < cubeCount; ++j) {
            bool cullable = readByte() != 0;
            int quadCount = readInt();
            for (int k = 0; k < quadCount; ++k) {
                FastQuad fq;
                fq.boneIdx = i;
                fq.cullable = cullable;

                alignas(16) float tmpX[4], tmpY[4], tmpZ[4], tmpU[4], tmpV[4];
                for (int v = 0; v < 4; ++v) {
                    tmpX[v] = readFloat();
                    tmpY[v] = readFloat();
                    tmpZ[v] = readFloat();
                }
                for (int v = 0; v < 4; ++v) {
                    tmpU[v] = readFloat();
                    tmpV[v] = readFloat();
                }
                fq.nx = readFloat();
                fq.ny = readFloat();
                fq.nz = readFloat();

                fq.x = _mm_load_ps(tmpX);
                fq.y = _mm_load_ps(tmpY);
                fq.z = _mm_load_ps(tmpZ);
                fq.u = _mm_load_ps(tmpU);
                fq.v = _mm_load_ps(tmpV);
                model->fastQuads.push_back(fq);
            }
        }
    }
    model->fastQuads.shrink_to_fit();

    std::vector<char> visited(boneCount, 0);
    std::function<void(int)> dfs = [&](int idx) {
        if (visited[idx]) return;
        visited[idx] = 1;
        if (model->bones[idx].parentIdx != -1) dfs(model->bones[idx].parentIdx);
        model->evalOrder.push_back(idx);
    };
    for (int i = 0; i < boneCount; ++i) dfs(i);

    return reinterpret_cast<jlong>(model);
}

JNIEXPORT void JNICALL Java_com_elfmcys_yesstevemodel_geckolib3_geo_render_built_GeoModel_nDestroyModelCache(
    JNIEnv *env, jclass clazz, jlong handle) {
    delete reinterpret_cast<NativeModel *>(handle);
}

JNIEXPORT jint JNICALL Java_com_elfmcys_yesstevemodel_geckolib3_geo_render_built_GeoModel_nComputeModelVertices(
    JNIEnv *env, jclass clazz, jlong handle,
    jobject outBuffer, jobject matrixBuffer, jobject animBuffer,
    jint renderPartMask, jint packedLight, jint packedOverlay,
    jfloat r, jfloat g, jfloat b, jfloat a,
    jboolean packTo36Bytes) {
    NativeModel *model = reinterpret_cast<NativeModel *>(handle);
    if (!model || model->fastQuads.empty()) return 0;

    uint8_t *outDataRaw = (uint8_t *) env->GetDirectBufferAddress(outBuffer);
    int vertexCountOut = 0;
    float *matricesData = (float *) env->GetDirectBufferAddress(matrixBuffer);
    float *animData = (float *) env->GetDirectBufferAddress(animBuffer);

    Mat4 rootPoseMat(matricesData);
    float *rootNormalArr = matricesData + 16;
    Mat4 projMat(matricesData + 32);

    Mat4 rootNormalMat;
    rootNormalMat.m[0] = rootNormalArr[0];
    rootNormalMat.m[1] = rootNormalArr[1];
    rootNormalMat.m[2] = rootNormalArr[2];
    rootNormalMat.m[4] = rootNormalArr[3];
    rootNormalMat.m[5] = rootNormalArr[4];
    rootNormalMat.m[6] = rootNormalArr[5];
    rootNormalMat.m[8] = rootNormalArr[6];
    rootNormalMat.m[9] = rootNormalArr[7];
    rootNormalMat.m[10] = rootNormalArr[8];

    Mat4 identityMat;
    for (int i: model->evalOrder) {
        NativeBone &bone = model->bones[i];
        Mat4 parentMatrix = (bone.parentIdx != -1) ? model->cacheLocalTransforms[bone.parentIdx] : identityMat;
        char isVisible = (bone.parentIdx != -1) ? model->cacheVisible[bone.parentIdx] : 1;

        int pOffset = i * 12;
        float animRx = animData[pOffset + 0], animRy = animData[pOffset + 1], animRz = animData[pOffset + 2];
        float animTx = animData[pOffset + 3], animTy = animData[pOffset + 4], animTz = animData[pOffset + 5];
        float animSx = animData[pOffset + 6], animSy = animData[pOffset + 7], animSz = animData[pOffset + 8];

        if (animSx == 0.0f && animSy == 0.0f && animSz == 0.0f) isVisible = 0;

        float px = bone.pivotX * 0.0625f, py = bone.pivotY * 0.0625f, pz = bone.pivotZ * 0.0625f;
        float dx = px - animTx * 0.0625f;
        float dy = py + animTy * 0.0625f;
        float dz = pz + animTz * 0.0625f;

        float cx, sx, cy, sy, cz, sz;
        FAST_SINCOS(animRx, &sx, &cx);
        FAST_SINCOS(animRy, &sy, &cy);
        FAST_SINCOS(animRz, &sz, &cz);

        Mat4 localMat;
        localMat.m[0] = (cz * cy) * animSx;
        localMat.m[1] = (sz * cy) * animSx;
        localMat.m[2] = (-sy) * animSx;
        localMat.m[4] = (cz * sy * sx - sz * cx) * animSy;
        localMat.m[5] = (sz * sy * sx + cz * cx) * animSy;
        localMat.m[6] = (cy * sx) * animSy;
        localMat.m[8] = (cz * sy * cx + sz * sx) * animSz;
        localMat.m[9] = (sz * sy * cx - cz * sx) * animSz;
        localMat.m[10] = (cy * cx) * animSz;

        localMat.m[12] = dx - (localMat.m[0] * px + localMat.m[4] * py + localMat.m[8] * pz);
        localMat.m[13] = dy - (localMat.m[1] * px + localMat.m[5] * py + localMat.m[9] * pz);
        localMat.m[14] = dz - (localMat.m[2] * px + localMat.m[6] * py + localMat.m[10] * pz);

        Mat4 resMat = parentMatrix;
        resMat.mul(localMat);
        model->cacheLocalTransforms[i] = resMat;
        model->cacheVisible[i] = isVisible;
    }

    int glowLight = (15 << 4) | (15 << 20);
    size_t boneCount = model->bones.size();
    for (size_t i = 0; i < boneCount; ++i) {
        if (model->cacheVisible[i] == 0) continue;
        Mat4 globalBoneMat = rootPoseMat;
        globalBoneMat.mul(model->cacheLocalTransforms[i]);
        Mat4 globalNormalMat = rootNormalMat;
        globalNormalMat.mul(model->cacheLocalTransforms[i].normalMatrix4x4());

        auto &precomp = model->cachePrecompMats[i];
        std::memcpy(precomp.gb, globalBoneMat.m, 16 * sizeof(float));
        precomp.gn_c0 = _mm_loadu_ps(&globalNormalMat.m[0]);
        precomp.gn_c1 = _mm_loadu_ps(&globalNormalMat.m[4]);
        precomp.gn_c2 = _mm_loadu_ps(&globalNormalMat.m[8]);
        precomp.currentLight = model->bones[i].glow ? glowLight : packedLight;
    }

    __m128 p00 = _mm_set1_ps(projMat.m[0]), p01 = _mm_set1_ps(projMat.m[4]), p02 = _mm_set1_ps(projMat.m[8]), p03 =
            _mm_set1_ps(projMat.m[12]);
    __m128 p10 = _mm_set1_ps(projMat.m[1]), p11 = _mm_set1_ps(projMat.m[5]), p12 = _mm_set1_ps(projMat.m[9]), p13 =
            _mm_set1_ps(projMat.m[13]);
    __m128 p30 = _mm_set1_ps(projMat.m[3]), p31 = _mm_set1_ps(projMat.m[7]), p32 = _mm_set1_ps(projMat.m[11]), p33 =
            _mm_set1_ps(projMat.m[15]);

    uint8_t cr = (uint8_t) (r * 255.0f), cg = (uint8_t) (g * 255.0f), cb = (uint8_t) (b * 255.0f), ca = (uint8_t) (
        a * 255.0f);
    uint32_t rgba_bits = cr | (cg << 8) | (cb << 16) | (ca << 24);
    __m128 gRGBA = _mm_set1_ps(*(float *) &rgba_bits);
    __m128 gOverlay = _mm_set1_ps(*(float *) &packedOverlay);

    for (const auto &fq: model->fastQuads) {
        int bIdx = fq.boneIdx;
        if (model->cacheVisible[bIdx] == 0) continue;
        const NativeBone &bone = model->bones[bIdx];
        if (renderPartMask != 0 && bone.partMask != renderPartMask && bone.partMask != 3) continue;

        const auto &pMat = model->cachePrecompMats[bIdx];

        __m128 gX = MADD_PS(_mm_set1_ps(pMat.gb[0]), fq.x,
                            MADD_PS(_mm_set1_ps(pMat.gb[4]), fq.y, MADD_PS(_mm_set1_ps(pMat.gb[8]), fq.z, _mm_set1_ps(
                                pMat.gb[12]))));
        __m128 gY = MADD_PS(_mm_set1_ps(pMat.gb[1]), fq.x,
                            MADD_PS(_mm_set1_ps(pMat.gb[5]), fq.y, MADD_PS(_mm_set1_ps(pMat.gb[9]), fq.z, _mm_set1_ps(
                                pMat.gb[13]))));
        __m128 gZ = MADD_PS(_mm_set1_ps(pMat.gb[2]), fq.x,
                            MADD_PS(_mm_set1_ps(pMat.gb[6]), fq.y, MADD_PS(_mm_set1_ps(pMat.gb[10]), fq.z, _mm_set1_ps(
                                pMat.gb[14]))));

        if (fq.cullable) {
            __m128 pX = MADD_PS(p00, gX, MADD_PS(p01, gY, MADD_PS(p02, gZ, p03)));
            __m128 pY = MADD_PS(p10, gX, MADD_PS(p11, gY, MADD_PS(p12, gZ, p13)));
            __m128 pW = MADD_PS(p30, gX, MADD_PS(p31, gY, MADD_PS(p32, gZ, p33)));

            __m128 pY_120 = _mm_shuffle_ps(pY, pY, _MM_SHUFFLE(3, 0, 2, 1));
            __m128 pW_201 = _mm_shuffle_ps(pW, pW, _MM_SHUFFLE(3, 1, 0, 2));
            __m128 pY_201 = _mm_shuffle_ps(pY, pY, _MM_SHUFFLE(3, 1, 0, 2));
            __m128 pW_120 = _mm_shuffle_ps(pW, pW, _MM_SHUFFLE(3, 0, 2, 1));

            __m128 sub = _mm_sub_ps(_mm_mul_ps(pY_120, pW_201), _mm_mul_ps(pY_201, pW_120));
            __m128 mx = _mm_mul_ps(pX, sub);
            __m128 mx1 = _mm_shuffle_ps(mx, mx, _MM_SHUFFLE(1, 1, 1, 1));
            __m128 mx2 = _mm_shuffle_ps(mx, mx, _MM_SHUFFLE(2, 2, 2, 2));
            __m128 sum = _mm_add_ps(mx, _mm_add_ps(mx1, mx2));

            float det = _mm_cvtss_f32(sum);
            if (det <= 0.0f) continue;
        }

        __m128 n_res = MADD_PS(pMat.gn_c0, _mm_set1_ps(fq.nx),
                               MADD_PS(pMat.gn_c1, _mm_set1_ps(fq.ny), _mm_mul_ps(pMat.gn_c2, _mm_set1_ps(fq.nz))));
        __m128 dp = _mm_mul_ps(n_res, n_res);
        __m128 sum = _mm_add_ps(dp, _mm_shuffle_ps(dp, dp, _MM_SHUFFLE(2, 3, 0, 1)));
        sum = _mm_add_ps(sum, _mm_shuffle_ps(sum, sum, _MM_SHUFFLE(1, 0, 3, 2)));
        n_res = _mm_mul_ps(n_res, _mm_rsqrt_ps(_mm_max_ps(sum, _mm_set1_ps(1e-8f))));

        alignas(16) float finalNorm[4];
        _mm_store_ps(finalNorm, n_res);
        int8_t bnx = (int8_t) (finalNorm[0] * 127.0f), bny = (int8_t) (finalNorm[1] * 127.0f), bnz = (int8_t) (
            finalNorm[2] * 127.0f);
        uint32_t norm_bits = (uint8_t) bnx | ((uint8_t) bny << 8) | ((uint8_t) bnz << 16);

        if (packTo36Bytes) {
            __m128 row0 = gX, row1 = gY, row2 = gZ, row3 = gRGBA;
            _MM_TRANSPOSE4_PS(row0, row1, row2, row3);

            __m128 gLight = _mm_set1_ps(*(float *) &pMat.currentLight);
            __m128 row0b = fq.u, row1b = fq.v, row2b = gOverlay, row3b = gLight;
            _MM_TRANSPOSE4_PS(row0b, row1b, row2b, row3b);

            uint8_t *outPtr = outDataRaw + (vertexCountOut * 36);

            _mm_storeu_ps((float *) (outPtr + 0), row0);
            _mm_storeu_ps((float *) (outPtr + 16), row0b);
            *(uint32_t *) (outPtr + 32) = norm_bits;
            _mm_storeu_ps((float *) (outPtr + 36), row1);
            _mm_storeu_ps((float *) (outPtr + 52), row1b);
            *(uint32_t *) (outPtr + 68) = norm_bits;
            _mm_storeu_ps((float *) (outPtr + 72), row2);
            _mm_storeu_ps((float *) (outPtr + 88), row2b);
            *(uint32_t *) (outPtr + 104) = norm_bits;
            _mm_storeu_ps((float *) (outPtr + 108), row3);
            _mm_storeu_ps((float *) (outPtr + 124), row3b);
            *(uint32_t *) (outPtr + 140) = norm_bits;

            vertexCountOut += 4;
        } else {
            alignas(16) float fx[4], fy[4], fz[4], fu[4], fv[4];
            _mm_store_ps(fx, gX);
            _mm_store_ps(fy, gY);
            _mm_store_ps(fz, gZ);
            _mm_store_ps(fu, fq.u);
            _mm_store_ps(fv, fq.v);
            float *outFloat = (float *) outDataRaw;
            for (int v = 0; v < 4; ++v) {
                int idx = vertexCountOut * 14;
                outFloat[idx + 0] = fx[v];
                outFloat[idx + 1] = fy[v];
                outFloat[idx + 2] = fz[v];
                outFloat[idx + 3] = r;
                outFloat[idx + 4] = g;
                outFloat[idx + 5] = b;
                outFloat[idx + 6] = a;
                outFloat[idx + 7] = fu[v];
                outFloat[idx + 8] = fv[v];
                outFloat[idx + 9] = *(float *) &packedOverlay;
                outFloat[idx + 10] = *(float *) &pMat.currentLight;
                outFloat[idx + 11] = finalNorm[0];
                outFloat[idx + 12] = finalNorm[1];
                outFloat[idx + 13] = finalNorm[2];
                vertexCountOut++;
            }
        }
    }
    return vertexCountOut;
}

static const JNINativeMethod gMethods[] = {
    {
        (char *) "nInitModelCache", (char *) "(Ljava/nio/ByteBuffer;)J",
        reinterpret_cast<void *>(Java_com_elfmcys_yesstevemodel_geckolib3_geo_render_built_GeoModel_nInitModelCache)
    },
    {
        (char *) "nDestroyModelCache", (char *) "(J)V",
        reinterpret_cast<void *>(Java_com_elfmcys_yesstevemodel_geckolib3_geo_render_built_GeoModel_nDestroyModelCache)
    },
    {
        (char *) "nComputeModelVertices",
        (char *) "(JLjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;IIIFFFFZ)I",
        reinterpret_cast<void *>(
            Java_com_elfmcys_yesstevemodel_geckolib3_geo_render_built_GeoModel_nComputeModelVertices)
    }
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) return JNI_ERR;
    jclass clazz = env->FindClass("com/elfmcys/yesstevemodel/geckolib3/geo/render/built/GeoModel");
    if (clazz == nullptr) return JNI_ERR;
    if (env->RegisterNatives(clazz, gMethods, 3) < 0) return JNI_ERR;
    return JNI_VERSION_1_6;
}
