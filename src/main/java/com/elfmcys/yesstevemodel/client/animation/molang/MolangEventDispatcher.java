package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatLists;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.util.List;

public class MolangEventDispatcher {

    public static final String PLAYER_INIT = "player_init";

    public static final String PLAYER_UPDATE = "player_update";

    public static final String SYNC = "sync";

    public static final String DEFER = "defer";

    public static IValue createExpression(List<IValue> list, FloatArrayList floatArrayList) {
        return createUpdateExpression(list, floatArrayList != null ? floatArrayList : FloatLists.emptyList());
    }

    public static IValue createInitExpression(List<IValue> list) {
        return createUpdateExpression(list, ObjectLists.emptyList());
    }

    public static IValue createUpdateExpression(List<IValue> list, List<?> list2) {
        return evaluator -> {
            Object entity = evaluator.entity();
            if (entity instanceof IContext context) {
                for (IValue value : list) {
                    context.callFunction(evaluator, value, list2);
                }
                return null;
            }
            return null;
        };
    }
}