package com.elfmcys.yesstevemodel.molang.runtime.binding;

import com.elfmcys.yesstevemodel.molang.parser.ast.AssignableVariableExpression;
import com.elfmcys.yesstevemodel.molang.parser.ast.ExecutionScopeExpression;
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression;
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluatorImpl;
import com.elfmcys.yesstevemodel.molang.runtime.Function;

/**
 * Class holding some default bindings and
 * static utility methods for ease working
 * with bindings
 */
public final class StandardBindings {
    private static final int MAX_LOOP_ROUND = 1024;

    public static final Function LOOP_FUNC = (ctx, args) -> {
        if (args.size() < 2) {
            return null;
        }

        int n = Math.min((int) Math.round(args.getAsDouble(ctx, 0)), MAX_LOOP_ROUND);
        Object expr = args.getExpression(1);

        if (expr instanceof ExecutionScopeExpression)
            ((ExpressionEvaluatorImpl<?>) ctx).loopFunciton((ExecutionScopeExpression) expr, n);
        return null;
    };

    public static final Function FOR_EACH_FUNC = (ctx, args) -> {
        // Parameters:
        // - any:              Variable
        // - array:            Any array
        // - CallableBinding:  The looped expressions

        if (args.size() != 3) {
            return null;
        }
        Expression variableExpr = args.getExpression(0);

        if (!(variableExpr instanceof AssignableVariableExpression)) {
            // first argument must be an access expression,
            // e.g. 'variable.test', 'v.pig', 't.entity' or
            // 't.entity.location.world'
            return null;
        }
        final AssignableVariable variableAccess = ((AssignableVariableExpression) variableExpr).target();

        Expression exper = args.getExpression(2);
        if (exper instanceof ExecutionScopeExpression executionScopeExpression) {
            Object obj = args.getValue(ctx, 1);
            if (obj instanceof Iterable) {
                ((ExpressionEvaluatorImpl<?>) ctx).forEachFunction(executionScopeExpression, variableAccess, (Iterable<?>) obj);
                return null;
            }
            return null;
        }
        return null;
    };
}