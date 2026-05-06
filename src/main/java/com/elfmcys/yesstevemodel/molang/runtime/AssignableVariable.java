package com.elfmcys.yesstevemodel.molang.runtime;

import org.jetbrains.annotations.NotNull;

public interface AssignableVariable extends Variable {
    void assign(@NotNull ExecutionContext<?> context, Object value);
}