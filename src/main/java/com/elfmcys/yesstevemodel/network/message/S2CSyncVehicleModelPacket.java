package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.ClientLazyCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.VehicleModelCapability;
import com.elfmcys.yesstevemodel.event.EntityJoinCallbackEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.capability.VehicleCapability;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncVehicleModelPacket {

    private final int entityId;

    private final VehicleModelCapability capability;

    private final Int2FloatOpenHashMap floatMap;

    public S2CSyncVehicleModelPacket(int entityId, VehicleModelCapability capability, Int2FloatOpenHashMap floatMap) {
        this.entityId = entityId;
        this.capability = capability;
        this.floatMap = floatMap;
    }

    public S2CSyncVehicleModelPacket(int entityId, VehicleModelCapability capability) {
        this(entityId, capability, new Int2FloatOpenHashMap(0));
    }

    public static void encode(S2CSyncVehicleModelPacket message, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(message.entityId);
        friendlyByteBuf.writeNbt(message.capability.serializeNBT());
    }

    public static S2CSyncVehicleModelPacket decode(FriendlyByteBuf buf) {
        int varInt = buf.readVarInt();
        CompoundTag nbt = buf.readNbt();
        VehicleModelCapability cap = new VehicleModelCapability();
        if (nbt != null) {
            cap.deserializeNBT(nbt);
        }
        Object2FloatOpenHashMap<String> objectMap = cap.getMolangVars();
        Int2FloatOpenHashMap floatMap = new Int2FloatOpenHashMap();
        objectMap.object2FloatEntrySet().fastForEach(entry -> floatMap.put(StringPool.computeIfAbsent(entry.getKey()), entry.getFloatValue()));
        return new S2CSyncVehicleModelPacket(varInt, cap, floatMap);
    }

    public static void handle(S2CSyncVehicleModelPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            EntityJoinCallbackEvent.addCallback(message.entityId, entity -> handleCapability(entity, message.capability, message.floatMap));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleCapability(Entity entity, VehicleModelCapability capability, Int2FloatOpenHashMap floatMap) {
        entity.getCapability(ClientLazyCapabilityProvider.CLIENT_LAZY_CAP).ifPresent(cap -> {
            VehicleCapability vehicleCapability = cap.getEntityRenderProvider().getOrCreateCapability();
            vehicleCapability.setOwnerModelId(capability.getOwnerModelId());
            vehicleCapability.setFloatMap(floatMap);
        });
    }
}