package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.network.NetworkHandler;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSyncAnimationExpressionPacket {

    private final FloatArrayList floatData;

    public C2SSyncAnimationExpressionPacket(FloatArrayList floatData) {
        this.floatData = floatData;
    }

    public static void encode(C2SSyncAnimationExpressionPacket message, FriendlyByteBuf buf) {
        buf.writeByte(message.floatData.size());
        for (Float floatDatum : message.floatData) {
            buf.writeFloat(floatDatum);
        }
    }

    public static C2SSyncAnimationExpressionPacket decode(FriendlyByteBuf buf) {
        int size = buf.readByte();
        FloatArrayList floatArrayList = new FloatArrayList(size);
        for (int i = 0; i < size; i++) {
            floatArrayList.add(buf.readFloat());
        }
        return new C2SSyncAnimationExpressionPacket(floatArrayList);
    }

    public static void handle(C2SSyncAnimationExpressionPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (context.getDirection().getReceptionSide().isServer() && sender != null) {
            context.enqueueWork(() -> NetworkHandler.sendToTrackingEntityAndSelf(new S2CSyncAnimationExpressionPacket(sender.getId(), message.floatData), sender));
        }
        context.setPacketHandled(true);
    }
}