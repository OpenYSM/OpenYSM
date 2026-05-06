package com.elfmcys.yesstevemodel.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

/**
 * The expression interface. It's the super-interface for
 * all the expression types.
 *
 * <p>Expressions are evaluable parts of code, expressions
 * are emitted by the parser.</p>
 *
 * <p>In Molang, almost every expression evaluates to a numerical
 * value</p>
 *
 * @since 3.0.0
 */
public interface Expression {

    /**
     * Visits this expression with the given visitor.
     *
     * @param visitor The expression visitor
     * @param <R>     The visit result return type
     * @return The visit result
     * @since 3.0.0
     */
    <R> R visit(final @NotNull ExpressionVisitor<R> visitor);

}