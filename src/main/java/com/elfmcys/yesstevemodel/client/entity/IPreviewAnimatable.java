package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.animation.AnimationTracker;
import org.jetbrains.annotations.NotNull;

public interface IPreviewAnimatable {
    @NotNull
    AnimationTracker getAnimationStateMachine();

    void setCustomAnimationActive(boolean active);
}