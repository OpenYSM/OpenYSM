package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable;

public interface IValueEvaluator<TValue, TContext> {
    TValue eval(TContext ctx);
}