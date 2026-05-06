package com.elfmcys.yesstevemodel.geckolib3.geo.animated;

import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouMaidBoneProcessor;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import org.joml.Vector3f;

public class AnimatedGeoBone implements IBone {

    private static final int ROT_X_OFFSET = 0;

    private static final int ROT_Y_OFFSET = 1;

    private static final int ROT_Z_OFFSET = 2;

    private static final int POS_X_OFFSET = 3;

    private static final int POS_Y_OFFSET = 4;

    private static final int POS_Z_OFFSET = 5;

    private static final int SCALE_X_OFFSET = 6;

    private static final int SCALE_Y_OFFSET = 7;

    private static final int SCALE_Z_OFFSET = 8;

    private static final int HIDDEN_OFFSET = 9;

    private static final int HIDE_CHILDREN_OFFSET = 10;

    private static final int TRACK_XFORM_OFFSET = 11;

    private static final int PIVOT_ABS_X_OFFSET = 0;

    private static final int PIVOT_ABS_Y_OFFSET = 1;

    private static final int PIVOT_ABS_Z_OFFSET = 2;

    private final String name;

    private final int boneId;

    private final float pivotX;

    private final float pivotY;

    private final float pivotZ;

    private final float[] matrixData;

    private final int matrixOffset;

    private final float[] stateBuffer;

    private final int stateOffset;

    private final Vector3f initialRotation;

    private Object touhouMaidBone = null;

    public AnimatedGeoBone(GeoBone geoBone, float[] matrixData, int matrixOffset, float[] stateBuffer, int stateOffset) {
        this.name = geoBone.getName();
        this.boneId = geoBone.getBoneId();
        this.pivotX = geoBone.getPivotX();
        this.pivotY = geoBone.getPivotY();
        this.pivotZ = geoBone.getPivotZ();
        this.matrixData = matrixData;
        this.matrixOffset = matrixOffset;
        this.stateBuffer = stateBuffer;
        this.stateOffset = stateOffset;
        setHidden(geoBone.isHidden(), geoBone.childBonesAreHiddenToo());
        setRotationX(geoBone.getRotX());
        setRotationY(geoBone.getRotY());
        setRotationZ(geoBone.getRotZ());
        setScaleX(1.0f);
        setScaleY(1.0f);
        setScaleZ(1.0f);
        this.initialRotation = new Vector3f(geoBone.getRotX(), geoBone.getRotY(), geoBone.getRotZ());
    }

    @Override
    public Vector3f getInitialRotation() {
        return this.initialRotation;
    }

    @Override
    public float getPivotAbsX() {
        return this.stateBuffer[this.stateOffset + PIVOT_ABS_X_OFFSET];
    }

    @Override
    public float getPivotAbsY() {
        return this.stateBuffer[this.stateOffset + PIVOT_ABS_Y_OFFSET];
    }

    @Override
    public float getPivotAbsZ() {
        return this.stateBuffer[this.stateOffset + PIVOT_ABS_Z_OFFSET];
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getBoneId() {
        return this.boneId;
    }

    @Override
    public float getRotationX() {
        return this.matrixData[this.matrixOffset + ROT_X_OFFSET];
    }

    @Override
    public void setRotationX(float f) {
        this.matrixData[this.matrixOffset + ROT_X_OFFSET] = f;
    }

    @Override
    public float getRotationY() {
        return this.matrixData[this.matrixOffset + ROT_Y_OFFSET];
    }

    @Override
    public void setRotationY(float f) {
        this.matrixData[this.matrixOffset + ROT_Y_OFFSET] = f;
    }

    @Override
    public float getRotationZ() {
        return this.matrixData[this.matrixOffset + ROT_Z_OFFSET];
    }

    @Override
    public void setRotationZ(float f) {
        this.matrixData[this.matrixOffset + ROT_Z_OFFSET] = f;
    }

    @Override
    public float getPositionX() {
        return this.matrixData[this.matrixOffset + POS_X_OFFSET];
    }

    @Override
    public void setPositionX(float f) {
        this.matrixData[this.matrixOffset + POS_X_OFFSET] = f;
    }

    @Override
    public float getPositionY() {
        return this.matrixData[this.matrixOffset + POS_Y_OFFSET];
    }

    @Override
    public void setPositionY(float f) {
        this.matrixData[this.matrixOffset + POS_Y_OFFSET] = f;
    }

    @Override
    public float getPositionZ() {
        return this.matrixData[this.matrixOffset + POS_Z_OFFSET];
    }

    @Override
    public void setPositionZ(float f) {
        this.matrixData[this.matrixOffset + POS_Z_OFFSET] = f;
    }

    @Override
    public float getScaleX() {
        return this.matrixData[this.matrixOffset + SCALE_X_OFFSET];
    }

    @Override
    public void setScaleX(float f) {
        this.matrixData[this.matrixOffset + SCALE_X_OFFSET] = f;
    }

    @Override
    public float getScaleY() {
        return this.matrixData[this.matrixOffset + SCALE_Y_OFFSET];
    }

    @Override
    public void setScaleY(float f) {
        this.matrixData[this.matrixOffset + SCALE_Y_OFFSET] = f;
    }

    @Override
    public float getScaleZ() {
        return this.matrixData[this.matrixOffset + SCALE_Z_OFFSET];
    }

    @Override
    public void setScaleZ(float f) {
        this.matrixData[this.matrixOffset + SCALE_Z_OFFSET] = f;
    }

    @Override
    public float getPivotX() {
        return this.pivotX;
    }

    @Override
    public float getPivotY() {
        return this.pivotY;
    }

    @Override
    public float getPivotZ() {
        return this.pivotZ;
    }

    @Override
    public boolean isHidden() {
        return this.matrixData[this.matrixOffset + HIDDEN_OFFSET] == 1.0f;
    }

    @Override
    public void setHidden(boolean z) {
        setHidden(z, z);
    }

    @Override
    public boolean childBonesAreHiddenToo() {
        return this.matrixData[this.matrixOffset + HIDE_CHILDREN_OFFSET] == 1.0f;
    }

    @Override
    public void setHidden(boolean selfHidden, boolean skipChildRendering) {
        this.matrixData[this.matrixOffset + HIDDEN_OFFSET] = selfHidden ? 1.0f : 0.0f;
        this.matrixData[this.matrixOffset + HIDE_CHILDREN_OFFSET] = skipChildRendering ? 1.0f : 0.0f;
    }

    @Override
    public boolean isTrackingXform() {
        return this.matrixData[this.matrixOffset + TRACK_XFORM_OFFSET] == 1.0f;
    }

    @Override
    public void setTrackXform(boolean z) {
        this.matrixData[this.matrixOffset + TRACK_XFORM_OFFSET] = z ? 1.0f : 0.0f;
    }

    public <T> T getTouhouMaidBone() {
        if (this.touhouMaidBone == null) {
            this.touhouMaidBone = TouhouMaidBoneProcessor.createLocationBone(this);
        }
        return (T) this.touhouMaidBone;
    }
}