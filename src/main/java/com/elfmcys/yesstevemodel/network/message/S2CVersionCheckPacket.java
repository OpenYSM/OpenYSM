package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CVersionCheckPacket {

    private final String version;

    public S2CVersionCheckPacket() {
        this(NetworkHandler.VERSION);
    }

    private S2CVersionCheckPacket(String version) {
        this.version = version;
    }

    public static S2CVersionCheckPacket decode(FriendlyByteBuf buf) {
        return new S2CVersionCheckPacket(buf.readUtf());
    }

    public static void encode(S2CVersionCheckPacket message, FriendlyByteBuf buf) {
        buf.writeUtf(message.version);
    }

    public static void handle(S2CVersionCheckPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (NetworkHandler.setChannelVersion(context.getNetworkManager(), message.version)) {
            context.enqueueWork(() -> ClientModelManager.onSyncConnected());
        }
        NetworkHandler.CHANNEL.reply(new C2SVersionCheckPacket(), context);
        context.setPacketHandled(true);
    }
}