package com.elfmcys.yesstevemodel.geckolib3.core.molang.context;

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.VehicleCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.ProjectileCapabilityProvider;
import com.elfmcys.yesstevemodel.audio.PlaybackFlags;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.*;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import com.elfmcys.yesstevemodel.util.log.ILogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnimationContext<TEntity> implements IContext<TEntity> {

    public final TEntity entity;

    public final AnimatableEntity<?> instance;

    public final AnimationEvent<?> animationEvent;

    public final EntityModelData data;

    public AnimationControllerContext animationControllerContext;

    public PlaybackFlags playbackFlags;

    public AudioPlayerManager audioPlayerManager;

    public RandomSource random;

    public VariableStorage storage;

    public IForeignVariableStorage foreignStorage;

    private ILogger logger;

    private boolean isClientSide;

    public AnimationContext(TEntity entity, AnimatableEntity<?> instance, AnimationEvent<?> animationEvent, EntityModelData data) {
        this.entity = entity;
        this.instance = instance;
        this.animationEvent = animationEvent;
        this.data = data;
    }

    private AnimationContext(TEntity entity, AnimationContext<?> context) {
        this.entity = entity;
        this.instance = context.instance;
        this.animationEvent = context.animationEvent;
        this.data = context.data;
        this.animationControllerContext = context.animationControllerContext;
        this.random = context.random;
        this.storage = context.storage;
        this.audioPlayerManager = context.audioPlayerManager;
        if (entity instanceof Player) {
            ((Player) entity).getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> this.foreignStorage = cap.getPropertyGetter());
        } else if (entity instanceof Projectile) {
            ((Projectile) entity).getCapability(ProjectileCapabilityProvider.PROJECTILE_CAP).ifPresent(cap -> this.foreignStorage = cap.getPropertyGetter());
        } else if (entity instanceof Entity) {
            ((Entity) entity).getCapability(VehicleCapabilityProvider.VEHICLE_CAP).ifPresent(cap -> this.foreignStorage = cap.getPropertyGetter());
        }
    }

    @Override
    public AnimationEvent<?> animationEvent() {
        return this.animationEvent;
    }

    @Override
    public AnimatableEntity<?> geoInstance() {
        return this.instance;
    }

    @Override
    public EntityModelData data() {
        return this.data;
    }

    @Override
    public AnimationControllerContext animationControllerContext() {
        return this.animationControllerContext;
    }

    @Override
    public PlaybackFlags getPlaybackFlags() {
        return this.playbackFlags;
    }

    @Override
    public RandomSource random() {
        return this.random;
    }

    @Override
    public TEntity entity() {
        return this.entity;
    }

    @Override
    public Minecraft mc() {
        return Minecraft.getInstance();
    }

    @Override
    public ClientLevel level() {
        Minecraft mc = mc();
        if (mc != null) {
            return mc.level;
        }
        return null;
    }

    @Override
    public <TChild> IContext<TChild> createChild(TChild child) {
        return new AnimationContext<>(child, this);
    }

    @Override
    public ITempVariableStorage tempStorage() {
        return this.storage.getLocalVariables();
    }

    @Override
    public IScopedVariableStorage scopedStorage() {
        return this.storage;
    }

    @Override
    public IForeignVariableStorage foreignStorage() {
        return this.foreignStorage;
    }

    @Override
    @Nullable
    public IControllerVariableStorage controllerStorage() {
        return this.animationControllerContext;
    }

    @Override
    @Nullable
    public IValue resolveExpression(String str) {
        return this.instance.resolveExpression(str);
    }

    @Override
    public Object callFunction(ExecutionContext<?> context, IValue value, List<?> list) {
        if (this.storage.getLocalVariables().pushScope(list)) {
            try {
                Object objMo1908xe6e508ff = value.evalSafe((ExpressionEvaluator) context);
                this.storage.getLocalVariables().popScope();
                return objMo1908xe6e508ff;
            } catch (Throwable th) {
                this.storage.getLocalVariables().popScope();
                throw th;
            }
        }
        return null;
    }

    @Override
    public Object callFunctionWithArgs(ExecutionContext<?> context, IValue value, Function.ArgumentCollection arguments) {
        if (this.storage.getLocalVariables().pushScopeWithArgs(context, arguments)) {
            try {
                Object objMo1908xe6e508ff = value.evalSafe((ExpressionEvaluator) context);
                this.storage.getLocalVariables().popScope();
                return objMo1908xe6e508ff;
            } catch (Throwable th) {
                this.storage.getLocalVariables().popScope();
                throw th;
            }
        }
        return null;
    }

    @Override
    public List<?> getAnimationLayers() {
        return this.storage.getLocalVariables().asList();
    }

    @Override
    public boolean isDebugMode() {
        return this.logger != null;
    }

    @Override
    public boolean isClientSide() {
        return this.isClientSide;
    }

    public void setIsClientSide(boolean z) {
        this.isClientSide = z;
    }

    @Override
    public void logWarning(String str, Object... objArr) {
        if (isDebugMode()) {
            this.logger.logFormatted(str, objArr);
        }
    }

    @Override
    public void logWarningComponent(Component component) {
        if (isDebugMode()) {
            this.logger.logComponent(component);
        }
    }

    @Override
    @Nullable
    public AudioPlayerManager getAudioPlayerManager(boolean global) {
        AudioPlayerManager audioPlayerManager1;
        AudioPlayerManager audioPlayerManager2;
        if (!global) {
            if (this.animationControllerContext != null && (audioPlayerManager2 = this.animationControllerContext.getAudioPlayerManager()) != null) {
                return audioPlayerManager2;
            }
            if (this.playbackFlags != null && (audioPlayerManager1 = this.playbackFlags.getAudioPlayerManager()) != null) {
                return audioPlayerManager1;
            }
        }
        return this.audioPlayerManager;
    }

    public void setAudioPlayerManager(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
    }

    public void setAnimationControllerContext(AnimationControllerContext context) {
        this.animationControllerContext = context;
    }

    public void setPlaybackFlags(PlaybackFlags playbackFlags2) {
        this.playbackFlags = playbackFlags2;
    }

    public void setStorage(VariableStorage variableStorage) {
        this.storage = variableStorage;
        this.foreignStorage = variableStorage;
    }

    public void setRandom(RandomSource random) {
        this.random = random;
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }
}