package com.elfmcys.yesstevemodel.mixin.client.parcool;

import com.alrex.parcool.client.animation.impl.DodgeAnimator;
import com.alrex.parcool.common.action.impl.Dodge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({DodgeAnimator.class})
public interface DodgeAnimatorAccessor {
    @Accessor(value = "direction", remap = false)
    Dodge.DodgeDirection getDodgeDirection();
}