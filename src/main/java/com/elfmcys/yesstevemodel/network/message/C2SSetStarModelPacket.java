package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSetStarModelPacket {

    private final String modelId;

    private final boolean isAdd;

    private C2SSetStarModelPacket(String modelId, boolean isAdd) {
        this.modelId = modelId;
        this.isAdd = isAdd;
    }

    public static C2SSetStarModelPacket add(String modelId) {
        return new C2SSetStarModelPacket(modelId, true);
    }

    public static C2SSetStarModelPacket remove(String modelId) {
        return new C2SSetStarModelPacket(modelId, false);
    }

    public static void encode(C2SSetStarModelPacket message, FriendlyByteBuf buf) {
        buf.writeUtf(message.modelId);
        buf.writeBoolean(message.isAdd);
    }

    public static C2SSetStarModelPacket decode(FriendlyByteBuf buf) {
        return new C2SSetStarModelPacket(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(C2SSetStarModelPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                if (sender == null) {
                    return;
                }
                handleCapability(message, sender);
            });
        }
        context.setPacketHandled(true);
    }

    private static void handleCapability(C2SSetStarModelPacket message, ServerPlayer sender) {
        sender.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(cap -> {
            if (message.isAdd) {
                cap.addModel(message.modelId);
            } else {
                cap.removeModel(message.modelId);
            }
        });
    }
}