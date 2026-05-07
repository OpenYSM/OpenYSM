package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SSetStarModelPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ModIconButton extends FlatColorButton {

    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");

    public ModIconButton(int x, int y) {
        super(x, y, 20, 20, Component.empty(), button -> {
        });
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        int iconOffsetX = (this.width - 16) / 2;
        int iconOffsetY = (this.height - 16) / 2;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            localPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                localPlayer.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(cap2 -> {
                    if (cap2.containsModel(cap.getModelId())) {
                        guiGraphics.blit(ICON_TEXTURE, getX() + iconOffsetX, getY() + iconOffsetY, 16, 16, 16.0f, 0.0f, 16, 16, 256, 256);
                    } else {
                        guiGraphics.blit(ICON_TEXTURE, getX() + iconOffsetX, getY() + iconOffsetY, 16, 16, 0.0f, 0.0f, 16, 16, 256, 256);
                    }
                });
            });
        }
    }

    public void onPress() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            localPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).ifPresent(cap -> {
                localPlayer.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(cap2 -> {
                    String str = cap.getModelId();
                    if (cap2.containsModel(str)) {
                        cap2.removeModel(str);
                        NetworkHandler.sendToServer(C2SSetStarModelPacket.remove(str));
                    } else {
                        cap2.addModel(str);
                        NetworkHandler.sendToServer(C2SSetStarModelPacket.add(str));
                    }
                });
            });
        }
    }
}