package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage;

public interface IControllerVariableStorage {
    Object getControllerVariable(int address);

    void setControllerVariable(int address, Object value);
}