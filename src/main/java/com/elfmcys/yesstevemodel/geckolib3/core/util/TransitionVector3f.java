package com.elfmcys.yesstevemodel.geckolib3.core.util;

import org.joml.Vector3f;

public class TransitionVector3f extends Vector3f {

    public float percentCompleted;

    public TransitionVector3f() {
        this.percentCompleted = 1.0f;
    }

    public TransitionVector3f(float x, float y, float z) {
        super(x, y, z);
        this.percentCompleted = 1.0f;
    }

    public TransitionVector3f(Vector3f v) {
        super(v);
        this.percentCompleted = 1.0f;
    }

    public void setPercentCompleted(float newPercent) {
        if (newPercent < this.percentCompleted) {
            this.percentCompleted = newPercent;
        }
    }

    public void applyLinearBlendTo(Vector3f targetVec) {
        float progress = this.percentCompleted;
        if (progress == 0.0f) {
            targetVec.set(this);
        } else {
            MathUtil.lerpValues(progress, this, targetVec, targetVec);
        }
    }

    public void applyRotationBlendTo(Vector3f targetEuler, Vector3f offsetEuler) {
        float progress = this.percentCompleted;
        if (progress == 0.0f) {
            targetEuler.set(this);
        } else {
            MathUtil.nlerpEulerAngles(progress, this, targetEuler, offsetEuler, targetEuler);
        }
    }
}