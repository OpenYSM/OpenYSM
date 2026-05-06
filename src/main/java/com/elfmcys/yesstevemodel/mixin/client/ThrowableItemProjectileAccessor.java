package com.elfmcys.yesstevemodel.mixin.client;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({ThrowableItemProjectile.class})
public interface ThrowableItemProjectileAccessor {
    @Invoker("getDefaultItem")
    Item invokeGetDefaultItem();
}