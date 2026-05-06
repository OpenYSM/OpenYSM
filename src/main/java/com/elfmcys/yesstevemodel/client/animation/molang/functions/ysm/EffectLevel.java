package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import com.elfmcys.yesstevemodel.mixin.client.ArrowEntityAccessor;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectLevel extends ContextFunction<Entity> {
    @Override
    public boolean validateArgumentSize(int size) {
        return size >= 1;
    }

    @Override
    public java.lang.Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        int effects = 0;

        for (int i = 0; i < arguments.size(); i++) {
            ResourceLocation var5 = arguments.getResourceLocation(context, i);
            if (var5 != null) {
                MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(var5);
                if (mobEffect != null) {
                    if (context.entity().geoInstance() instanceof PlayerCapability cap
                            && !cap.isLocalPlayerModel()) {
                        effects += cap.getPositionTracker().getEffectAmplifier(mobEffect);
                    } else if (((IContext<?>)context.entity()).entity() instanceof LivingEntity) {
                        MobEffectInstance mobEffectInstance = ((LivingEntity)((IContext<?>)context.entity()).entity())
                                .getEffect(mobEffect);
                        if (mobEffectInstance != null) {
                            effects += mobEffectInstance.getAmplifier() + 1;
                        }
                    } else {
                        if (!(((IContext<?>)context.entity()).entity() instanceof Arrow)) {
                            return null;
                        }

                        for (MobEffectInstance mobEffectInstance : ((ArrowEntityAccessor)((IContext<?>)context.entity()).entity())
                                .getEffects()) {
                            if (mobEffectInstance.getEffect() == mobEffect) {
                                effects += mobEffectInstance.getAmplifier() + 1;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return effects;
    }
}