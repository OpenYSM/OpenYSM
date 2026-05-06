package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.event.EntityJoinCallbackEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSetModelAndTexturePacket {

    private final int entityId;

    private final String modelId;

    private final String textureId;

    private final boolean disabled;

    private final S2CSyncPlayerStatePacket entityModelSync;

    public S2CSetModelAndTexturePacket(int i, String str, String str2, boolean z, S2CSyncPlayerStatePacket playerState) {
        this.entityId = i;
        this.modelId = str;
        this.textureId = str2;
        this.entityModelSync = playerState;
        this.disabled = z;
    }

    public static void encode(S2CSetModelAndTexturePacket other, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(other.entityId);
        friendlyByteBuf.writeUtf(other.modelId);
        friendlyByteBuf.writeUtf(other.textureId);
        friendlyByteBuf.writeBoolean(other.disabled);
        S2CSyncPlayerStatePacket.encode(other.entityModelSync, friendlyByteBuf);
    }

    public static S2CSetModelAndTexturePacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new S2CSetModelAndTexturePacket(friendlyByteBuf.readVarInt(), friendlyByteBuf.readUtf(), friendlyByteBuf.readUtf(), friendlyByteBuf.readBoolean(), S2CSyncPlayerStatePacket.decode(friendlyByteBuf));
    }

    public static void handle(S2CSetModelAndTexturePacket other, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            EntityJoinCallbackEvent.addCallback(other.entityId, entity -> {
                applyOnClient(entity, other);
            });
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void applyOnClient(Entity entity, S2CSetModelAndTexturePacket other) {
        entity.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
            cap.initModelWithTexture(other.modelId, other.textureId);
            cap.setForceDisabled(other.disabled);
            S2CSyncPlayerStatePacket.handleCapability(entity, other.entityModelSync);
        });
    }
}