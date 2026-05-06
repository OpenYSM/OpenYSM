package com.elfmcys.yesstevemodel.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

public class BinaryOperationExpression implements Expression {

    private final Expression left;

    private final Expression right;

    public BinaryOperationExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public <R> R visit(@NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitBinaryOperation(this);
    }

    public Expression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }
}