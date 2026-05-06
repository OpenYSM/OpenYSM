package com.elfmcys.yesstevemodel.geckolib3.geo.render.built;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;

public class GeoBone {
    private static final String GLOWING_PREFIX = "ysmGlow";

    private final String name;

    private final int boneId;

    private final boolean isHidden;

    private final boolean areCubesHidden;

    private final boolean hideChildBonesToo;

    private final float pivotX;

    private final float pivotY;

    private final float pivotZ;

    private final float rotX;

    private final float rotY;

    private final float rotZ;

    private final boolean glow;

    public int partMask = 0;

    public int parentIdx = -1;

    public String parentName = "";

    public GeoBone(String name, boolean isHidden, boolean areCubesHidden, boolean hideChildBonesToo, float pivotX, float pivotY, float pivotZ, float rotationX, float rotationY, float rotationZ) {
        this.name = name;
        this.boneId = StringPool.computeIfAbsent(name);
        this.isHidden = isHidden;
        this.areCubesHidden = areCubesHidden;
        this.hideChildBonesToo = hideChildBonesToo;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
        this.rotX = rotationX;
        this.rotY = rotationY;
        this.rotZ = rotationZ;
        this.glow = name.startsWith(GLOWING_PREFIX);
    }

    public String getName() {
        return this.name;
    }

    public int getBoneId() {
        return this.boneId;
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    public boolean cubesAreHidden() {
        return this.areCubesHidden;
    }

    public boolean childBonesAreHiddenToo() {
        return this.hideChildBonesToo;
    }

    public float getPivotX() {
        return this.pivotX;
    }

    public float getPivotY() {
        return this.pivotY;
    }

    public float getPivotZ() {
        return this.pivotZ;
    }

    public float getRotX() {
        return this.rotX;
    }

    public float getRotY() {
        return this.rotY;
    }

    public float getRotZ() {
        return this.rotZ;
    }

    public boolean glow() {
        return glow;
    }
}
