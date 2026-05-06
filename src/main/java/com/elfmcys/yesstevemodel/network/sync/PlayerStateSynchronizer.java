package com.elfmcys.yesstevemodel.network.sync;

import com.elfmcys.yesstevemodel.client.event.ShieldBlockCooldownEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.S2CSyncPlayerStatePacket;
import com.elfmcys.yesstevemodel.util.TickCounter;
import it.unimi.dsi.fastutil.objects.Object2ByteArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class PlayerStateSynchronizer {

    private TickCounter tickCounter;

    private boolean dirty;

    private boolean isFlying;

    private int experienceLevel = -1;

    private int health = -1;

    private int maxHealth = -1;

    private int foodLevel = -1;

    private float strafeInput = 0.0f;

    private float verticalInput = 0.0f;

    private float forwardInput = 0.0f;

    private boolean isShieldBlocking = false;

    private String syncedModelId = StringPool.EMPTY;

    private S2CSyncPlayerStatePacket syncMessage = new S2CSyncPlayerStatePacket(-1);

    public PlayerStateSynchronizer() {
        setDirty(false);
    }

    public void setDirty(boolean isDirty) {
        if (isDirty != this.dirty || this.tickCounter == null) {
            this.dirty = isDirty;
            if (this.dirty) {
                this.tickCounter = new TickCounter(3, 3.0f);
            } else {
                this.tickCounter = new TickCounter(4, 7.0f);
            }
        }
    }

    private S2CSyncPlayerStatePacket getOrCreateSyncMessage(ServerPlayer serverPlayer, boolean sendNow) {
        if (!sendNow || this.syncMessage.entityId != serverPlayer.getId()) {
            this.syncMessage.reset(serverPlayer.getId());
        }
        return this.syncMessage;
    }

    private void trySendSync(ServerPlayer serverPlayer) {
        if (!this.syncMessage.isEmpty() && this.tickCounter.tryIncrement()) {
            NetworkHandler.sendToTrackingEntityAndSelf(this.syncMessage, serverPlayer);
            this.syncMessage = new S2CSyncPlayerStatePacket(serverPlayer.getId());
        }
    }

    public void updateAndSync(ServerPlayer serverPlayer, boolean sendNow, boolean isDirty) {
        setDirty(isDirty);
        S2CSyncPlayerStatePacket message = getOrCreateSyncMessage(serverPlayer, sendNow);
        if (this.experienceLevel != serverPlayer.experienceLevel) {
            this.experienceLevel = serverPlayer.experienceLevel;
            if (sendNow) {
                message.setExperienceLevel(this.experienceLevel);
            }
        }
        if (this.isFlying != serverPlayer.getAbilities().flying) {
            this.isFlying = serverPlayer.getAbilities().flying;
            if (sendNow) {
                message.setFlying(this.isFlying);
            }
        }
        if (this.health != ((int) serverPlayer.getHealth())) {
            this.health = (int) serverPlayer.getHealth();
            if (sendNow) {
                message.setHealth(this.health);
            }
        }
        if (this.maxHealth != ((int) serverPlayer.getMaxHealth())) {
            this.maxHealth = (int) serverPlayer.getMaxHealth();
            if (sendNow) {
                message.setMaxHealth(this.maxHealth);
            }
        }
        if (this.foodLevel != serverPlayer.getFoodData().getFoodLevel()) {
            this.foodLevel = serverPlayer.getFoodData().getFoodLevel();
            if (sendNow) {
                message.setFoodLevel(this.foodLevel);
            }
        }
        if (this.strafeInput != serverPlayer.xxa) {
            this.strafeInput = serverPlayer.xxa;
            if (sendNow) {
                message.setStrafeInput(this.strafeInput);
            }
        }
        if (this.verticalInput != serverPlayer.yya) {
            this.verticalInput = serverPlayer.yya;
            if (sendNow) {
                message.setVerticalInput(this.verticalInput);
            }
        }
        if (this.forwardInput != serverPlayer.zza) {
            this.forwardInput = serverPlayer.zza;
            if (sendNow) {
                message.setForwardInput(this.forwardInput);
            }
        }
        boolean onCooldown = ShieldBlockCooldownEvent.isOnCooldown(serverPlayer);
        if (this.isShieldBlocking != onCooldown) {
            this.isShieldBlocking = onCooldown;
            if (sendNow) {
                message.setShieldBlockCooldown(this.isShieldBlocking);
            }
        }
        if (sendNow) {
            trySendSync(serverPlayer);
        }
    }

    public void syncEffectAdded(ServerPlayer serverPlayer, MobEffect effect, int amplifier) {
        getOrCreateSyncMessage(serverPlayer, true).addEffect(effect, amplifier);
    }

    public void syncEffectRemoved(ServerPlayer serverPlayer, MobEffect effect) {
        getOrCreateSyncMessage(serverPlayer, true).removeEffect(effect);
    }

    public void syncModelSwitch(ServerPlayer serverPlayer, boolean sendNow, String modelId) {
        if (!StringUtils.isEmpty(modelId) || !StringUtils.isEmpty(this.syncedModelId)) {
            this.syncedModelId = modelId;
            getOrCreateSyncMessage(serverPlayer, sendNow).setModelSwitch(modelId);
            if (sendNow) {
                trySendSync(serverPlayer);
            }
        }
    }

    public void syncMolangVars(ServerPlayer serverPlayer, boolean sendNow, int hashId, Object2FloatMap<String> variables) {
        if (!this.dirty && sendNow) {
            getOrCreateSyncMessage(serverPlayer, true).setMolangVars(hashId, variables);
            trySendSync(serverPlayer);
        }
    }

    public S2CSyncPlayerStatePacket buildFullSyncMessage(ServerPlayer serverPlayer, boolean resetMessage) {
        if (resetMessage) {
            this.syncMessage.reset(serverPlayer.getId());
        }
        S2CSyncPlayerStatePacket message = new S2CSyncPlayerStatePacket(serverPlayer.getId());
        message.markFullSync();
        message.setFlying(serverPlayer.getAbilities().flying);
        message.setExperienceLevel(serverPlayer.experienceLevel);
        message.setFoodLevel(serverPlayer.getFoodData().getFoodLevel());
        Collection<MobEffectInstance> activeEffects = serverPlayer.getActiveEffects();
        if (activeEffects.isEmpty()) {
            message.setEffects(Object2ByteMaps.emptyMap());
        } else if (activeEffects.size() == 1) {
            MobEffectInstance instance = activeEffects.iterator().next();
            message.setEffects(Object2ByteMaps.singleton(instance.getEffect(), (byte) (instance.getAmplifier() + 1)));
        } else {
            MobEffect[] effectIds = new MobEffect[activeEffects.size()];
            byte[] amplifiers = new byte[activeEffects.size()];
            int i = 0;
            for (MobEffectInstance instance : activeEffects) {
                effectIds[i] = instance.getEffect();
                amplifiers[i] = (byte) (instance.getAmplifier() + 1);
                i++;
            }
            message.setEffects((Object2ByteMap<MobEffect>) new Object2ByteArrayMap(effectIds, amplifiers));
        }
        message.setHealth((int) serverPlayer.getHealth());
        message.setMaxHealth((int) serverPlayer.getMaxHealth());
        message.setStrafeInput(serverPlayer.xxa);
        message.setVerticalInput(serverPlayer.yya);
        message.setForwardInput(serverPlayer.zza);
        if (ShieldBlockCooldownEvent.isOnCooldown(serverPlayer)) {
            message.setShieldBlockCooldown(true);
        }
        message.setModelSwitch(this.syncedModelId);
        return message;
    }
}
