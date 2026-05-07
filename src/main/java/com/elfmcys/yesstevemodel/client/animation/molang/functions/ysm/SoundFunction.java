package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.StringUtils;

public class SoundFunction {
    public static class StopSoundFunction extends EntityFunction {
        @Override
        public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
            int id;
            if (!context.entity().isClientSide()) {
                return false;
            }
            Object idValue = arguments.getValue(context, 0);
            if (idValue instanceof Number) {
                id = -((Number) idValue).intValue();
                if (id > 0) {
                    return false;
                }
            } else {
                id = ValueConversions.asStringId(idValue);
            }
            AudioPlayerManager audioPlayerManager = context.entity().getAudioPlayerManager(arguments.size() == 2 && arguments.getAsBoolean(context, 1));
            if (audioPlayerManager != null) {
                return audioPlayerManager.stopSound(id);
            }
            return false;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 1 || size == 2;
        }
    }

    public static class StopAllSoundsFunction extends EntityFunction {
        @Override
        public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
            if (!context.entity().isClientSide()) {
                return false;
            }

            // global = 1：停止全局上下文的播放实例
            // global = 0 或不写：停止当前上下文的播放实例
            AudioPlayerManager audioPlayerManager = context.entity().getAudioPlayerManager(arguments.size() > 0 && arguments.getAsBoolean(context, 0));
            if (audioPlayerManager != null) {
                audioPlayerManager.stopAll();
                return true;
            }
            return false;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size <= 1;
        }
    }

    public static class PlaySoundFunction extends EntityFunction {
        @Override
        public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
            int id;
            int flags;
            if (!context.entity().isClientSide()) {
                return false;
            }
            Object idValue = arguments.getValue(context, 0);
            if (idValue instanceof Number) {
                id = -((Number) idValue).intValue();
                if (id > 0) {
                    return false;
                }
            } else {
                id = ValueConversions.asStringId(idValue);
            }
            String soundName = arguments.getAsString(context, 1);
            if (!StringUtils.isBlank(soundName) && context.entity().entity() != null) {
                if (arguments.size() >= 3) {
                    flags = arguments.getAsInt(context, 2);
                    if (flags < 0 || flags > 7) {
                        return false;
                    }
                } else {
                    flags = 0;
                }
                AudioPlayerManager audioPlayerManager = context.entity().getAudioPlayerManager((flags & 2) == 2);
                if (audioPlayerManager == null) {
                    return false;
                }
                int i = flags;
                return audioPlayerManager.playSound(context.entity().geoInstance(), id, soundName, (flags & 1) == 1, sound -> {
                    if (sound == null) {
                        context.entity().logWarning("Sound not found: %s", soundName);
                        return;
                    }
                    sound.setLooping((i & 4) == 4);
                    if (arguments.size() >= 4) {
                        sound.setVolume(Mth.clamp(arguments.getAsFloat(context, 3), 0.001f, 1000.0f));
                    }
                    if (arguments.size() >= 5) {
                        sound.setPitch(Mth.clamp(arguments.getAsFloat(context, 4), 0.001f, 1000.0f));
                    }
                    if (context.entity().geoInstance().hasCustomTexture()) {
                        sound.stopSound();
                    }
                });
            }
            return false;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size >= 2 && size <= 5;
        }
    }
}