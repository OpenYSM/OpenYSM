package com.elfmcys.yesstevemodel.client.compat.jade;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.VehicleCapabilityProvider;
import com.elfmcys.yesstevemodel.util.FileTypeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class JadeWailaPlugin implements IWailaPlugin {
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(new ModelInfoComponentProvider(), Entity.class);
    }

    private static class ModelInfoComponentProvider implements IEntityComponentProvider {

        private static final ResourceLocation RESOURCE_ID = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "model_info");

        private ModelInfoComponentProvider() {
        }

        public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
            Entity entity = entityAccessor.getEntity();
            if (entity instanceof Player) {
                entity.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                    if (cap.isModelActive()) {
                        iTooltip.add(Component.translatable("top.yes_steve_model.model_info.id").append(cap.getModelAssembly().getDisplayName(FileTypeUtil.getNameWithoutArchiveExtension(cap.getModelId()))));
                    }
                });
            } else {
                entityAccessor.getEntity().getCapability(VehicleCapabilityProvider.VEHICLE_CAP).ifPresent(cap -> {
                    if (cap.isModelInitialized() && cap.isModelReady()) {
                        iTooltip.add(Component.translatable("top.yes_steve_model.model_info.id").append(cap.getModelAssembly().getDisplayName(FileTypeUtil.getNameWithoutArchiveExtension(cap.getModelId()))));
                    }
                });
            }
        }

        public ResourceLocation getUid() {
            return RESOURCE_ID;
        }
    }
}