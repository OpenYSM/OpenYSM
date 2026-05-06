package com.elfmcys.yesstevemodel.molang.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Variable {
    @Nullable
    Object evaluate(@NotNull ExecutionContext<?> context);
}