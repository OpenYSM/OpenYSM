package com.elfmcys.yesstevemodel.geckolib3.core.snapshot;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import org.joml.Vector3f;

public class BoneSnapshot {
    public final int boneId;

    public final Vector3f position = new Vector3f();

    public final Vector3f rotation = new Vector3f();

    public final Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);

    public boolean hidden;

    public boolean childrenHidden;

    public BoneSnapshot(IBone bone) {
        applyTransform(bone);
        this.boneId = bone.getBoneId();
    }

    public void applyTransform(IBone bone) {
        Vector3f initialRotation = bone.getInitialRotation();
        this.position.set(bone.getPositionX(), bone.getPositionY(), bone.getPositionZ());
        this.rotation.set(bone.getRotationX() - initialRotation.x, bone.getRotationY() - initialRotation.y, bone.getRotationZ() - initialRotation.z);
        this.scale.set(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
        this.hidden = bone.isHidden();
        this.childrenHidden = bone.childBonesAreHiddenToo();
    }

    public void copyFrom(BoneSnapshot snapshot) {
        this.position.set(snapshot.position);
        this.rotation.set(snapshot.rotation);
        this.scale.set(snapshot.scale);
        this.hidden = snapshot.hidden;
        this.childrenHidden = snapshot.childrenHidden;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof BoneSnapshot) && this.boneId == ((BoneSnapshot) obj).boneId;
    }

    public int hashCode() {
        return this.boneId;
    }
}