package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.event.ClientTickEvent;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.animation.AnimationTracker;
import com.elfmcys.yesstevemodel.client.animation.molang.PhysicsManager;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.util.log.ILogger;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerPreviewEntity extends CustomPlayerEntity implements IPreviewAnimatable {

    private final AnimationTracker animationStateMachine;

    private boolean customAnimationActive;

    public PlayerPreviewEntity() {
        super(new DummyPlayer(), false, false);
        this.animationStateMachine = new AnimationTracker();
    }

    @Override
    public void resetModel() {
        this.animationStateMachine.setQueuedAnimation(StringPool.EMPTY);
        this.animationStateMachine.setCurrentAnimation(StringPool.EMPTY);
        this.animationStateMachine.setPreviousAnimation(StringPool.EMPTY);
        this.customAnimationActive = false;
        super.resetModel();
    }

    @Override
    @NotNull
    public AnimationTracker getAnimationStateMachine() {
        return this.animationStateMachine;
    }

    @Override
    public PhysicsManager getPhysicsManager() {
        return this.physicsManager;
    }

    @Override
    public void setCustomAnimationActive(boolean active) {
        this.customAnimationActive = active;
    }

    @Override
    public boolean isDebugMode() {
        return true;
    }

    @Override
    public boolean shouldRenderOverlay() {
        return this.customAnimationActive;
    }

    @Override
    public int getRefreshRate() {
        return ClientTickEvent.getRefreshRate();
    }

    @Override
    public boolean hasCustomTexture() {
        return true;
    }

    @Override
    public AnimationEvent<?> processAnimationImpl(float partialTick, boolean isFirstPerson) {
        Entity entity2 = this.entity;
        if ((entity2 instanceof DummyPlayer) && !((DummyPlayer) entity2).ensureLevel()) {
            return null;
        }
        return super.processAnimationImpl(partialTick, isFirstPerson);
    }

    public static boolean isPreviewPlayer(Player player) {
        return player instanceof DummyPlayer;
    }

    @Override
    public boolean shouldSkipAnimation(AnimationEvent<?> event) {
        return true;
    }

    @Override
    public ILogger getLogger() {
        return null;
    }

    @Override
    @NotNull
    public LivingAnimatable<Player>.TexturedModelWrapper buildRenderShape(ModelAssembly modelAssembly, boolean isActive) {
        return new TexturedModelWrapper(modelAssembly, isActive, false, true, 300);
    }

    private static class DummyPlayer extends AbstractClientPlayer {
        public DummyPlayer() {
            super(Minecraft.getInstance().level, createGameProfile());
        }

        private static GameProfile createGameProfile() {
            UUID uuidRandomUUID = UUID.randomUUID();
            return new GameProfile(uuidRandomUUID, "ysm_" + uuidRandomUUID.toString().replace('-', '_'));
        }

        public boolean isSpectator() {
            return false;
        }

        public boolean isCreative() {
            return false;
        }

        public boolean ensureLevel() {
            ClientLevel clientLevel = Minecraft.getInstance().level;
            if (clientLevel != null) {
                setLevel(clientLevel);
                return true;
            }
            return false;
        }
    }
}