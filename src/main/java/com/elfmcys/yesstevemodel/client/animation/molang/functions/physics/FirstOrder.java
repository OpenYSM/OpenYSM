package com.elfmcys.yesstevemodel.client.animation.molang.functions.physics;

public class FirstOrder implements IPhysics {

    private float input;

    private float response;

    private float lastSimulation = 0.0f;

    public FirstOrder(float input, float response) {
        this.input = input;
        this.response = response;
    }

    @Override
    public void update(float timeStep) {
        this.lastSimulation = ((1 - (timeStep / this.response)) * this.lastSimulation) + ((timeStep / this.response) * this.input);
    }

    @Override
    public void setArgs(float input, float response, float arg2, float arg3) {
        this.input = input;
        this.response = response;
    }

    @Override
    public float getValue() {
        return this.lastSimulation;
    }
}