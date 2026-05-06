package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncAnimationExpressionPacket {

    private final int entityId;

    private final FloatArrayList floatData;

    public S2CSyncAnimationExpressionPacket(int entityId, FloatArrayList floatData) {
        this.entityId = entityId;
        this.floatData = floatData;
    }

    public static void encode(S2CSyncAnimationExpressionPacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.entityId);
        buf.writeByte(message.floatData.size());
        for (Float floatDatum : message.floatData) {
            buf.writeFloat(floatDatum);
        }
    }

    public static S2CSyncAnimationExpressionPacket decode(FriendlyByteBuf buf) {
        int varInt = buf.readVarInt();
        int i = buf.readByte();
        FloatArrayList floatArrayList = new FloatArrayList(i);
        for (int i2 = 0; i2 < i; i2++) {
            floatArrayList.add(buf.readFloat());
        }
        return new S2CSyncAnimationExpressionPacket(varInt, floatArrayList);
    }

    public static void handleCapability(S2CSyncAnimationExpressionPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                Minecraft.getInstance().level.getEntity(message.entityId).getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> cap.executeAnimationExpression(message.floatData));
            });
        }
        context.setPacketHandled(true);
    }
}