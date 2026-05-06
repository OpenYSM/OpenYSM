package com.elfmcys.yesstevemodel.molang.runtime.binding;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.molang.parser.ast.StringExpression;
import org.jetbrains.annotations.Nullable;

public final class ValueConversions {
    public static boolean asBoolean(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Boolean) {
            return (Boolean)obj;
        }
        if (obj instanceof Number) {
            float f = ((Number) obj).floatValue();
            return !Float.isNaN(f) && f != 0.0f;
        }
        return true;
    }

    public static float asFloat(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            float f = ((Number)obj).floatValue();
            if (!Float.isNaN(f)) {
                return f;
            }
            return 0.0f;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj ? 1 : 0;
        }
        return 1;
    }

    public static int asInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if ((obj instanceof Number)) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1 : 0;
        }
        return 1;
    }

    public static double asDouble(final @Nullable Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            double d = ((Number) obj).doubleValue();
            if (!Double.isNaN(d)) {
                return d;
            }
            return ((Number) obj).doubleValue();
        }
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1 : 0;
        }
        return 1;
    }

    public static String asString(final @Nullable Object obj) {
        if (obj instanceof StringExpression) {
            return ((StringExpression) obj).getName();
        }
        if (obj instanceof String) {
            return ((String) obj);
        }
        return null;
    }

    public static int asStringId(@Nullable Object obj) {
        if (obj instanceof StringExpression) {
            return ((StringExpression) obj).getPath();
        }
        if (obj instanceof String) {
            return StringPool.computeIfAbsent((String) obj);
        }
        return StringPool.EMPTY_ID;
    }
}