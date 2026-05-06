package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.compat.gun.tacz.TacCompat;
import com.elfmcys.yesstevemodel.client.compat.immersiveaircraft.ImmersiveAirCraftCompat;
import com.elfmcys.yesstevemodel.client.compat.immersivemelodies.ImmersiveMelodiesCompat;
import com.elfmcys.yesstevemodel.client.compat.ironsspellbooks.SpellbooksCompat;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.compat.acceleratedrendering.AcceleratedRenderingCompat;
import com.elfmcys.yesstevemodel.client.compat.bettercombat.BetterCombatCompat;
import com.elfmcys.yesstevemodel.client.compat.carryon.CarryOnCompat;
import com.elfmcys.yesstevemodel.client.compat.cosmeticarmorreworked.CosmeticArmorCompat;
import com.elfmcys.yesstevemodel.client.compat.curios.CuriosCompat;
import com.elfmcys.yesstevemodel.client.compat.elytraslot.ElytraSlotCompat;
import com.elfmcys.yesstevemodel.client.compat.firstperson.FirstPersonCompat;
import com.elfmcys.yesstevemodel.client.compat.gun.swarfare.SWarfareCompat;
import com.elfmcys.yesstevemodel.client.compat.oculus.OculusCompat;
import com.elfmcys.yesstevemodel.client.compat.optifine.OptiFineDetector;
import com.elfmcys.yesstevemodel.client.compat.parcool.ParcoolCompat;
import com.elfmcys.yesstevemodel.client.compat.playeranimator.PlayerAnimatorCompat;
import com.elfmcys.yesstevemodel.client.compat.realcamera.RealCameraCompat;
import com.elfmcys.yesstevemodel.client.compat.simplehats.SimpleHatsHelper;
import com.elfmcys.yesstevemodel.client.compat.slashblade.SlashBladeCompat;
import com.elfmcys.yesstevemodel.client.compat.swem.SWEMCompat;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import com.elfmcys.yesstevemodel.client.input.ExtraAnimationKey;
import com.elfmcys.yesstevemodel.client.input.*;
import com.elfmcys.yesstevemodel.client.compat.sbackpack.SBackpackCompat;
import com.elfmcys.yesstevemodel.client.renderer.*;
import com.elfmcys.yesstevemodel.client.compat.simpleplanes.SimplePlanesCompat;
import com.elfmcys.yesstevemodel.client.compat.create.CreateCompat;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.LoadingModList;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Optional;

@Mod.EventBusSubscriber(value = {Dist.CLIENT}, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetupEvent {
    public static Object nativeClientInit() {
        try {
            int maxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
            if (maxTexSize <= 0) {
                return Component.literal("YSM: OpenGL context not available");
            }
            // 原始C++碼檢查了GL20（著色器）和 GL30（VAO）的可用性
            try {
                int testShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
                if (testShader != 0) {
                    GL20.glDeleteShader(testShader);
                }
            } catch (Exception e) {
                return Component.literal("YSM: GL20 (shaders) not available");
            }

            // 预載入default模型，延遲至第一次渲染tick
            // 不能在FMLClientSetupEvent中同步執行ModelAssembler，會導致StackOverflow
            //ClientModelManager.schedulePreloadDefaultModel();
            return null; // 成功
        } catch (Exception e) {
            return Component.literal("YSM Client Init Failed: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        AnimationRegister.registerAnimationState();
        event.enqueueWork(() -> {
            CuriosCompat.init();
            FirstPersonCompat.init();
            RealCameraCompat.init();
            PlayerAnimatorCompat.init();
            BetterCombatCompat.init();
            OculusCompat.init();
            AcceleratedRenderingCompat.init();
            OptiFineDetector.init();
            CosmeticArmorCompat.init();
            ElytraSlotCompat.init();
            TacCompat.init();
            SWarfareCompat.init();
            TouhouLittleMaidCompat.init();
            CarryOnCompat.init();
            ParcoolCompat.init();
            SlashBladeCompat.init();
            SWEMCompat.init();
            CreateCompat.init();
            SBackpackCompat.init();
            SimpleHatsHelper.init();
            ImmersiveMelodiesCompat.init();
            SpellbooksCompat.init();
            SimplePlanesCompat.init();
            ImmersiveAirCraftCompat.init();
            showInCompatibleMod(SBackpackCompat.getInCompatibleInfo());
            showInCompatibleMod(ParcoolCompat.getInCompatibleInfo());
            showInCompatibleMod("epicfight", "Epic Fight");
            checkNativeInitialization();
        });
    }

    private static void showInCompatibleMod(Optional<Pair<String, String>> optional) {
        optional.ifPresent(pair -> ModLoader.get().addWarning(new ModLoadingWarning(LoadingModList.get().getModFileById(YesSteveModel.MOD_ID).getMods().get(0), ModLoadingStage.SIDED_SETUP, "error.yes_steve_model.incompatible_mod_version", pair.getKey(), pair.getValue())));
    }

    private static void showInCompatibleMod(String str, String str2) {
        if (LoadingModList.get().getModFileById(str) != null) {
            ModLoader.get().addWarning(new ModLoadingWarning(LoadingModList.get().getModFileById(YesSteveModel.MOD_ID).getMods().get(0), ModLoadingStage.SIDED_SETUP, "error.yes_steve_model.incompatible_mod", str2));
        }
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(PlayerModelToggleKey.KEY_MAPPING);
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        event.register(AnimationRouletteKey.KEY_ROULETTE);
        event.register(AnimationRouletteKey.KEY_LOCK);
        event.register(DebugAnimationKey.KEY_MAPPING);
        event.register(ExtraPlayerRenderKey.KEY_MAPPING);
        ExtraAnimationKey.registerKeyMappings(event);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        event.registerAbove(VanillaGuiOverlay.DEBUG_TEXT.id(), "ysm_debug_info", AnimationDebugOverlay.createOverlay());
        event.registerAbove(VanillaGuiOverlay.DEBUG_TEXT.id(), "ysm_extra_player", new LoadingStateOverlay());
        event.registerAbove(VanillaGuiOverlay.DEBUG_TEXT.id(), "ysm_loading_state", new ModelSyncStateOverlay());
    }

    private static void checkNativeInitialization() {
        Component component = (Component) nativeClientInit();
        if (component != null) {
            throw new RuntimeException("YSM Client Initialization Failed: " + component.getString(256));
        }
    }

    // 這裡本來有一個native方法，可能是運行時會初始化載入模型
}