package com.elfmcys.yesstevemodel.network;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.network.message.*;
import io.netty.util.AttributeKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class NetworkHandler {

    public static final String VERSION = "2.6.0";

    public static final ResourceLocation CHANNEL_ID = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, VERSION.replace('.', '_'));

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_ID, () -> VERSION, str -> true, str2 -> true);

    private static final AttributeKey<String> CHANNEL_VERSION_KEY = AttributeKey.valueOf("yes_steve_model_channel_version");

    public static boolean setChannelVersion(Connection connection, String str) {
        return connection.channel().attr(CHANNEL_VERSION_KEY).compareAndSet(null, str);
    }

    public static boolean isPlayerConnected(ServerPlayer serverPlayer) {
        return serverPlayer.connection != null && isConnectionValid(serverPlayer.connection.connection);
    }

    public static boolean isClientConnected() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return false;
        }
        return isConnectionValid(connection.getConnection());
    }

    public static boolean isConnectionValid(@Nullable Connection connection) {
        return connection != null && connection.channel() != null && VERSION.equals(connection.channel().attr(CHANNEL_VERSION_KEY).get());
    }

    public static void init() {
        CHANNEL.registerMessage(1, S2CModelSyncPayload.class, S2CModelSyncPayload::encode, S2CModelSyncPayload::decode, S2CModelSyncPayload::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(2, C2SModelSyncPayload.class, C2SModelSyncPayload::encode, C2SModelSyncPayload::decode, C2SModelSyncPayload::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(3, S2CExecuteMolangPacket.class, S2CExecuteMolangPacket::encode, S2CExecuteMolangPacket::decode, S2CExecuteMolangPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(4, S2CSetModelAndTexturePacket.class, S2CSetModelAndTexturePacket::encode, S2CSetModelAndTexturePacket::decode, S2CSetModelAndTexturePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(5, C2SRequestSwitchModelPacket.class, C2SRequestSwitchModelPacket::encode, C2SRequestSwitchModelPacket::decode, C2SRequestSwitchModelPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(6, S2CSyncAuthModelsPacket.class, S2CSyncAuthModelsPacket::encode, S2CSyncAuthModelsPacket::decode, S2CSyncAuthModelsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(7, C2SPlayAnimationPacket.class, C2SPlayAnimationPacket::encode, C2SPlayAnimationPacket::decode, C2SPlayAnimationPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(8, S2CSyncStarModelsPacket.class, S2CSyncStarModelsPacket::encode, S2CSyncStarModelsPacket::decode, S2CSyncStarModelsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(9, C2SSetStarModelPacket.class, C2SSetStarModelPacket::encode, C2SSetStarModelPacket::decode, C2SSetStarModelPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(15, C2SCompleteFeedbackPacket.class, C2SCompleteFeedbackPacket::encode, C2SCompleteFeedbackPacket::decode, C2SCompleteFeedbackPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(16, S2CSyncProjectileModelPacket.class, S2CSyncProjectileModelPacket::encode, S2CSyncProjectileModelPacket::decode, S2CSyncProjectileModelPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(17, C2SRequestExecuteMolangPacket.class, C2SRequestExecuteMolangPacket::encode, C2SRequestExecuteMolangPacket::decode, C2SRequestExecuteMolangPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(18, C2SSyncAnimationExpressionPacket.class, C2SSyncAnimationExpressionPacket::encode, C2SSyncAnimationExpressionPacket::decode, C2SSyncAnimationExpressionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(19, S2CSyncAnimationExpressionPacket.class, S2CSyncAnimationExpressionPacket::encode, S2CSyncAnimationExpressionPacket::decode, S2CSyncAnimationExpressionPacket::handleCapability, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(21, S2CSyncPlayerStatePacket.class, S2CSyncPlayerStatePacket::encode, S2CSyncPlayerStatePacket::decode, S2CSyncPlayerStatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(22, S2CSyncVehicleModelPacket.class, S2CSyncVehicleModelPacket::encode, S2CSyncVehicleModelPacket::decode, S2CSyncVehicleModelPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(23, C2SSwingArmPacket.class, C2SSwingArmPacket::encode, C2SSwingArmPacket::decode, C2SSwingArmPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(51, S2CVersionCheckPacket.class, S2CVersionCheckPacket::encode, S2CVersionCheckPacket::decode, S2CVersionCheckPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(52, C2SVersionCheckPacket.class, C2SVersionCheckPacket::encode, C2SVersionCheckPacket::decode, C2SVersionCheckPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sendToServer(Object obj) {
        if (isClientConnected()) {
            CHANNEL.sendToServer(obj);
        }
    }

    public static void sendToClientPlayer(Object obj, Player player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), obj);
    }

    public static void sendToAll(Object obj) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), obj);
    }

    public static void sendToTrackingEntity(Object obj, Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), obj);
    }

    public static void sendToTrackingEntityAndSelf(Object obj, Player player) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), obj);
    }
}