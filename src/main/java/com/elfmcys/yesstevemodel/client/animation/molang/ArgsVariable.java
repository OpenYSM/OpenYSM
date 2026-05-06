package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArgsVariable implements Variable {

    public static final ArgsVariable INSTANCE = new ArgsVariable();

    @Override
    @Nullable
    public Object evaluate(@NotNull ExecutionContext<?> context) {
        Object entity = context.entity();
        if (entity instanceof IContext) {
            return ((IContext<?>) entity).getAnimationLayers();
        }
        return null;
    }
}