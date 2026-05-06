package com.elfmcys.yesstevemodel.geckolib3.util.json;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;

public class JsonMolangUtils {
    // 默认不合并
    public static IValue[] getExpressions(@Nullable JsonElement element, MolangParser parser, boolean mergeMultilineExpr) {
        if (element == null) return new IValue[]{};

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return new IValue[]{parser.parseExpression(element.getAsString(), false)};
        }
        if (!element.isJsonArray()) return new IValue[]{};

        JsonArray array = element.getAsJsonArray();

        if (mergeMultilineExpr) {
            StringBuilder parserText = new StringBuilder();

            for (int i = 0; i < array.size(); i++) {
                parserText.append(array.get(i).getAsString());
                // 如果不是最后一行，就追加一个换行符
                if (i < array.size() - 1) {
                    parserText.append("\n");
                }
            }

            return new IValue[]{parser.parseExpression(parserText.toString(), false)};
        } else {
            IValue[] values = new IValue[array.size()];
            for (int i = 0; i < array.size(); i++) {
                String parserText = array.get(i).getAsString();
                values[i] = parser.parseExpression(parserText, false);
            }
            return values;
        }
    }
}
