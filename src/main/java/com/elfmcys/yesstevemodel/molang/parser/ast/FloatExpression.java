package com.elfmcys.yesstevemodel.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class FloatExpression implements Expression {

    public static final FloatExpression ZERO = new FloatExpression(0.0f);

    public static final FloatExpression ONE = new FloatExpression(1.0f);

    private final float value;

    public FloatExpression(float value) {
        this.value = value;
    }

    public float value() {
        return this.value;
    }

    @Override
    public <R> R visit(@NotNull ExpressionVisitor<R> expressionVisitor) {
        return expressionVisitor.visitFloat(this);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && getClass() == obj.getClass() && Float.compare(((FloatExpression) obj).value, this.value) == 0;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.value));
    }
}