package com.elfmcys.yesstevemodel.geckolib3.core.snapshot;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import org.joml.Vector3f;

public class BoneTopLevelSnapshot extends BoneSnapshot {

    public final IBone bone;

    @Deprecated
    public final Vector3f currentValue;

    public boolean isCurrentlyRunningAnimation;

    public boolean isCurrentlyRunningRotationAnimation;

    public boolean isCurrentlyRunningPositionAnimation;

    public boolean isCurrentlyRunningScaleAnimation;

    public float mostRecentResetRotationTick;

    public float mostRecentResetPositionTick;

    public float mostRecentResetScaleTick;

    public Vector3f prevRotation;

    public Vector3f prevPosition;

    public Vector3f prevScale;

    public BoneTopLevelSnapshot(IBone bone) {
        super(bone);
        this.currentValue = new Vector3f();
        this.isCurrentlyRunningAnimation = false;
        this.isCurrentlyRunningRotationAnimation = true;
        this.isCurrentlyRunningPositionAnimation = true;
        this.isCurrentlyRunningScaleAnimation = true;
        this.bone = bone;
    }

    public void reset() {
        this.bone.setHidden(this.hidden, this.childrenHidden);
        Vector3f initialRotation = this.bone.getInitialRotation();
        this.bone.setRotationX(this.rotation.x + initialRotation.x);
        this.bone.setRotationY(this.rotation.y + initialRotation.y);
        this.bone.setRotationZ(this.rotation.z + initialRotation.z);
        this.bone.setPositionX(this.position.x);
        this.bone.setPositionY(this.position.y);
        this.bone.setPositionZ(this.position.z);
        this.bone.setScaleX(this.scale.x);
        this.bone.setScaleY(this.scale.y);
        this.bone.setScaleZ(this.scale.z);
        this.currentValue.set(0.0f, 0.0f, 0.0f);
    }
}