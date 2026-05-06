package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class PlayerSkinTextureManager {

    private static final ResourceLocation STEVE_SKIN = ResourceLocation.parse("textures/entity/player/wide/steve.png");

    private static final ResourceLocation ALEX_SKIN = ResourceLocation.parse("textures/entity/player/slim/alex.png");

    private static final String STEVE_TEXTURE_ID = "misc/2_steve";

    private static final String ALEX_TEXTURE_ID = "misc/1_alex";

    @SubscribeEvent
    public static void onRenderTexture(SpecialPlayerRenderEvent event) {
        ResourceLocation location;
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        Player player = event.getPlayer();
        if (isDefaultSkin(event.getModelId()) && (player instanceof AbstractClientPlayer abstractClientPlayer)) {
            Minecraft minecraft = Minecraft.getInstance();
            Map insecureSkinInformation = minecraft.getSkinManager().getInsecureSkinInformation(abstractClientPlayer.getGameProfile());
            if (insecureSkinInformation.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                location = minecraft.getSkinManager().registerTexture((MinecraftProfileTexture) insecureSkinInformation.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            } else {
                location = getSkinTexture(event.getModelId());
            }
            event.setTextureLocation(location);
        }
    }

    private static boolean isDefaultSkin(String str) {
        return str.equals(STEVE_TEXTURE_ID) || str.equals(ALEX_TEXTURE_ID);
    }

    private static ResourceLocation getSkinTexture(String str) {
        return str.equals(STEVE_TEXTURE_ID) ? STEVE_SKIN : ALEX_SKIN;
    }
}