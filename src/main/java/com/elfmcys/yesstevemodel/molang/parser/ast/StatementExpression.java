package com.elfmcys.yesstevemodel.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class StatementExpression implements Expression {

    private final Op op;

    public StatementExpression(final @NotNull Op op) {
        this.op = Objects.requireNonNull(op, "op");
    }

    /**
     * Gets the operation/type of this statement.
     *
     * @return The statement operation/type.
     * @since 3.0.0
     */
    public @NotNull Op op() {
        return op;
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitStatement(this);
    }


    /**
     * Enum containing all the possible operations/types
     * of statement expressions.
     *
     * @since 3.0.0
     */
    public enum Op {
        /**
         * The break statement type
         *
         * @since 3.0.0
         */
        BREAK,

        /**
         * The continue statement type
         *
         * @since 3.0.0
         */
        CONTINUE
    }
}