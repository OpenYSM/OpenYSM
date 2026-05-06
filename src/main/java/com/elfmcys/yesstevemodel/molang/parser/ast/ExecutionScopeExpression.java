package com.elfmcys.yesstevemodel.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class ExecutionScopeExpression implements Expression {

    private final List<Expression> expressions;

    public ExecutionScopeExpression(final @NotNull List<Expression> expressions) {
        this.expressions = Objects.requireNonNull(expressions, "expressions");
    }

    /**
     * Returns the expressions inside this
     * execution scope, never null
     */
    public @NotNull List<Expression> expressions() {
        return expressions;
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitExecutionScope(this);
    }

    @Override
    public String toString() {
        return "ExecutionScope(" + this.expressions + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionScopeExpression that = (ExecutionScopeExpression) o;
        return expressions.equals(that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }
}