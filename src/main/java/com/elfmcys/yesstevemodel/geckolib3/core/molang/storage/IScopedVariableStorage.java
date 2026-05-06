package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage;

public interface IScopedVariableStorage {
    Object getScoped(int address);

    void setScoped(int address, Object value);
}