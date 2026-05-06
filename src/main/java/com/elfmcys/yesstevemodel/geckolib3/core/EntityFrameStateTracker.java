package com.elfmcys.yesstevemodel.geckolib3.core;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EntityFrameStateTracker<T extends Entity> {

    public T entity;

    private int currentTick;

    private Vec3 lastPosition;

    private String cachedModelId;

    public float currentTime;

    public float timeDelta;

    private Vec3 positionDelta = Vec3.ZERO;

    private final IntOpenHashSet animatedEntities = new IntOpenHashSet();

    public EntityFrameStateTracker(T entity) {
        this.entity = entity;
    }

    public void reset() {
        this.animatedEntities.clear();
        this.currentTick = 0;
        this.lastPosition = null;
        this.positionDelta = Vec3.ZERO;
        this.cachedModelId = null;
        this.currentTime = 0.0f;
        this.timeDelta = 0.0f;
    }

    public final void updateState(int tickCount, float seekTime, float frameTime) {
        if (this.currentTick < tickCount) {
            onTickUpdate(tickCount, this.currentTick);
            this.currentTick = tickCount;
        }
        if (this.currentTime < seekTime) {
            onTimeUpdate(seekTime, this.currentTime, frameTime);
            this.currentTime = seekTime;
        }
    }

    public void setEntity(T t) {
        this.entity = t;
    }

    public void onTimeUpdate(float f, float f2, float f3) {
        this.timeDelta = f - f2;
        updatePosition(f3);
        this.cachedModelId = null;
    }

    public void onTickUpdate(int i, int i2) {
        this.animatedEntities.clear();
    }

    private void updatePosition(float f) {
        Vec3 vec3 = new Vec3(Mth.lerp(f, this.entity.xo, this.entity.getX()), Mth.lerp(f, this.entity.yo, this.entity.getY()), Mth.lerp(f, this.entity.zo, this.entity.getZ()));
        if (this.lastPosition != null) {
            this.positionDelta = vec3.subtract(this.lastPosition);
        }
        this.lastPosition = vec3;
    }

    public boolean markProcessed(int i) {
        return this.animatedEntities.add(i);
    }

    public boolean isProcessed(int i) {
        return this.animatedEntities.contains(i);
    }

    public Vec3 getPositionDelta() {
        return this.positionDelta;
    }

    @Nullable
    public String getCachedModelId() {
        return this.cachedModelId;
    }

    public void setCachedModelId(String str) {
        this.cachedModelId = str;
    }

    public float getTimeDelta() {
        return this.timeDelta;
    }
}