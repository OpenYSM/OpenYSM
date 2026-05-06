package com.elfmcys.yesstevemodel.resource.models;

public record MainModelInfo(int bones, int cubes, int faces) {
    public int getBones() {
        return this.bones;
    }

    public int getCubes() {
        return this.cubes;
    }

    public int getFaces() {
        return this.faces;
    }
}
