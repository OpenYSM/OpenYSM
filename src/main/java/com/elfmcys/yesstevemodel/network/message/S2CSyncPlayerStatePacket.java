package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.event.EntityJoinCallbackEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import it.unimi.dsi.fastutil.ints.Int2FloatArrayMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.StringUtils;
import org.joml.Math;

import java.util.function.Supplier;

public class S2CSyncPlayerStatePacket {

    public int entityId;

    public short flags;

    public boolean isFlying;

    public Object2ByteMap<MobEffect> effectAmplifiers;

    public int experienceLevel;

    public int foodLevel;

    public int health;

    public int maxHealth;

    public byte strafeInput;

    public byte verticalInput;

    public byte forwardInput;

    public boolean shieldBlockCooldown;

    public String modelSwitchId;

    private int molangHashId;

    public Object2FloatMap<String> molangVars;

    public Int2FloatMap molangVarData;

    public S2CSyncPlayerStatePacket(int entityId) {
        this.entityId = entityId;
    }

    public boolean isEmpty() {
        return this.flags == 0;
    }

    public boolean isFullSync() {
        return (this.flags & 1) != 0;
    }

    public void markFullSync() {
        this.flags = (short) (this.flags | 1);
    }

    public void reset(int entityId) {
        this.entityId = entityId;
        this.flags = (short) 0;
        this.effectAmplifiers = null;
        this.molangVars = null;
    }

    public S2CSyncPlayerStatePacket setFlying(boolean isFlying) {
        this.flags = (short) (this.flags | 2);
        this.isFlying = isFlying;
        return this;
    }

    public S2CSyncPlayerStatePacket addEffect(MobEffect effect, int amplifier) {
        this.flags = (short) (this.flags | 4);
        if (this.effectAmplifiers == null) {
            this.effectAmplifiers = Object2ByteMaps.singleton(effect, (byte) amplifier);
        } else if (this.effectAmplifiers.size() == 1) {
            this.effectAmplifiers = new Object2ByteOpenHashMap(this.effectAmplifiers);
            this.effectAmplifiers.put(effect, (byte) amplifier);
        } else {
            this.effectAmplifiers.put(effect, (byte) amplifier);
        }
        return this;
    }

    public S2CSyncPlayerStatePacket setEffects(Object2ByteMap<MobEffect> effects) {
        this.flags = (short) (this.flags | 4);
        this.effectAmplifiers = effects;
        return this;
    }

    public S2CSyncPlayerStatePacket removeEffect(MobEffect effect) {
        addEffect(effect, 0);
        return this;
    }

    public S2CSyncPlayerStatePacket setExperienceLevel(int level) {
        this.flags = (short) (this.flags | 8);
        this.experienceLevel = level;
        return this;
    }

    public S2CSyncPlayerStatePacket setFoodLevel(int level) {
        this.flags = (short) (this.flags | 16);
        this.foodLevel = level;
        return this;
    }

    public S2CSyncPlayerStatePacket setHealth(int health) {
        this.flags = (short) (this.flags | 32);
        this.health = health;
        return this;
    }

    public S2CSyncPlayerStatePacket setMaxHealth(int maxHealth) {
        this.flags = (short) (this.flags | 64);
        this.maxHealth = maxHealth;
        return this;
    }

    public S2CSyncPlayerStatePacket setStrafeInput(float input) {
        this.flags = (short) (this.flags | 128);
        this.strafeInput = (byte) Math.round(Math.clamp(input, -1.0f, 1.0f) * 127.0f);
        return this;
    }

    public S2CSyncPlayerStatePacket setVerticalInput(float input) {
        this.flags = (short) (this.flags | 256);
        this.verticalInput = (byte) Math.round(Math.clamp(input, -1.0f, 1.0f) * 127.0f);
        return this;
    }

    public S2CSyncPlayerStatePacket setForwardInput(float input) {
        this.flags = (short) (this.flags | 512);
        this.forwardInput = (byte) Math.round(Math.clamp(input, -1.0f, 1.0f) * 127.0f);
        return this;
    }

    public S2CSyncPlayerStatePacket setShieldBlockCooldown(boolean onCooldown) {
        this.flags = (short) (this.flags | 1024);
        this.shieldBlockCooldown = onCooldown;
        return this;
    }

    public S2CSyncPlayerStatePacket setModelSwitch(String modelId) {
        this.flags = (short) (this.flags | 2048);
        this.modelSwitchId = modelId;
        return this;
    }

    public S2CSyncPlayerStatePacket setMolangVars(int hashId, Object2FloatMap<String> variables) {
        if (this.molangVars == null || this.molangHashId != hashId) {
            this.flags = (short) (this.flags | 4096);
            this.molangHashId = hashId;
            this.molangVars = new Object2FloatOpenHashMap<>(variables);
        } else {
            this.molangVars.putAll(variables);
        }
        return this;
    }

    public static void encode(S2CSyncPlayerStatePacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeShort(message.flags);
        short flags = message.flags;
        if ((flags & 2) != 0) {
            buffer.writeBoolean(message.isFlying);
        }
        if ((flags & 4) != 0) {
            buffer.writeVarInt(message.effectAmplifiers.size());
            Object2ByteMaps.fastForEach(message.effectAmplifiers, entry -> {
                buffer.writeId(BuiltInRegistries.MOB_EFFECT, entry.getKey());
                buffer.writeByte(entry.getByteValue());
            });
        }
        if ((flags & 8) != 0) {
            buffer.writeVarInt(message.experienceLevel);
        }
        if ((flags & 16) != 0) {
            buffer.writeVarInt(message.foodLevel);
        }
        if ((flags & 32) != 0) {
            buffer.writeVarInt(message.health);
        }
        if ((flags & 64) != 0) {
            buffer.writeVarInt(message.maxHealth);
        }
        if ((flags & 128) != 0) {
            buffer.writeByte(message.strafeInput);
        }
        if ((flags & 256) != 0) {
            buffer.writeByte(message.verticalInput);
        }
        if ((flags & 512) != 0) {
            buffer.writeByte(message.forwardInput);
        }
        if ((flags & 1024) != 0) {
            buffer.writeBoolean(message.shieldBlockCooldown);
        }
        if ((flags & 2048) != 0) {
            buffer.writeUtf(message.modelSwitchId);
        }
        if ((flags & 4096) != 0) {
            buffer.writeInt(message.molangHashId);
            buffer.writeVarInt(message.molangVars.size());
            Object2FloatMaps.fastForEach(message.molangVars, entry -> {
                buffer.writeUtf(entry.getKey());
                buffer.writeFloat(entry.getFloatValue());
            });
        }
    }

    public static S2CSyncPlayerStatePacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        short flags = buffer.readShort();
        S2CSyncPlayerStatePacket message = new S2CSyncPlayerStatePacket(entityId);
        message.flags = flags;
        if ((flags & 2) != 0) {
            message.isFlying = buffer.readBoolean();
        }
        if ((flags & 4) != 0) {
            int effectCount = buffer.readVarInt();
            if (effectCount == 0) {
                message.effectAmplifiers = Object2ByteMaps.emptyMap();
            } else if (effectCount == 1) {
                message.effectAmplifiers = Object2ByteMaps.singleton(buffer.readById(BuiltInRegistries.MOB_EFFECT), buffer.readByte());
            } else {
                MobEffect[] effects = new MobEffect[effectCount];
                byte[] amplifiers = new byte[effectCount];
                for (int i = 0; i < effectCount; i++) {
                    effects[i] = buffer.readById(BuiltInRegistries.MOB_EFFECT);
                    amplifiers[i] = buffer.readByte();
                }
                message.effectAmplifiers = new Object2ByteArrayMap<>(effects, amplifiers);
            }
        }
        if ((flags & 8) != 0) {
            message.experienceLevel = buffer.readVarInt();
        }
        if ((flags & 16) != 0) {
            message.foodLevel = buffer.readVarInt();
        }
        if ((flags & 32) != 0) {
            message.health = buffer.readVarInt();
        }
        if ((flags & 64) != 0) {
            message.maxHealth = buffer.readVarInt();
        }
        if ((flags & 128) != 0) {
            message.strafeInput = buffer.readByte();
        }
        if ((flags & 256) != 0) {
            message.verticalInput = buffer.readByte();
        }
        if ((flags & 512) != 0) {
            message.forwardInput = buffer.readByte();
        }
        if ((flags & 1024) != 0) {
            message.shieldBlockCooldown = buffer.readBoolean();
        }
        if ((flags & 2048) != 0) {
            message.modelSwitchId = buffer.readUtf();
        }
        if ((flags & 4096) != 0) {
            message.molangHashId = buffer.readInt();
            int varCount = buffer.readVarInt();
            if (message.isFullSync()) {
                Int2FloatOpenHashMap roamingVars = new Int2FloatOpenHashMap(varCount);
                message.molangVarData = roamingVars;
                for (int i = 0; i < varCount; i++) {
                    roamingVars.put(StringPool.computeIfAbsent(buffer.readUtf()), buffer.readFloat());
                }
            } else if (varCount == 0) {
                message.molangVarData = Int2FloatMaps.EMPTY_MAP;
            } else if (varCount == 1) {
                message.molangVarData = Int2FloatMaps.singleton(StringPool.computeIfAbsent(buffer.readUtf()), buffer.readFloat());
            } else {
                int[] keys = new int[varCount];
                float[] values = new float[varCount];
                for (int i = 0; i < varCount; i++) {
                    keys[i] = StringPool.computeIfAbsent(buffer.readUtf());
                    values[i] = buffer.readFloat();
                }
                message.molangVarData = new Int2FloatArrayMap(keys, values);
            }
        }
        return message;
    }

    public static void handle(S2CSyncPlayerStatePacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            EntityJoinCallbackEvent.addCallback(message.entityId, entity -> handleCapability(entity, message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleCapability(Entity entity, S2CSyncPlayerStatePacket message) {
        if (entity instanceof Player) {
            entity.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                if ((message.flags & 2048) != 0) {
                    if (!StringUtils.isEmpty(message.modelSwitchId)) {
                        cap.requestModelSwitch(message.modelSwitchId);
                    } else {
                        cap.clearModelSwitch();
                    }
                }
                if ((message.flags & 4096) != 0) {
                    if (message.isFullSync()) {
                        cap.updateMolangVars(message.molangHashId, (Int2FloatOpenHashMap) message.molangVarData);
                    } else {
                        cap.enqueueMolangDelta(message.molangHashId, message.molangVarData);
                    }
                }
                cap.getPositionTracker().applySyncMessage(message);
            });
        }
    }
}
