package com.elfmcys.yesstevemodel.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Expression implementation for binary expressions
 * (expressions composed by <b>two</b> other expressions)
 *
 * <p>Example binary expressions: {@code 1 + 1}, {@code 5 * 9},
 * {@code a == b}, {@code a < b}, {@code true ?? false}</p>
 *
 * @since 3.0.0
 */
public final class BinaryExpression implements Expression {

    private final Op op;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(
            final @NotNull Op op,
            final @NotNull Expression left,
            final @NotNull Expression right
    ) {
        this.op = requireNonNull(op, "op");
        this.left = requireNonNull(left, "left");
        this.right = requireNonNull(right, "right");
    }

    /**
     * Gets the binary expression type/operation.
     *
     * @return The expression operation.
     * @since 3.0.0
     */
    public @NotNull Op op() {
        return op;
    }

    /**
     * Gets the left-hand expression for this
     * binary expression.
     *
     * @return The left-hand expression
     * @since 3.0.0
     */
    public @NotNull Expression left() {
        return left;
    }

    /**
     * Gets the right-hand expression for this
     * binary expression.
     *
     * @return The right-hand expression
     * @since 3.0.0
     */
    public @NotNull Expression right() {
        return right;
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitBinary(this);
    }

    @Override
    public String toString() {
        return op.name() + "(" + left + ", " + right + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryExpression that = (BinaryExpression) o;
        if (op != that.op) return false;
        if (!left.equals(that.left)) return false;
        return right.equals(that.right);
    }

    @Override
    public int hashCode() {
        int result = op.hashCode();
        result = 31 * result + left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    public enum Op {
        AND(1800, 0),
        OR(1600, 1),
        LT(2200, 2),
        LTE(2200, 3),
        GT(2200, 4),
        GTE(2200, 5),
        ADD(2400, 6),
        SUB(2400, 7),
        MUL(2600, 8),
        DIV(2600, 9),
        ARROW(3000, 10),        // ?
        NULL_COALESCE(1200, 11),
        ASSIGN(1, 12),
        CONDITIONAL(1400, 13),      // ?
        EQ(2000, 14),
        NEQ(2000, 15);

        private final int precedence;
        private final int index;

        Op(final int precedence, final int index) {
            this.precedence = precedence;
            this.index = index;
        }

        public int precedence() {
            return precedence;
        }

        public int index() {
            return index;
        }
    }
}