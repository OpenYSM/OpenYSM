package com.elfmcys.yesstevemodel.client.compat.immersivemelodies;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

public class ImmersiveMelodiesCompat {

    private static final String MOD_ID = "immersive_melodies";

    private static boolean IS_LOADED = false;

    public static void init() {
        IS_LOADED = ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static void updateMelodyProgress(LivingEntity livingEntity, ImmersiveMelodiesData imData) {
        if (IS_LOADED) {
            ImmersiveMelodiesBinding.updateInstrumentData(livingEntity, imData);
        }
    }

    public static void registerBindings(CtrlBinding binding) {
        if (IS_LOADED) {
            ImmersiveMelodiesBinding.registerControllerFunctions(binding);
        } else {
            registerDummyBindings(binding);
        }
    }

    /**
     * 没有安装此模组时，这些 molang 应该存在，否则会报错
     */
    private static void registerDummyBindings(CtrlBinding binding) {
        binding.livingEntityVar("im_pitch", interfaceC0807x6b368640 -> 0.0f);
        binding.livingEntityVar("im_volume", interfaceC0807x6b3686402 -> 0.0f);
        binding.livingEntityVar("im_current", interfaceC0807x6b3686403 -> 0.0f);
        binding.livingEntityVar("im_delta", interfaceC0807x6b3686404 -> 0L);
        binding.livingEntityVar("im_time", interfaceC0807x6b3686405 -> 0L);
    }

    /**
     * 沉浸音乐模组的数据缓存
     */
    public static final class ImmersiveMelodiesData {
        /**
         * 音高，一般是 0-2 之间
         */
        public float pitch = 0f;
        /**
         * 音量，一般是 0-2 之间
         */
        public float volume = 0f;
        /**
         * 电平强度，范围 0-1
         */
        public float current = 0f;
        /**
         * 自上次音符输出后，经过的时间（单位：ms）
         */
        public long delta = 0L;
        /**
         * 自开始演奏后，经过的时间（单位：ms）
         */
        public long time = 0L;
    }
}