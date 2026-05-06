package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class S2CModelSyncPayload {

    private final ByteBuffer data;

    public S2CModelSyncPayload(ByteBuffer data) {
        this.data = data;
    }

    public static void encode(S2CModelSyncPayload message, FriendlyByteBuf buf) {
        buf.writeBytes(message.data);
    }

    public static S2CModelSyncPayload decode(FriendlyByteBuf buf) {
        ByteBuffer data = ByteBuffer.allocateDirect(buf.readableBytes());
        buf.readBytes(data);
        return new S2CModelSyncPayload(data);
    }

    public static void handle(S2CModelSyncPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            ClientModelManager.startSync(context.getNetworkManager(), message.data);
        }
        context.setPacketHandled(true);
    }
}