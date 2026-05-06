package com.elfmcys.yesstevemodel.mixin.client.parcool;

import com.alrex.parcool.client.animation.Animator;
import com.alrex.parcool.common.capability.Animation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Animation.class})
public interface AnimationAccessor {
    @Accessor(value = "animator", remap = false)
    Animator getAnimator();
}