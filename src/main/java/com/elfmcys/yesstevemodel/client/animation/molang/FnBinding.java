package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FnBinding implements ObjectBinding, ResetVariable {

    private final Object2ReferenceOpenHashMap<String, Fn> functions = new Object2ReferenceOpenHashMap<>();

    @Override
    public Function getProperty(String str) {
        return this.functions.computeIfAbsent(str, Fn::new);
    }

    @Override
    public void reset() {
        this.functions.clear();
    }

    private static class Fn implements Function {

        private String functionName;

        private IValue cachedIValue;

        private Fn(String str) {
            this.functionName = str;
        }

        @Override
        @Nullable
        public Object evaluate(@NotNull ExecutionContext<?> context, @NotNull Function.ArgumentCollection arguments) {
            Object entity = context.entity();
            if (entity instanceof IContext ctx) {
                if (this.cachedIValue == null) {
                    if (this.functionName == null) {
                        return null;
                    }
                    this.cachedIValue = ctx.resolveExpression(this.functionName);
                    if (this.cachedIValue == null) {
                        ctx.logWarning("User function not found: %s", this.functionName);
                        this.functionName = null;
                        return null;
                    }
                }
                return ctx.callFunctionWithArgs(context, this.cachedIValue, arguments);
            }
            return null;
        }
    }
}