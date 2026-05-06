package com.elfmcys.yesstevemodel.mixin.client;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.Arrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin({Arrow.class})
public interface ArrowEntityAccessor {
    @Accessor("effects")
    Set<MobEffectInstance> getEffects();
}