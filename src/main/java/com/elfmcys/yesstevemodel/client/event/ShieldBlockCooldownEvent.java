package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = YesSteveModel.MOD_ID)
public class ShieldBlockCooldownEvent {

    public static final String TAG_KEY = "ysm$shield_block_cooldown";

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        event.getEntity().getPersistentData().putInt(TAG_KEY, 5);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getPersistentData().contains(TAG_KEY)) {
            int i = entity.getPersistentData().getInt(TAG_KEY);
            if (i > 0) {
                entity.getPersistentData().putInt(TAG_KEY, i - 1);
            } else {
                entity.getPersistentData().remove(TAG_KEY);
            }
        }
    }

    public static boolean isOnCooldown(LivingEntity livingEntity) {
        return livingEntity.getPersistentData().contains(TAG_KEY);
    }
}