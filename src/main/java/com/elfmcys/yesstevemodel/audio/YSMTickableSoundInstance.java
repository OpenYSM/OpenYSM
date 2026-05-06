package com.elfmcys.yesstevemodel.audio;

import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class YSMTickableSoundInstance extends AbstractTickableSoundInstance implements IAudioPlayer {

    public final Entity entity;

    public float targetVolume;

    public YSMTickableSoundInstance(SoundEvent soundEvent, Entity entity) {
        super(soundEvent, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.targetVolume = 1.0f;
        this.entity = entity;
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
    }

    public void tick() {
        this.volume = (this.targetVolume * GeneralConfig.SOUND_VOLUME.get().floatValue()) / 100.0f;
        if (this.entity.isRemoved()) {
            stop();
            return;
        }
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
    }

    public void setVolume(float f) {
        this.targetVolume = f;
    }

    public void setPitch(float f) {
        this.pitch = f;
    }

    public void stopSound() {
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
    }

    @Override
    public void release() {
        stop();
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getSoundManager().stop(this);
        });
    }

    public void setLooping(boolean z) {
        this.looping = z;
    }
}