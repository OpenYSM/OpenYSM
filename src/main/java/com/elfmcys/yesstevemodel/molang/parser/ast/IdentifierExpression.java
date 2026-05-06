package com.elfmcys.yesstevemodel.molang.parser.ast;

import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import com.elfmcys.yesstevemodel.molang.runtime.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class IdentifierExpression implements Expression {

    private final String name;
    private final Object target;

    private IdentifierExpression(@NotNull String name, Object target) {
        Objects.requireNonNull(name, "name");

        this.name = name.toLowerCase(); // case-insensitive
        this.target = target;
    }

    public static Expression get(String str, Object obj) {
        if (obj instanceof Number) {
            return new FloatExpression(((Number) obj).floatValue());
        }
        if (obj instanceof String) {
            return new StringExpression((String) obj);
        }
        if (obj instanceof Function) {
            return new CallExpression((Function) obj);
        }
        if (obj instanceof AssignableVariable) {
            return new AssignableVariableExpression((AssignableVariable) obj);
        }
        if (obj instanceof Variable) {
            return new VariableExpression((Variable) obj);
        }
        return new IdentifierExpression(str, obj);
    }

    /**
     * Gets the identifier name.
     *
     * @return The identifier name.
     * @since 3.0.0
     */
    public @NotNull String name() {
        return name;
    }

    public Object target() {
        return target;
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitIdentifier(this);
    }

    @Override
    public String toString() {
        return "Identifier(" + name + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentifierExpression that = (IdentifierExpression) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}