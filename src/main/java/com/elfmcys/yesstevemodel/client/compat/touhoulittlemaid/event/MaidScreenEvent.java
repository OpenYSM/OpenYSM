package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.event;

import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapabilityProvider;
import com.elfmcys.yesstevemodel.client.gui.TouhouMaidModelScreen;
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.event.OpenYsmMaidScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public final class MaidScreenEvent {
    @SubscribeEvent
    public void onOpenMaidScreen(OpenYsmMaidScreenEvent event) {
        if (event.getMaid().getCapability(MaidCapabilityProvider.MAID_CAP).isPresent()) {
            Minecraft.getInstance().setScreen(new TouhouMaidModelScreen(event.getMaid()));
        }
    }
}