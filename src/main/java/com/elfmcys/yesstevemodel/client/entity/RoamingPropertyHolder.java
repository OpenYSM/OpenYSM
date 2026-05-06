package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.molang.runtime.Struct;
import org.jetbrains.annotations.Nullable;

public interface RoamingPropertyHolder {
    @Nullable
    Struct getPropertyContainer();
}