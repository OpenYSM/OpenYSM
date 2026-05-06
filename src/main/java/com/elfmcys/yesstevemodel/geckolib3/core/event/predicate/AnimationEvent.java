package com.elfmcys.yesstevemodel.geckolib3.core.event.predicate;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import org.jetbrains.annotations.NotNull;

public class AnimationEvent<T extends AnimatableEntity<?>> {

    private final T animatable;

    private final float limbSwing;

    private final float limbSwingAmount;

    private final int tickCount;

    private final float partialTick;

    private final float frameTime;

    private final boolean isMoving;

    private final boolean isFirstPerson;

    public float currentTick;

    private final EntityModelData modelData;

    public PredicateBasedController<T> controller;

    public AnimationEvent(T t, float f, float f2, int tickCount, float partialTick, float frameTime, boolean isMoving, boolean z2, @NotNull EntityModelData modelData) {
        this.animatable = t;
        this.limbSwing = f;
        this.limbSwingAmount = f2;
        this.tickCount = tickCount;
        this.partialTick = partialTick;
        this.frameTime = frameTime;
        this.currentTick = tickCount + frameTime;
        this.isMoving = isMoving;
        this.isFirstPerson = z2;
        this.modelData = modelData;
    }

    public float getCurrentTick() {
        return this.currentTick;
    }

    public T getAnimatable() {
        return this.animatable;
    }

    public float getLimbSwing() {
        return this.limbSwing;
    }

    public float getLimbSwingAmount() {
        return this.limbSwingAmount;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    public float getFrameTime() {
        return this.frameTime;
    }

    public boolean isMoving() {
        return this.isMoving;
    }

    public boolean isFirstPerson() {
        return this.isFirstPerson;
    }

    public PredicateBasedController<T> getController() {
        return this.controller;
    }

    public void setController(PredicateBasedController<T> controller) {
        this.controller = controller;
    }

    @NotNull
    public EntityModelData getModelData() {
        return this.modelData;
    }
}