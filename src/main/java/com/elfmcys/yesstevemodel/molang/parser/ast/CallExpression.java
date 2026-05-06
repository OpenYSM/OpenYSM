package com.elfmcys.yesstevemodel.molang.parser.ast;

import com.elfmcys.yesstevemodel.molang.runtime.Function;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Call expression implementation, executes function
 * with certain arguments.
 *
 * <p>Example call expressions: {@code print('hello')},
 * {@code math.sqrt(9)}, {@code math.pow(3, 2)}</p>
 *
 * @since 3.0.0
 */
public final class CallExpression implements Expression {
    public static final Function.ArgumentCollection EMPTY = new Function.ArgumentCollection(ObjectLists.emptyList());

    private final Function function;
    private final Function.ArgumentCollection arguments;

    public CallExpression(@NotNull Function function) {
        this(function, EMPTY);
    }

    public CallExpression(
            final @NotNull Function function,
            final @NotNull Function.ArgumentCollection arguments
    ) {
        this.function = requireNonNull(function, "function");
        this.arguments = requireNonNull(arguments, "arguments");
    }

    /**
     * Gets the function expression.
     *
     * @since 3.0.0
     */
    public @NotNull Function function() {
        return function;
    }

    /**
     * Gets the list of arguments to pass to
     * the function.
     *
     * @since 3.0.0
     */
    public @NotNull Function.ArgumentCollection arguments() {
        return arguments;
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitCall(this);
    }

    @Override
    public String toString() {
        return "Call(" + function + ", " + arguments + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallExpression that = (CallExpression) o;
        if (!function.equals(that.function)) return false;
        return arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        int result = function.hashCode();
        result = 31 * result + arguments.hashCode();
        return result;
    }
}