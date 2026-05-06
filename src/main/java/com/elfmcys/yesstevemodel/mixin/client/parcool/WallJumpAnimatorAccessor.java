package com.elfmcys.yesstevemodel.mixin.client.parcool;

import com.alrex.parcool.client.animation.impl.WallJumpAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({WallJumpAnimator.class})
public interface WallJumpAnimatorAccessor {
    @Accessor(value = "wallRightSide", remap = false)
    boolean isJumpingRight();
}