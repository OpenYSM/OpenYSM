package com.elfmcys.yesstevemodel.geckolib3.core.event;

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import net.minecraft.util.StringUtil;

import java.util.List;

public class SoundKeyFrameExecutor {

    private final List<EventKeyFrame<String>> soundKeyFrames;

    private final AudioPlayerManager audioPlayerManager;

    private int nextIndex = 0;

    public SoundKeyFrameExecutor(List<EventKeyFrame<String>> soundKeyFrames, AudioPlayerManager audioPlayerManager) {
        this.soundKeyFrames = soundKeyFrames;
        this.audioPlayerManager = audioPlayerManager;
    }

    public void playSound(AnimatableEntity<?> entity, float currentTick, boolean playAudio) {
        while (!reachEnd()) {
            EventKeyFrame<String> sound = this.soundKeyFrames.get(this.nextIndex);
            if (sound.getStartTick() > currentTick) {
                return;
            }
            this.nextIndex++;
            if (playAudio && !StringUtil.isNullOrEmpty(sound.getEventData())) {
                this.audioPlayerManager.playSound(entity, 0, sound.getEventData(), false, null);
            }
        }
    }

    public void reset() {
        this.nextIndex = 0;
        this.audioPlayerManager.stopAll();
    }

    public void stop() {
        this.audioPlayerManager.stopAll();
    }

    public boolean reachEnd() {
        return this.nextIndex >= this.soundKeyFrames.size();
    }
}