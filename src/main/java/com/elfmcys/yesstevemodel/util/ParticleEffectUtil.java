package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector3d;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public class ParticleEffectUtil {
    private static final Cache<String, ParticleOptions> particleCache = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build();

    public static boolean handleParticle(ExecutionContext<IContext<Entity>> context, Function.ArgumentCollection arguments, boolean isAbsolute) throws ExecutionException, CommandSyntaxException {
        String particleId = arguments.getAsString(context, 0);
        if (StringUtils.isBlank(particleId)) {
            return false;
        }

        Vector3d offset = new Vector3d(0.0d, 0.0d, 0.0d);
        Vector3d delta = new Vector3d(0.0d, 0.0d, 0.0d);
        double speed = 0.0d;
        int count = 0;
        int lifetime = 20;
        int argCount = arguments.size();

        if (argCount > 1) {
            offset.x = arguments.getAsDouble(context, 1);
        }
        if (argCount > 2) {
            offset.y = arguments.getAsDouble(context, 2);
        }
        if (argCount > 3) {
            offset.z = arguments.getAsDouble(context, 3);
        }
        if (argCount > 4) {
            delta.x = arguments.getAsDouble(context, 4);
        }
        if (argCount > 5) {
            delta.y = arguments.getAsDouble(context, 5);
        }
        if (argCount > 6) {
            delta.z = arguments.getAsDouble(context, 6);
        }
        if (argCount > 7) {
            speed = arguments.getAsDouble(context, 7);
        }
        if (argCount > 8) {
            count = Math.max(arguments.getAsInt(context, 8), 0);
        }
        if (argCount > 9) {
            lifetime = Math.max(arguments.getAsInt(context, 9), 1);
        }

        spawnParticles(context.entity().entity(), particleId, offset, delta, speed, count, lifetime, isAbsolute, context.entity().random());
        return true;
    }

    private static void spawnParticles(Entity entity, String particleId, Vector3d offset, Vector3d delta, double speed, int count, int lifetime, boolean isAbsolute, RandomSource random) throws ExecutionException, CommandSyntaxException {
        ParticleOptions particleOptions = particleCache.get(particleId, () -> {
            return ParticleArgument.readParticle(new StringReader(particleId), BuiltInRegistries.PARTICLE_TYPE.asLookup());
        });

        if (particleOptions == null) {
            return;
        }

        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;

        if (count == 0) {
            Vec3 spawnPos = new Vec3(offset.x(), offset.y(), offset.z());
            if (!isAbsolute) {
                if (entity instanceof Player) {
                    spawnPos = spawnPos.yRot((-((Player) entity).yBodyRot) * 0.017453292f);
                } else {
                    spawnPos = spawnPos.yRot((-entity.getYRot()) * 0.017453292f);
                }
            }

            double x = entity.getX() + spawnPos.x();
            double y = entity.getY() + spawnPos.y();
            double z = entity.getZ() + spawnPos.z();
            double velocityX = speed * delta.x();
            double velocityY = speed * delta.y();
            double velocityZ = speed * delta.z();

            Minecraft.getInstance().execute(() -> {
                Particle particle = particleEngine.createParticle(particleOptions, x, y, z, velocityX, velocityY, velocityZ);
                if (particle != null) {
                    particle.setLifetime(lifetime);
                }
            });
            return;
        }

        for (int i = 0; i < count; i++) {
            emitParticle(entity, offset, delta, speed, lifetime, particleEngine, particleOptions, isAbsolute, random);
        }
    }

    private static void emitParticle(Entity entity, Vector3d offset, Vector3d delta, double speed, int lifetime, ParticleEngine particleEngine, ParticleOptions particleOptions, boolean isAbsolute, RandomSource random) {
        double spreadX = random.nextGaussian() * delta.x();
        double spreadY = random.nextGaussian() * delta.y();
        double spreadZ = random.nextGaussian() * delta.z();
        double velocityX = random.nextGaussian() * speed;
        double velocityY = random.nextGaussian() * speed;
        double velocityZ = random.nextGaussian() * speed;

        Vec3 spawnPos = new Vec3(offset.x() + spreadX, offset.y() + spreadY, offset.z() + spreadZ);

        if (!isAbsolute) {
            spawnPos = spawnPos.yRot((-entity.getYRot()) * 0.017453292f);
        }

        double x = entity.getX() + spawnPos.x();
        double y = entity.getY() + spawnPos.y();
        double z = entity.getZ() + spawnPos.z();

        Minecraft.getInstance().execute(() -> {
            Particle particle = particleEngine.createParticle(particleOptions, x, y, z, velocityX, velocityY, velocityZ);
            if (particle != null) {
                particle.setLifetime(lifetime);
            }
        });
    }
}