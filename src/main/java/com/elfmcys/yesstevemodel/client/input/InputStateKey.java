package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.util.InputUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = {Dist.CLIENT}, modid = YesSteveModel.MOD_ID)
public class InputStateKey {

    public static volatile boolean[] keyStates = new boolean[349];

    public static volatile boolean[] mouseStates = new boolean[8];

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (YesSteveModel.isAvailable() && InputUtil.isPlayerReady() && 32 <= event.getKey() && event.getKey() <= 348) {
            if (event.getAction() == 1) {
                keyStates[event.getKey()] = true;
            } else if (event.getAction() == 0) {
                keyStates[event.getKey()] = false;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Post event) {
        if (YesSteveModel.isAvailable() && InputUtil.isPlayerReady() && 0 <= event.getButton() && event.getButton() <= 7) {
            if (event.getAction() == 1) {
                mouseStates[event.getButton()] = true;
            } else if (event.getAction() == 0) {
                mouseStates[event.getButton()] = false;
            }
        }
    }
}