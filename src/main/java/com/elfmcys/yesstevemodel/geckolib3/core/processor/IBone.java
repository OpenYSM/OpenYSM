package com.elfmcys.yesstevemodel.geckolib3.core.processor;

import org.joml.Vector3f;

public interface IBone {
    float getRotationX();

    void setRotationX(float value);

    float getRotationY();

    void setRotationY(float value);

    float getRotationZ();

    void setRotationZ(float value);

    float getPositionX();

    void setPositionX(float value);

    float getPositionY();

    void setPositionY(float value);

    float getPositionZ();

    void setPositionZ(float value);

    float getScaleX();

    void setScaleX(float value);

    float getScaleY();

    void setScaleY(float value);

    float getScaleZ();

    void setScaleZ(float value);

    float getPivotX();

    float getPivotY();

    float getPivotZ();

    boolean isHidden();

    void setHidden(boolean hidden);

    boolean childBonesAreHiddenToo();

    void setHidden(boolean selfHidden, boolean skipChildRendering);

    boolean isTrackingXform();

    void setTrackXform(boolean z);

    Vector3f getInitialRotation();

    float getPivotAbsX();

    float getPivotAbsY();

    float getPivotAbsZ();

    String getName();

    int getBoneId();
}