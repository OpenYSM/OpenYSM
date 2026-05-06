package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class ReplacePlayerHandRenderEvent {
    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        if (!YesSteveModel.isAvailable() || GeneralConfig.DISABLE_SELF_MODEL.get() || GeneralConfig.DISABLE_SELF_HANDS.get()) {
            return;
        }
        Player player = event.getPlayer();
        if (!(player instanceof LocalPlayer localPlayer)) {
            return;
        }
        localPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
            if (!cap.isModelActive()) {
                return;
            }
            HumanoidArm arm = event.getArm();
            ModelAssembly context = cap.getModelAssembly();
            if (context == null || !hasArmBone(arm, context.getAnimationBundle().getArmModel())) {
                return;
            }
            RendererManager.getHandRenderer().renderHandItem(localPlayer, context, cap, arm, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), Minecraft.getInstance().getPartialTick());
            event.setCanceled(true);
        });
    }

    private static boolean hasArmBone(HumanoidArm humanoidArm, GeoModel meshData) {
        if (humanoidArm == HumanoidArm.LEFT) {
            return meshData.hasCustomLeftHand;
        }
        return meshData.hasCustomRightHand;
    }
}