package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.network.message.S2CSyncPlayerStatePacket;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class PlayerEntityFrameState extends LivingEntityFrameState<Player> {

    private final boolean isLocalPlayer;

    private final Object2ByteOpenHashMap<MobEffect> effectAmplifiers;

    private boolean isFlying;

    private int experienceLevel;

    private int health;

    private int maxHealth;

    private int foodLevel;

    private float strafeInput;

    private float verticalInput;

    private float forwardInput;

    private boolean isShieldBlocking;

    private static float headYawDelta;

    private static float lastYRot;

    public PlayerEntityFrameState(Player player, boolean isLocalPlayer) {
        super(player);
        this.isLocalPlayer = isLocalPlayer;
        this.effectAmplifiers = new Object2ByteOpenHashMap<>(8);
    }

    @Override
    public void reset() {
        super.reset();
        this.effectAmplifiers.clear();
        this.isFlying = false;
        this.experienceLevel = 0;
        this.health = 0;
        this.maxHealth = 0;
        this.foodLevel = 0;
        this.strafeInput = 0.0f;
        this.verticalInput = 0.0f;
        this.forwardInput = 0.0f;
        this.isShieldBlocking = false;
    }

    public void applySyncMessage(S2CSyncPlayerStatePacket message) {
        if ((message.flags & 2) != 0) {
            this.isFlying = message.isFlying;
        }
        if ((message.flags & 4) != 0) {
            if (message.isFullSync()) {
                this.effectAmplifiers.clear();
            }
            this.effectAmplifiers.putAll(message.effectAmplifiers);
        }
        if ((message.flags & 8) != 0) {
            this.experienceLevel = message.experienceLevel;
        }
        if ((message.flags & 16) != 0) {
            this.foodLevel = message.foodLevel;
        }
        if ((message.flags & 32) != 0) {
            this.health = message.health;
        }
        if ((message.flags & 64) != 0) {
            this.maxHealth = message.maxHealth;
        }
        if ((message.flags & 128) != 0) {
            this.strafeInput = message.strafeInput / 127.0f;
        }
        if ((message.flags & 256) != 0) {
            this.verticalInput = message.verticalInput / 127.0f;
        }
        if ((message.flags & 512) != 0) {
            this.forwardInput = message.forwardInput / 127.0f;
        }
        if ((message.flags & 1024) != 0) {
            this.isShieldBlocking = message.shieldBlockCooldown;
        }
    }

    public boolean isFlying() {
        if (this.isLocalPlayer) {
            return this.entity.getAbilities().flying;
        }
        return this.isFlying;
    }

    public int getExperienceLevel() {
        return this.experienceLevel;
    }

    public int getHealth() {
        return this.health;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public float getStrafeInput() {
        return this.strafeInput;
    }

    public float getVerticalInput() {
        return this.verticalInput;
    }

    public float getForwardInput() {
        return this.forwardInput;
    }

    public boolean isShieldBlocking() {
        return this.isShieldBlocking;
    }

    public byte getEffectAmplifier(MobEffect mobEffect) {
        if (this.isLocalPlayer) {
            MobEffectInstance effect = this.entity.getEffect(mobEffect);
            if (effect != null) {
                return (byte) (effect.getAmplifier() + 1);
            }
            return (byte) 0;
        }
        return this.effectAmplifiers.getOrDefault(mobEffect, (byte) 0);
    }

    @Override
    public void onTickUpdate(int currentTick, int previousTick) {
        if (this.isLocalPlayer) {
            updateHeadYaw(this.entity, currentTick, previousTick);
        }
        super.onTickUpdate(currentTick, previousTick);
    }

    private static void updateHeadYaw(Player player, int currentTick, int previousTick) {
        float yRot = player.getYRot();
        if (previousTick > 0) {
            headYawDelta = ((yRot - lastYRot) * 20.0f) / (currentTick - previousTick);
        }
        lastYRot = yRot;
    }

    public static float getHeadYawDelta() {
        return headYawDelta;
    }
}