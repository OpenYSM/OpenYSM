package com.elfmcys.yesstevemodel.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Ternary conditional expression implementation, similar to
 * "if {...} else {...}" expressions in other languages.
 *
 * <p>If the {@code conditional} expression evaluates to a
 * truthy value, then {@code trueExpression} is evaluated
 * as the result, otherwise, {@code falseExpression} is.</p>
 *
 * <p>Example ternary conditional expressions: {@code true ? 1 : 0},
 * {@code (age > 18) ? 'adult' : 'minor'}, {@code open ? 'open' : 'closed'}</p>
 *
 * @since 3.0.0
 */
public final class TernaryConditionalExpression implements Expression {

    private final Expression conditional;

    private final Expression trueExpression;

    private final Expression falseExpression;

    public TernaryConditionalExpression(
            final @NotNull Expression conditional,
            final @NotNull Expression trueExpression,
            final @NotNull Expression falseExpression
    ) {
        this.conditional = requireNonNull(conditional, "conditional");
        this.trueExpression = requireNonNull(trueExpression, "trueExpression");
        this.falseExpression = requireNonNull(falseExpression, "falseExpression");
    }

    /**
     * Gets the expression condition.
     *
     * @since 3.0.0
     */
    public @NotNull Expression condition() {
        return conditional;
    }

    /**
     * Gets the expression that should be used when
     * condition is evaluated as a truthy value.
     *
     * @since 3.0.0
     */
    public @NotNull Expression trueExpression() {
        return trueExpression;
    }


    /**
     * Gets the expression that should be used when
     * condition is evaluated as a falsy value.
     *
     * @since 3.0.0
     */
    public @NotNull Expression falseExpression() {
        return falseExpression;
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitTernaryConditional(this);
    }

    @Override
    public String toString() {
        return "TernaryCondition(" + conditional + ", "
                + trueExpression + ", "
                + falseExpression + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TernaryConditionalExpression that = (TernaryConditionalExpression) o;
        return conditional.equals(that.conditional)
                && trueExpression.equals(that.trueExpression)
                && falseExpression.equals(that.falseExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditional, trueExpression, falseExpression);
    }
}