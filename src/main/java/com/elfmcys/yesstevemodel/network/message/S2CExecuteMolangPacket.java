package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouMaidCompat;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.molang.parser.ParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CExecuteMolangPacket {

    private final int[] entityIds;

    private final String expression;

    public S2CExecuteMolangPacket(int entityIds, String expression) {
        this.entityIds = new int[]{entityIds};
        this.expression = expression;
    }

    public S2CExecuteMolangPacket(int[] entityIds, String expression) {
        this.entityIds = entityIds;
        this.expression = expression;
    }

    public static void encode(S2CExecuteMolangPacket message, FriendlyByteBuf buf) {
        buf.writeVarIntArray(message.entityIds);
        buf.writeUtf(message.expression);
    }

    public static S2CExecuteMolangPacket decode(FriendlyByteBuf buf) {
        return new S2CExecuteMolangPacket(buf.readVarIntArray(), buf.readUtf());
    }

    public static void handle(S2CExecuteMolangPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                handleCapability(message);
            });
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleCapability(S2CExecuteMolangPacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        for (int i : message.entityIds) {
            Entity entity = minecraft.level.getEntity(i);
            if (entity instanceof Player) {
                entity.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                    try {
                        cap.executeExpression(GeckoLibCache.parseSimpleExpression(message.expression), true, false, null);
                    } catch (ParseException e) {
                        YesSteveModel.LOGGER.error("Failed to execute molang " + message.expression, e);
                    }
                });
            } else if (TouhouMaidCompat.isMaidEntity(entity)) {
                TouhouMaidCompat.playMaidAnimation(entity, message.expression);
            }
        }
    }
}