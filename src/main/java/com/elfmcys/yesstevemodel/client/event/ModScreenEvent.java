package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.DebugAnimationScreen;
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = YesSteveModel.MOD_ID, value = {Dist.CLIENT})
public class ModScreenEvent {

    private static final String IMC_METHOD = "DownloadScreen";

    @Nullable
    private static Screen receivedScreen;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onProcessIMC(InterModProcessEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        InterModComms.getMessages(YesSteveModel.MOD_ID).findFirst().ifPresent(message -> {
            if (IMC_METHOD.equals(message.method())) {
                Object screenObj = message.messageSupplier().get();
                if (screenObj instanceof Screen screen) {
                    receivedScreen = screen;
                }
            }
        });
    }

    public static void openScreen(PlayerModelScreen modelScreen) {
        modelScreen.getMinecraft().setScreen(Objects.requireNonNullElseGet(receivedScreen, () -> {
            return new DebugAnimationScreen(modelScreen);
        }));
    }
}