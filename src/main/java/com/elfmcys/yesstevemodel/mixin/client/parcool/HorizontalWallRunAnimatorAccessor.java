package com.elfmcys.yesstevemodel.mixin.client.parcool;

import com.alrex.parcool.client.animation.impl.HorizontalWallRunAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({HorizontalWallRunAnimator.class})
public interface HorizontalWallRunAnimatorAccessor {
    @Accessor(value = "wallIsRightSide", remap = false)
    boolean isRunningRight();
}