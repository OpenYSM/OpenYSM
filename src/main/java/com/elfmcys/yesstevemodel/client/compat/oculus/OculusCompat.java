package com.elfmcys.yesstevemodel.client.compat.oculus;

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.texture.pbr.loader.PBRTextureLoader;
import net.coderbot.iris.texture.pbr.loader.PBRTextureLoaderRegistry;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.function.LongSupplier;

public class OculusCompat {

    private static final String MOD_ID = "oculus";

    private static LongSupplier currentFrameSupplier;

    private static VertexFormat vertexFormat;

    private static boolean IS_LOADED = false;

    private static long lastFrameId = -1;

    private static boolean pbrActive = false;

    public static void init() {
        ModList.get().getModContainerById(MOD_ID).ifPresent(modContainer -> {
            try {
                if (modContainer.getModInfo().getVersion().compareTo(new DefaultArtifactVersion("1.7.0")) >= 0) {
                    vertexFormat = IrisVertexFormats.ENTITY;
                    currentFrameSupplier = OculusCompat::getOrUpdateFrame;
                    PBRTextureLoaderV2.register();
                } else {
                    vertexFormat = net.coderbot.iris.vertices.IrisVertexFormats.ENTITY;
                    currentFrameSupplier = OculusCompat::getCurrentFrame;
                    PBRTextureLoaderV1.register();
                }
                currentFrameSupplier.getAsLong();
                isPBRActive();
                IS_LOADED = true;
            } catch (Throwable th) {
                YesSteveModel.LOGGER.error("Failed to setup oculus compat", th);
                vertexFormat = null;
                currentFrameSupplier = null;
                IS_LOADED = false;
            }
        });
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static boolean isPBRActive() {
        return IS_LOADED && IrisApi.getInstance().isRenderingShadowPass();
    }

    private static long getCurrentFrame() {
        return ((short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity()) | (((short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity()) << 16) | ((long) ((short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem()) << 32);
    }

    private static long getOrUpdateFrame() {
        return ((short) net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE.getCurrentRenderedEntity()) | (((short) net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity()) << 16) | ((long) ((short) net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE.getCurrentRenderedItem()) << 32);
    }

    public static void updatePBRState() {
        if (IS_LOADED) {
            lastFrameId = currentFrameSupplier.getAsLong();
            pbrActive = IrisApi.getInstance().isRenderingShadowPass();
        }
    }

    private static class PBRTextureLoaderV1 implements PBRTextureLoader<OuterFileTexture> {

        private static final PBRTextureLoaderV1 INSTANCE = new PBRTextureLoaderV1();

        private PBRTextureLoaderV1() {
        }

        public void load(OuterFileTexture texture, ResourceManager resourceManager, PBRTextureLoader.PBRTextureConsumer pBRTextureConsumer) {
            AbstractTexture abstractTexture = texture.getSuffixTextures().get(ShadersTextureType.NORMAL);
            if (abstractTexture != null) {
                pBRTextureConsumer.acceptNormalTexture(abstractTexture);
            }
            AbstractTexture abstractTexture2 = texture.getSuffixTextures().get(ShadersTextureType.SPECULAR);
            if (abstractTexture2 != null) {
                pBRTextureConsumer.acceptSpecularTexture(abstractTexture2);
            }
        }

        public static void register() {
            PBRTextureLoaderRegistry.INSTANCE.register(OuterFileTexture.class, INSTANCE);
        }
    }

    private static class PBRTextureLoaderV2 implements net.irisshaders.iris.texture.pbr.loader.PBRTextureLoader<OuterFileTexture> {

        private static final PBRTextureLoaderV2 INSTANCE = new PBRTextureLoaderV2();

        private PBRTextureLoaderV2() {
        }

        public void load(OuterFileTexture texture, ResourceManager resourceManager, net.irisshaders.iris.texture.pbr.loader.PBRTextureLoader.PBRTextureConsumer pBRTextureConsumer) {
            AbstractTexture abstractTexture = texture.getSuffixTextures().get(ShadersTextureType.NORMAL);
            if (abstractTexture != null) {
                pBRTextureConsumer.acceptNormalTexture(abstractTexture);
            }
            AbstractTexture abstractTexture2 = texture.getSuffixTextures().get(ShadersTextureType.SPECULAR);
            if (abstractTexture2 != null) {
                pBRTextureConsumer.acceptSpecularTexture(abstractTexture2);
            }
        }

        public static void register() {
            net.irisshaders.iris.texture.pbr.loader.PBRTextureLoaderRegistry.INSTANCE.register(OuterFileTexture.class, INSTANCE);
        }
    }
}