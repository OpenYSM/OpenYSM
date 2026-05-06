package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import org.apache.commons.lang3.StringUtils;

public class AnimationTracker {

    private String currentAnimation = StringPool.EMPTY;

    private String previousAnimation = StringPool.EMPTY;

    private String queuedAnimation = StringPool.EMPTY;

    public String getCurrentAnimation() {
        return this.currentAnimation;
    }

    public void setCurrentAnimation(String animationName) {
        this.currentAnimation = animationName;
    }

    public boolean hasAnimation() {
        return StringUtils.isNoneBlank(this.currentAnimation);
    }

    public boolean isCurrentAnimation(String animationName) {
        return hasAnimation() && animationName.equals(this.currentAnimation);
    }

    public String getPreviousAnimation() {
        return this.previousAnimation;
    }

    public String getQueuedAnimation() {
        return this.queuedAnimation;
    }

    public void setPreviousAnimation(String animationName) {
        this.previousAnimation = animationName;
    }

    public void setQueuedAnimation(String animationName) {
        this.queuedAnimation = animationName;
    }
}