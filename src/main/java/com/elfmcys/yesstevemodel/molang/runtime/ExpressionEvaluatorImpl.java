package com.elfmcys.yesstevemodel.molang.runtime;

import com.elfmcys.yesstevemodel.molang.parser.ast.*;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public final class ExpressionEvaluatorImpl<TEntity> implements ExpressionEvaluator<TEntity>, ExpressionVisitor<Object> {
    private static final Evaluator[] BINARY_EVALUATORS = {
            bool((a, b) -> a.eval() && b.eval()),
            bool((a, b) -> a.eval() || b.eval()),
            compare((a, b) -> a.eval() < b.eval()),
            compare((a, b) -> a.eval() <= b.eval()),
            compare((a, b) -> a.eval() > b.eval()),
            compare((a, b) -> a.eval() >= b.eval()),
            (evaluator, a, b) -> {
                final Object aVal = a.visit(evaluator);
                final Object bVal = b.visit(evaluator);
                return ValueConversions.asFloat(aVal) + ValueConversions.asFloat(bVal);
            },
            arithmetic((a, b) -> a.eval() - b.eval()),
            arithmetic((a, b) -> a.eval() * b.eval()),
            arithmetic((a, b) -> {
                // Molang allows division by zero,
                // which is always equal to 0
                float dividend = a.eval();
                float divisor = b.eval();
                if (divisor == 0) return 0;
                else return dividend / divisor;
            }),
            (evaluator, a, b) -> { // arrow
                Object val = a.visit(evaluator);
                if (val == null) {
                    return null;
                }
                ExpressionEvaluatorImpl child = evaluator.createChild(val);
                Object res = b.visit(child);
                evaluator.returnValue = child.returnValue;
                return res;
            },
            (evaluator, a, b) -> { // null coalesce
                Object val = a.visit(evaluator);
                if (val == null) {
                    return b.visit(evaluator);
                } else {
                    return val;
                }
            },
            (evaluator, a, b) -> { // assignation
                Object val = b.visit(evaluator);
                if (a instanceof AssignableVariableExpression) {
                    AssignableVariable var = ((AssignableVariableExpression) a).target();
                    if (val instanceof Struct) {
                        val = ((Struct) val).copy();
                    }
                    var.assign(evaluator, val);
                } else if (a instanceof StructAccessExpression exp) {
                    if (val instanceof Struct) {
                        // 不允许结构体嵌套
                        return val;
                    }
                    Object value = exp.left().visit(evaluator);
                    if (value instanceof Struct) {
                        ((Struct) value).putProperty(exp.path(), val);
                    } else if (exp.left() instanceof AssignableVariableExpression) {
                        AssignableVariable variable = ((AssignableVariableExpression) exp.left()).target();
                        Struct struct = new HashMapStruct();
                        struct.putProperty(exp.path(), val);
                        variable.assign(evaluator, struct);
                    }
                }
                // TODO: (else case) This isn't fail-fast, we can only assign to access expressions
                return val;
            },
            (evaluator, a, b) -> { // conditional
                Object condition = a.visit(evaluator);
                if (ValueConversions.asBoolean(condition)) {
                    return b.visit(evaluator);
                }
                return null;
            }, (evaluator, a, b) -> {
                Object left = a.visit(evaluator);
                Object right = b.visit(evaluator);
                if (left == right)
                    return true;
                if (left instanceof Number || right instanceof Number)
                    return ValueConversions.asFloat(right) == ValueConversions.asFloat(left);
                if (left == null || right == null)
                    return false;
                if (left instanceof StringExpression)
                    return left.equals(right);
                if (right instanceof StringExpression)
                    return right.equals(left);
                return false;
            }, //eq
            (evaluator, a, b) -> {
                Object left = a.visit(evaluator);
                Object right = b.visit(evaluator);
                if (left == right)
                    return false;
                if (left instanceof Number || right instanceof Number)
                    return ValueConversions.asFloat(right) != ValueConversions.asFloat(left);
                if (left == null || right == null)
                    return true;
                if (left instanceof StringExpression)
                    return !left.equals(right);
                if (right instanceof StringExpression)
                    return !right.equals(left);
                return false;
            }
    };

    private final TEntity entity;

    private @Nullable Object returnValue;

    @Nullable
    private StatementExpression.Op op;

    private int cnt = 0;

    private int working = 0;

    public ExpressionEvaluatorImpl(@Nullable TEntity tentity) {
        this.entity = tentity;
    }

    private static Evaluator bool(BooleanOperator op) {
        return (evaluator, a, b) -> op.operate(
                () -> ValueConversions.asBoolean(a.visit(evaluator)),
                () -> ValueConversions.asBoolean(b.visit(evaluator))
        );
    }

    private static Evaluator compare(Comparator comp) {
        return (evaluator, a, b) -> comp.compare(
                () -> ValueConversions.asFloat(a.visit(evaluator)),
                () -> ValueConversions.asFloat(b.visit(evaluator))
        );
    }

    private static Evaluator arithmetic(ArithmeticOperator op) {
        return (evaluator, a, b) -> op.operate(
                () -> ValueConversions.asFloat(a.visit(evaluator)),
                () -> ValueConversions.asFloat(b.visit(evaluator))
        );
    }

    @Override
    public TEntity entity() {
        return this.entity;
    }

    @Override
    @Nullable
    public Object eval(@NotNull Expression expression) {
        try {
            return expression.visit(this);
        } finally {
            this.returnValue = null;
            this.op = null;
        }
    }

    @Override
    @Nullable
    public Object evalAll(@NotNull Iterable<Expression> iterable, boolean z) {
        if (z) {
            this.working++;
        }
        Object objValueOf = 0.0d;
        try {
            Iterator<Expression> it = iterable.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                objValueOf = it.next().visit(this);
                Object obj = popReturnValue();
                if (obj != null) {
                    objValueOf = obj;
                    break;
                }
            }
            return objValueOf;
        } finally {
            this.returnValue = null;
            this.op = null;
            if (z) {
                this.working--;
            }
        }
    }

    @NotNull
    public <TNewEntity> ExpressionEvaluatorImpl<TNewEntity> createChild(@Nullable TNewEntity tnewentity) {
        return new ExpressionEvaluatorImpl<>(tnewentity);
    }

    @Nullable
    private Object popReturnValue() {
        Object obj = this.returnValue;
        if (this.working == 0) {
            this.returnValue = null;
        }
        return obj;
    }

    @Override
    @Nullable
    public Object visitCall(@NotNull CallExpression expression) {
        return expression.function().evaluate(this, expression.arguments());
    }

    @Override
    public Object visitFloat(@NotNull FloatExpression floatExpression) {
        return Float.valueOf(floatExpression.value());
    }

    @Override
    public Object visitExecutionScope(@NotNull ExecutionScopeExpression executionScope) {
        Object objMo2074xaffeef43 = null;
        Iterator<Expression> it = executionScope.expressions().iterator();
        while (it.hasNext()) {
            objMo2074xaffeef43 = it.next().visit(this);
            Object obj = popReturnValue();
            if (obj != null) {
                return obj;
            }
            if (this.cnt > 0 && this.op != null) {
                return null;
            }
        }
        return objMo2074xaffeef43;
    }

    private boolean buildExecutionScope(@NotNull ExecutionScopeExpression executionScope) {
        this.cnt++;
        try {
            Iterator<Expression> it = executionScope.expressions().iterator();
            while (it.hasNext()) {
                it.next().visit(this);
                if (popReturnValue() != null) {
                    return true;
                }
                StatementExpression.Op op = this.op;
                this.op = null;
                if (op == StatementExpression.Op.CONTINUE) {
                    break;
                }
                if (op == StatementExpression.Op.BREAK) {
                    this.cnt--;
                    return true;
                }
            }
            this.cnt--;
            return false;
        } finally {
            this.cnt--;
        }
    }

    public void loopFunciton(@NotNull ExecutionScopeExpression executionScope, int n) {
        for (int i = 0; i < n && !buildExecutionScope(executionScope); i++) {
        }
    }

    public void forEachFunction(@NotNull ExecutionScopeExpression executionScope, AssignableVariable variableAccess, Iterable<?> iterable) {
        Iterator<?> it = iterable.iterator();
        while (it.hasNext()) {
            variableAccess.assign(this, it.next());
            if (buildExecutionScope(executionScope)) {
                return;
            }
        }
    }

    @Override
    public Object visitIdentifier(@NotNull IdentifierExpression identifierExpression) {
        throw new RuntimeException("Unknown identifier type");
    }

    @Override
    public Object visitVariable(@NotNull VariableExpression expression) {
        return expression.target().evaluate(this);
    }

    @Override
    public Object visitAssignableVariable(@NotNull AssignableVariableExpression expression) {
        return expression.target().evaluate(this);
    }

    @Override
    public Object visitStruct(@NotNull StructAccessExpression expression) {
        Object value = expression.left().visit(this);
        if (value instanceof Struct) {
            return ((Struct) value).getProperty(expression.path());
        } else {
            return null;
        }
    }

    @Override
    public Object visitBinary(@NotNull BinaryExpression expression) {
        return BINARY_EVALUATORS[expression.op().index()].eval(
                this,
                expression.left(),
                expression.right()
        );
    }

    @Override
    public Object visitBinaryOperation(BinaryOperationExpression expression) {
        Object objMo2074xaffeef43 = expression.getLeft().visit(this);
        Object objMo2074xaffeef432 = expression.getRight().visit(this);
        if (objMo2074xaffeef432 instanceof Number) {
            int iIntValue = ((Number) objMo2074xaffeef432).intValue();
            if (iIntValue < 0) {
                iIntValue = 0;
            }
            if (objMo2074xaffeef43 instanceof List list) {
                if (list.size() > iIntValue) {
                    return list.get(iIntValue);
                }
                return null;
            }
            return null;
        }
        return null;
    }

    @Override
    public Object visitUnary(@NotNull UnaryExpression expression) {
        Object value = expression.expression().visit(this);
        switch (expression.op()) {
            case LOGICAL_NEGATION:
                return !ValueConversions.asBoolean(value);
            case ARITHMETICAL_NEGATION:
                return -ValueConversions.asFloat(value);
            case RETURN: {
                this.returnValue = value;
                return 0D;
            }
            default:
                throw new IllegalStateException("Unknown operation");
        }
    }

    @Override
    public Object visitStatement(@NotNull StatementExpression expression) {
        switch (expression.op()) {
            case BREAK: {
                this.op = StatementExpression.Op.BREAK;
                break;
            }
            case CONTINUE: {
                this.op = StatementExpression.Op.CONTINUE;
                break;
            }
        }
        return null;
    }

    @Override
    public Object visitString(@NotNull StringExpression expression) {
        return expression;
    }

    @Override
    public Object visitTernaryConditional(@NotNull TernaryConditionalExpression expression) {
        Object obj = expression.condition().visit(this);
        obj = ValueConversions.asBoolean(obj)
                ? expression.trueExpression().visit(this)
                : expression.falseExpression().visit(this);
        return obj;
    }

    @Override
    public Object visit(@NotNull Expression expression) {
        throw new UnsupportedOperationException("Unsupported expression type: " + expression);
    }

    private interface Evaluator<TEntity> {
        Object eval(ExpressionEvaluatorImpl<TEntity> evaluator, Expression a, Expression b);
    }

    private interface BooleanOperator {
        boolean operate(LazyEvaluableBoolean a, LazyEvaluableBoolean b);
    }

    interface LazyEvaluableBoolean {
        boolean eval();
    }

    interface LazyEvaluableFloat {
        float eval();
    }

    private interface Comparator {
        boolean compare(LazyEvaluableFloat a, LazyEvaluableFloat b);
    }

    private interface ArithmeticOperator {
        float operate(LazyEvaluableFloat a, LazyEvaluableFloat b);
    }
}