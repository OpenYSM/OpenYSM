package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.audio.*;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.client.animation.molang.MolangEventDispatcher;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.compat.oculus.OculusCompat;
import com.elfmcys.yesstevemodel.client.animation.molang.PhysicsManager;
import com.elfmcys.yesstevemodel.client.animation.molang.MolangWatchRegistry;
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.AnimationProcessor;
import com.elfmcys.yesstevemodel.util.*;
import com.elfmcys.yesstevemodel.util.log.ChatLogger;
import com.elfmcys.yesstevemodel.util.log.ILogger;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public abstract class GeoEntity<T extends Entity> extends AnimatableEntity<T> {
    private String modelId;

    private ModelAssembly modelAssembly;

    private ModelWrapper renderShape;

    private boolean loaded;

    private int updateTicks;

    @Nullable
    private PhysicsManager bones;

    @Nullable
    private MolangWatchRegistry boneLookup;

    @Nullable
    private List<IValue> renderLayers;

    @Nullable
    private Future<AnimationEvent<?>> modelFuture;

    @Nullable
    public abstract GeoEntity.ModelWrapper buildRenderShape(ModelAssembly modelAssembly, boolean isDefault);

    public abstract GeoModel getAnimationProcessor();

    public GeoEntity(T t, boolean registerWithCache) {
        super(t);
        this.modelId = "default";
        if (registerWithCache) {
            EntityRenderCache.register(this);
        }
    }

    @Override
    public PhysicsManager getPhysicsManager() {
        if (ModelPreviewRenderer.isFirstPerson() || ModelPreviewRenderer.isExtraPlayer()) {
            return this.physicsManager;
        }
        if (this.bones == null) {
            this.bones = new PhysicsManager();
        }
        return this.bones;
    }

    @Nullable
    public List<IValue> getRenderLayers() {
        return this.renderLayers;
    }

    public void setBoneLookup(@Nullable MolangWatchRegistry watchRegistry) {
        this.boneLookup = watchRegistry;
    }

    @Override
    public void setupAnim(float seekTime, boolean isFirstPerson) {
        super.setupAnim(seekTime, isFirstPerson);
        if (this.boneLookup != null) {
            AnimationProcessor<T> processor = getEvaluationContext();
            processor.execute(evaluator -> {
                this.boneLookup.evauatePreAnimation(evaluator);
                return null;
            }, false, true, null);
            processor.execute(it -> {
                this.boneLookup.evaluatePostAnimation(it);
                return null;
            }, false, false, null);
        }
    }

    public void tickModel() {
        if (this.updateTicks < this.entity.tickCount) {
            refreshModel();
            this.updateTicks = this.entity.tickCount;
        }
    }

    public final ModelAssembly getModelAssembly() {
        return this.modelAssembly;
    }

    public final void setModelId(String str) {
        this.modelId = str;
        refreshModel();
    }

    private void refreshModel() {
        ClientModelManager.getModelContext(this.modelId).ifPresentOrElse(assembly -> {
            if (this.renderShape == null || this.renderShape.isDefault || assembly != this.renderShape.context) {
                this.renderShape = buildRenderShape(assembly, false);
            }
        }, () -> {
            ModelAssembly modelAssembly = ClientModelManager.getLocalModelContext();
            if (this.renderShape == null || !this.renderShape.isDefault || modelAssembly != this.renderShape.context) {
                this.renderShape = buildRenderShape(modelAssembly, true);
            }
        });
        if (this.renderShape != null) {
            if ((this.renderShape.context != this.modelAssembly || this.renderShape.isDefault != this.loaded) && this.renderShape.isValid()) {
                this.modelAssembly = this.renderShape.context;
                this.loaded = this.renderShape.isDefault;
                onModelLoaded(this.modelAssembly);
                initAnimationControllers(getAnimationProcessor(), this.modelAssembly.getExpressionCache().getEvents());
                return;
            }
            return;
        }
        if (this.modelAssembly != null) {
            clearModel();
        }
    }

    public final ModelWrapper getRenderShape() {
        return this.renderShape;
    }

    public void onModelLoaded(ModelAssembly modelAssembly) {
        this.renderShape.audioProvider = AudioStreamCache.getOrCreateProvider(modelAssembly);
        this.renderLayers = modelAssembly.getExpressionCache().getEvents().get(MolangEventDispatcher.DEFER);
    }

    public void clearModel() {
        this.modelAssembly = null;
        this.renderLayers = null;
        this.renderShape = null;
        this.loaded = false;
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        this.bones = null;
        this.updateTicks = 0;
    }

    public void resetModel() {
        this.modelId = "default";
        this.modelInitialized = false;
        clearModel();
    }

    public final String getModelId() {
        return this.modelId;
    }

    public boolean isModelReady() {
        return this.renderShape != null && !this.renderShape.isDefault && this.renderShape.isValid();
    }

    @Override
    public boolean shouldSkipAnimation(AnimationEvent<?> event) {
        return event.isFirstPerson() || OculusCompat.isPBRActive();
    }

    @Override
    @Nullable
    public final IValue resolveExpression(String str) {
        return getModelAssembly().getExpressionCache().getFunctions().get(str);
    }

    @Override
    public Optional<IAudioStreamFactory> getAudioStreamFactory(String str) {
        AudioTrackData trackData;
        if (this.renderShape.audioProvider != null && (trackData = getModelAssembly().getExpressionCache().getSoundEffects().get(str)) != null && trackData.getData() != null && trackData.getCodec() != AudioCodec.UNDEFINED) {
            IAudioStreamProvider streamProvider = this.renderShape.audioProvider;
            return Optional.of(() -> {
                return streamProvider.createAudioStream(trackData);
            });
        }
        return Optional.empty();
    }

    @Override
    public ILogger getLogger() {
        if (AnimationDebugOverlay.isDebugActive()) {
            return ChatLogger.INSTANCE;
        }
        return null;
    }

    public void submitAsyncUpdate(float partialTick) {
        UnsafeUtil.getUnsafe().storeFence();
        this.modelFuture = YSMThreadPool.submitCallable(() -> {
            try {
                AnimationEvent<?> event = super.processAnimationImpl(partialTick, true);
                UnsafeUtil.getUnsafe().storeFence();
                return event;
            } catch (Throwable th) {
                UnsafeUtil.getUnsafe().storeFence();
                throw th;
            }
        });
    }

    @Override
    @Nullable
    public AnimationEvent<?> processAnimationImpl(float partialTick, boolean isFirstPerson) {
        RenderSystem.assertOnRenderThread();
        if (isFirstPerson && this.modelFuture != null) {
            return awaitAsyncResult();
        }
        awaitAsyncResult();
        return super.processAnimationImpl(partialTick, isFirstPerson);
    }

    public AnimationEvent<?> awaitAsyncResult() {
        if (this.modelFuture != null) {
            AnimationEvent<?> event = null;
            try {
                event = this.modelFuture.get();
                UnsafeUtil.getUnsafe().loadFence();
            } catch (InterruptedException e) {
            } catch (Throwable th) {
                th.printStackTrace();
            }
            this.modelFuture = null;
            return event;
        }
        return null;
    }

    public boolean supportsAsync() {
        return true;
    }

    public static class ModelWrapper {

        public final ModelAssembly context;

        public final boolean isDefault;

        @Nullable
        public IAudioStreamProvider audioProvider;

        public ModelWrapper(ModelAssembly modelAssembly, boolean isDefault) {
            this.context = modelAssembly;
            this.isDefault = isDefault;
        }

        public boolean isValid() {
            return true;
        }
    }
}