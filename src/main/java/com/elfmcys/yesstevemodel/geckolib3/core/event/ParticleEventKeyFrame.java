package com.elfmcys.yesstevemodel.geckolib3.core.event;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;

public class ParticleEventKeyFrame extends EventKeyFrame<String> {
    public final String effect;

    public final String locator;

    public final String script;

    public ParticleEventKeyFrame(Double startTick, String eventData, String effect, String locator, String script) {
        super(startTick, eventData);
        this.script = script;
        this.locator = locator;
        this.effect = effect;
    }
}