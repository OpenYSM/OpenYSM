package com.elfmcys.yesstevemodel.client.animation.molang.functions.physics;

public interface IPhysics {
    void update(float timeStep);

    void setArgs(float arg0, float arg1, float arg2, float arg3);

    float getValue();
}