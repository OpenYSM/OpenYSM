package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.FloatValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.RotationValue;

@SuppressWarnings("FieldMayBeFinal,unused")
public class RawBoneKeyFrame {

    public double startTick;

    public EasingType easingType;

    public double preX;
    public IValue preXValue;
    public double preY;
    public IValue preYValue;
    public double preZ;
    public IValue preZValue;

    public double postX;
    public IValue postXValue;
    public double postY;
    public IValue postYValue;
    public double postZ;
    public IValue postZValue;

    public boolean contiguous = true;

    public Vector3v preValue;
    public Vector3v postValue;

    private IValue getValue(IValue value, double primitive, boolean isRotation, boolean flip) {
        if (value == null) {
            if (isRotation) {
                return new FloatValue(RotationValue.convert((float) primitive, flip));
            }
            return new FloatValue((float) primitive);
        }
        if (isRotation) {
            return new RotationValue(value, flip);
        }
        return value;
    }

    public void init(boolean isRotation) {
        if (this.preValue != null) {
            return;
        }
        this.preValue = new Vector3v(getValue(this.preXValue, this.preX, isRotation, true), getValue(this.preYValue, this.preY, isRotation, true), getValue(this.preZValue, this.preZ, isRotation, false));
        if (this.contiguous) {
            this.postValue = this.preValue;
        } else {
            this.postValue = new Vector3v(getValue(this.postXValue, this.postX, isRotation, true), getValue(this.postYValue, this.postY, isRotation, true), getValue(this.postZValue, this.postZ, isRotation, false));
        }

        if (easingType == null) easingType = EasingType.LINEAR;
    }

    public float startTick() {
        return (float) this.startTick;
    }

    public EasingType easingType() {
        return this.easingType;
    }

    public Vector3v preValue() {
        return this.preValue;
    }

    public Vector3v postValue() {
        return this.postValue;
    }
}