package com.elfmcys.yesstevemodel.mixin.client.parcool;

import com.alrex.parcool.client.animation.impl.FlippingAnimator;
import com.alrex.parcool.common.action.impl.Flipping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({FlippingAnimator.class})
public interface FlippingAnimatorAccessor {
    @Accessor(value = "direction", remap = false)
    Flipping.Direction getFlippingDirection();
}