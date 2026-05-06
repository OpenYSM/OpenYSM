package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.resource.models.ModelProperties;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouMaidCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Supplier;

public class C2SPlayAnimationPacket {

    private final int animationIndex;

    private final String category;

    private final int entityId;

    public C2SPlayAnimationPacket(int animationIndex, String category, int entityId) {
        this.animationIndex = animationIndex;
        this.category = category;
        this.entityId = entityId;
    }

    public C2SPlayAnimationPacket(int animationIndex, String category) {
        this(animationIndex, category, -1);
    }

    public static C2SPlayAnimationPacket createDefault() {
        return new C2SPlayAnimationPacket(-1, StringPool.EMPTY);
    }

    public static C2SPlayAnimationPacket createWithIndex(int entityId) {
        return new C2SPlayAnimationPacket(-1, StringPool.EMPTY, entityId);
    }

    public static void encode(C2SPlayAnimationPacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.animationIndex);
        buf.writeUtf(message.category);
        buf.writeVarInt(message.entityId);
    }

    public static C2SPlayAnimationPacket decode(FriendlyByteBuf buf) {
        return new C2SPlayAnimationPacket(buf.readVarInt(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(C2SPlayAnimationPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
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

    private static void handleCapability(C2SPlayAnimationPacket message, ServerPlayer sender) {
        if (message.entityId != -1) {
            Entity entity = sender.serverLevel().getEntity(message.entityId);
            if (TouhouMaidCompat.isMaidEntity(entity)) {
                TouhouMaidCompat.registerAnimationRoulette(entity, message.category, message.animationIndex);
                return;
            }
            return;
        }

        sender.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelInfoCap -> {
            if (message.animationIndex == -1) {
                modelInfoCap.stopAnimation(sender);
            } else {
                ServerModelManager.getModelDefinition(modelInfoCap.getModelId()).ifPresent(serverModelCap -> {
                    OrderedStringMap<String, String> extraAnimations;
                    ModelProperties modelProperties = serverModelCap.getLoadedModelData().getModelProperties();
                    Map<String, OrderedStringMap<String, String>> extraAnimationClassify = modelProperties.getExtraAnimationClassify();
                    if (StringUtils.isNotBlank(message.category) && extraAnimationClassify.containsKey(message.category)) {
                        extraAnimations = extraAnimationClassify.get(message.category);
                    } else {
                        extraAnimations = modelProperties.getExtraAnimation();
                    }
                    if (extraAnimations.size() > message.animationIndex) {
                        modelInfoCap.playAnimation(sender, extraAnimations.getKeyAt(message.animationIndex));
                    }
                });
            }
        });
    }
}