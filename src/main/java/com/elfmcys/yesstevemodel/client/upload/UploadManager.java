package com.elfmcys.yesstevemodel.client.upload;

import com.elfmcys.yesstevemodel.ResourceCleanupHelper;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.texture.ITextureMap;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ReferenceIntMutablePair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UploadManager {

    private static final int UPLOAD_TIME_LIMIT_MS = 20;

    private static long textureCounter = 0;

    private static final IdentityHashMap<AbstractTexture, WeakReference<TextureLocatable>> textureCache = new IdentityHashMap<>();

    private static final Queue<Pair<TextureLocatable, AbstractTexture>> pendingUploads = Queues.newArrayDeque();

    private static final ConcurrentHashMap<AbstractTexture, ReferenceIntMutablePair<ResourceLocation>> expiredTextures = new ConcurrentHashMap<>();

    private static final Queue<ResourceLocation> pendingReleases = Queues.newArrayDeque();

    public static IResourceLocatable getOrCreateLocatable(AbstractTexture texture, boolean z) {
        return getOrCreateLocatableWithSize(texture, z, 200);
    }

    public static IResourceLocatable getOrCreateLocatableWithSize(AbstractTexture texture, boolean z, int i) {
        RenderSystem.assertOnRenderThread();
        WeakReference<TextureLocatable> weakReference = textureCache.get(texture);
        if (weakReference != null) {
            TextureLocatable locatable = weakReference.get();
            if (locatable != null) {
                if (z && !locatable.registered) {
                    registerTexture(texture, locatable);
                }
                return locatable;
            }
            textureCache.remove(texture);
        }
        ReferenceIntMutablePair<ResourceLocation> removed = expiredTextures.remove(texture);
        TextureLocatable locatable;
        if (removed != null) {
            locatable = new TextureLocatable(removed.first(), i);
        } else {
            locatable = new TextureLocatable(i);
        }
        if (texture instanceof ITextureMap) {
            for (AbstractTexture suffixTexture : ((ITextureMap) texture).getSuffixTextures().values()) {
                if (locatable.suffixTextures == null)
                    locatable.suffixTextures = new ArrayList<>(2);

                locatable.suffixTextures.add(getOrCreateLocatableWithSize(suffixTexture, z, i));
            }
        }
        textureCache.put(texture, new WeakReference<>(locatable));
        if (z) {
            registerTexture(texture, locatable);
        } else {
            pendingUploads.add(Pair.of(locatable, texture));
        }
        return locatable;
    }

    public static void removeTexture(AbstractTexture abstractTexture) {
        RenderSystem.assertOnRenderThread();
        textureCache.remove(abstractTexture);
    }

    public static void processPendingUploads() {
        RenderSystem.assertOnRenderThread();
        if (!expiredTextures.isEmpty()) {
            Iterator<Map.Entry<AbstractTexture, ReferenceIntMutablePair<ResourceLocation>>> it = expiredTextures.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<AbstractTexture, ReferenceIntMutablePair<ResourceLocation>> next = it.next();
                int iSecondInt = next.getValue().secondInt();
                if (iSecondInt <= 0) {
                    pendingReleases.add(next.getValue().first());
                    it.remove();
                } else {
                    next.getValue().second(iSecondInt - 1);
                }
            }
        }
        StopWatch stopWatchCreateStarted = StopWatch.createStarted();
        do {
            Pair<TextureLocatable, AbstractTexture> pairPoll = pendingUploads.poll();
            if (pairPoll != null) {
                registerTexture(pairPoll.right(), pairPoll.left());
            } else {
                TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                do {
                    ResourceLocation resourceLocationPoll = pendingReleases.poll();
                    if (resourceLocationPoll != null) {
                        textureManager.release(resourceLocationPoll);
                    } else {
                        return;
                    }
                } while (stopWatchCreateStarted.getTime() < UPLOAD_TIME_LIMIT_MS);
                return;
            }
        } while (stopWatchCreateStarted.getTime() < UPLOAD_TIME_LIMIT_MS);
    }

    private static void registerTexture(AbstractTexture texture, TextureLocatable locatable) {
        if (!locatable.registered) {
            Minecraft.getInstance().getTextureManager().register(locatable.resourceLocation, texture);
            ResourceCleanupHelper.registerBiCleanup(locatable, locatable.resourceLocation, locatable.resolution, (resourceLocation, num) -> {
                expiredTextures.put(texture, ReferenceIntMutablePair.of(resourceLocation, num));
            });
            locatable.markRegistered();
        }
    }

    private static class TextureLocatable implements IResourceLocatable {

        private final ResourceLocation resourceLocation;

        private final int resolution;

        private List<IResourceLocatable> suffixTextures;

        private volatile boolean registered;

        public TextureLocatable(ResourceLocation resourceLocation, int resolution) {
            this.resourceLocation = resourceLocation;
            this.resolution = resolution;
        }

        TextureLocatable(int resolution) {
            this.resourceLocation = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "textures/" + ++textureCounter);
            this.resolution = resolution;
            this.registered = false;
        }

        @Override
        public Optional<ResourceLocation> getResourceLocation() {
            return this.registered ? Optional.of(this.resourceLocation) : Optional.empty();
        }

        public void markRegistered() {
            this.registered = true;
        }
    }
}