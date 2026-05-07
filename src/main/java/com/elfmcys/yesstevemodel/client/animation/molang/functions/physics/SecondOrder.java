package com.elfmcys.yesstevemodel.client.animation.molang.functions.physics;

import net.minecraft.util.Mth;

/**
 * @author MicroCraft
 *
 * <a href="https://www.youtube.com/watch?v=KPoeNZZ6H4s">Giving Personality to Procedural Animations using Math</a>
 */
public class SecondOrder implements IPhysics {

    private float inputFunction = 0.0f;
    private float lastSimulation = 0.0f;
    private float lastSimulationDot = 0.0f;
    private float input;
    private float frequency;
    private float coefficient;
    private float response;

    public SecondOrder(float input, float frequency, float coefficient, float response) {
        this.input = input;
        this.frequency = Mth.clamp(frequency, 0, 5);
        this.coefficient = Mth.clamp(coefficient, 0, 1);
        this.response = response;
    }

    @Override
    public void update(float timeStep) {
        float input = this.input;
        float frequency = Mth.clamp(this.frequency, 0, 5);
        float coefficient = Mth.clamp(this.coefficient, 0, 1);
        float response = this.response;

        float k1 = coefficient / Mth.PI / frequency;
        float k2 = 1 / (2 * Mth.PI * frequency) / (2 * Mth.PI * frequency);
        float k3 = response * coefficient / 2 / Mth.PI / frequency;

        float inputFunctionDot = (input - this.inputFunction) / timeStep;
        this.inputFunction = input;

        float maxTimeStep = (float) Math.sqrt(4 * k2 + k1 * k1) - k1;
        int cycleTime = (int) Math.ceil(timeStep / maxTimeStep);
        timeStep = timeStep / cycleTime;

        var lastSimulationDot = this.lastSimulationDot;
        var lastSimulation = this.lastSimulation;
        for (; cycleTime > 0; cycleTime--) {
            lastSimulation = lastSimulation + timeStep * lastSimulationDot;
            lastSimulationDot = lastSimulationDot + timeStep * (k3 * inputFunctionDot + input - lastSimulation - k1 * lastSimulationDot) / k2;
        }
        this.lastSimulation = lastSimulation;
        this.lastSimulationDot = lastSimulationDot;
    }

    @Override
    public void setArgs(float input, float frequency, float coefficient, float response) {
        this.input = input;
        this.frequency = frequency;
        this.coefficient = coefficient;
        this.response = response;
    }

    @Override
    public float getValue() {
        return this.lastSimulation;
    }
}