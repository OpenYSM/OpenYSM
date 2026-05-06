package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MobEffectEvent {
    @SubscribeEvent
    public static void onEffectAdded(net.minecraftforge.event.entity.living.MobEffectEvent.Added event) {
        if (!YesSteveModel.isAvailable() || event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            if (event.getEffectInstance().getEffect() != null) {
                MobEffectInstance effectInstance = event.getEffectInstance();
                serverPlayer.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                    cap.getAnimSync().syncEffectAdded(serverPlayer, effectInstance.getEffect(), effectInstance.getAmplifier() + 1);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(net.minecraftforge.event.entity.living.MobEffectEvent.Remove event) {
        if (!YesSteveModel.isAvailable() || event.getEntity().level().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            if (event.getEffect() != null) {
                serverPlayer.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                    cap.getAnimSync().syncEffectRemoved(serverPlayer, event.getEffect());
                });
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(net.minecraftforge.event.entity.living.MobEffectEvent.Expired event) {
        if (!YesSteveModel.isAvailable() || event.getEntity().level().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            if (event.getEffectInstance() != null && event.getEffectInstance().getEffect() != null) {
                serverPlayer.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                    cap.getAnimSync().syncEffectRemoved(serverPlayer, event.getEffectInstance().getEffect());
                });
            }
        }
    }
}