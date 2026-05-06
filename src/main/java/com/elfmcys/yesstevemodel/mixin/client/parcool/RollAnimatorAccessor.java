package com.elfmcys.yesstevemodel.mixin.client.parcool;

import com.alrex.parcool.client.animation.impl.RollAnimator;
import com.alrex.parcool.common.action.impl.Roll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({RollAnimator.class})
public interface RollAnimatorAccessor {
    @Accessor(value = "direction", remap = false)
    Roll.Direction getRollDirection();
}