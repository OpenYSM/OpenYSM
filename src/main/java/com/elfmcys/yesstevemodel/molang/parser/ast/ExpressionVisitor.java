package com.elfmcys.yesstevemodel.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

public interface ExpressionVisitor<R> {
    R visit(@NotNull Expression expression);

    default R visitFloat(@NotNull FloatExpression expression) {
        return visit(expression);
    }

    default R visitString(@NotNull StringExpression expression) {
        return visit(expression);
    }

    default R visitIdentifier(@NotNull IdentifierExpression expression) {
        return visit(expression);
    }

    default R visitVariable(@NotNull VariableExpression expression) {
        return visit(expression);
    }

    default R visitAssignableVariable(@NotNull AssignableVariableExpression expression) {
        return visit(expression);
    }

    default R visitStruct(@NotNull StructAccessExpression expression) {
        return visit(expression);
    }

    /**
     * Evaluate for ternary conditional expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 3.0.0
     */
    default R visitTernaryConditional(@NotNull TernaryConditionalExpression expression) {
        return visit(expression);
    }

    /**
     * Evaluate for unary expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 3.0.0
     */
    default R visitUnary(@NotNull UnaryExpression expression) {
        return visit(expression);
    }

    /**
     * Evaluate for execution scope expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 3.0.0
     */
    default R visitExecutionScope(@NotNull ExecutionScopeExpression expression) {
        return visit(expression);
    }

    /**
     * Evaluate for binary expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 3.0.0
     */
    default R visitBinary(@NotNull BinaryExpression expression) {
        return visit(expression);
    }

    /**
     * Evaluate for call expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 3.0.0
     */
    default R visitCall(@NotNull CallExpression expression) {
        return visit(expression);
    }

    /**
     * Evaluate for statement expression.
     *
     * @param expression The expression.
     * @return The result.
     * @since 3.0.0
     */
    default R visitStatement(@NotNull StatementExpression expression) {
        return visit(expression);
    }

    default R visitBinaryOperation(BinaryOperationExpression expression) {
        return visit(expression);
    }
}