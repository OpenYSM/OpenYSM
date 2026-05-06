package com.elfmcys.yesstevemodel.mixin;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouMaidCompat;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Projectile.class})
public class ProjectileEntityMixin {
    @Inject(at = {@At("RETURN")}, method = {"setOwner(Lnet/minecraft/world/entity/Entity;)V"})
    private void onSetOwner(Entity entity, CallbackInfo callbackInfo) {
        Projectile projectile;
        if (!YesSteveModel.isAvailable() || (projectile = (Projectile) (Object) this) == null || projectile.level() == null || projectile.level().isClientSide()) {
            return;
        }
        if (entity instanceof ServerPlayer) {
            CapabilityEvent.syncProjectileModel(projectile, (ServerPlayer) entity);
        } else if (TouhouMaidCompat.isMaidEntity(entity)) {
            TouhouMaidCompat.handleProjectileOwner(projectile, entity);
        }
    }
}