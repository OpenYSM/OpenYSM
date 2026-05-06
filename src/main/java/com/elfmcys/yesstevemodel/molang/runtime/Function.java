package com.elfmcys.yesstevemodel.molang.runtime;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression;
import com.elfmcys.yesstevemodel.molang.parser.ast.StringExpression;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface Function {
    /**
     * Executes this function with the given arguments.
     *
     * @param context   The execution context
     * @param arguments The arguments
     * @return The function result
     * @since 3.0.0
     */
    @Nullable Object evaluate(final @NotNull ExecutionContext<?> context, final @NotNull ArgumentCollection arguments);

    default boolean validateArgumentSize(int size) {
        return true;
    }

    ArgumentCollection EMPTY_ARGUMENT = new ArgumentCollection(new ArrayList());

    Function NOOP = (executionContext, argumentCollection) -> null;

    class ArgumentCollection {

        private final List<Expression> arguments;

        public ArgumentCollection(List<Expression> arguments) {
            this.arguments = arguments;
        }

        public int size() {
            return this.arguments.size();
        }

        @Nullable
        public String getAsString(@NotNull ExecutionContext<?> ctx, final int index) {
            return ValueConversions.asString(ctx.evalSafe(this.arguments.get(index)));
        }

        public int getStringId(@NotNull ExecutionContext<?> ctx, final int index) {
            return ValueConversions.asStringId(ctx.evalSafe(this.arguments.get(index)));
        }

        public double getAsDouble(@NotNull ExecutionContext<?> ctx, final int index) {
            return ValueConversions.asDouble(ctx.evalSafe(this.arguments.get(index)));
        }

        public int getAsInt(@NotNull ExecutionContext<?> ctx, final int index) {
            return ValueConversions.asInt(ctx.evalSafe(this.arguments.get(index)));
        }

        public float getAsFloat(@NotNull ExecutionContext<?> ctx, final int index) {
            return ValueConversions.asFloat(ctx.evalSafe(this.arguments.get(index)));
        }

        public boolean getAsBoolean(@NotNull ExecutionContext<?> ctx, final int index) {
            return ValueConversions.asBoolean(ctx.evalSafe(this.arguments.get(index)));
        }

        @Nullable
        public ResourceLocation getResourceLocation(@NotNull ExecutionContext<? extends IContext<?>> ctx, final int index) {
            Object obj;
            Object obj2 = getValue(ctx, index);
            if (obj2 instanceof StringExpression stringExpression) {
                if (stringExpression.getResourceLocation() != null) {
                    return stringExpression.getResourceLocation();
                }
                ResourceLocation resourceLocationTryParse = ResourceLocation.tryParse(stringExpression.getName());
                if (resourceLocationTryParse != null) {
                    stringExpression.setResourceLocation(resourceLocationTryParse);
                    return resourceLocationTryParse;
                }
                obj = stringExpression.getName();
            } else if (obj2 instanceof String str) {
                ResourceLocation resourceLocationTryParse2 = ResourceLocation.tryParse(str);
                if (resourceLocationTryParse2 != null) {
                    return resourceLocationTryParse2;
                }
                obj = str;
            } else {
                obj = obj2;
            }
            ctx.entity().logWarning("Illegal resource location: ", obj);
            return null;
        }

        @Nullable
        public Object getValue(@NotNull ExecutionContext<?> ctx, final int index) {
            return ctx.evalSafe(this.arguments.get(index));
        }

        public Expression getExpression(int i) {
            return this.arguments.get(i);
        }
    }
}