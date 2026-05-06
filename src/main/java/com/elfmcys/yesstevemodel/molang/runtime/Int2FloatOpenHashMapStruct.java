package com.elfmcys.yesstevemodel.molang.runtime;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

public class Int2FloatOpenHashMapStruct implements Struct {

    private final Int2FloatOpenHashMap properties;

    public Int2FloatOpenHashMapStruct(Int2FloatOpenHashMap properties) {
        this.properties = properties;
    }

    @Override
    public Object getProperty(int name) {
        return this.properties.get(name);
    }

    @Override
    public void putProperty(int name, Object value) {
        this.properties.put(name, ValueConversions.asFloat(value));
    }

    public void merge(Int2FloatMap int2FloatMap) {
        this.properties.putAll(int2FloatMap);
    }

    @Override
    public Struct copy() {
        HashMapStruct hashMapStruct = new HashMapStruct(true);
        for (Int2FloatMap.Entry entry : this.properties.int2FloatEntrySet()) {
            hashMapStruct.putProperty(entry.getIntKey(), entry.getFloatValue());
        }
        return hashMapStruct;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("roaming{");
        boolean z = true;
        for (Int2FloatMap.Entry entry : this.properties.int2FloatEntrySet()) {
            if (!z) {
                sb.append(", ");
            }
            z = false;
            sb.append(String.format("%s=%s", StringPool.getString(entry.getIntKey()), Float.valueOf(entry.getFloatValue())));
        }
        sb.append("}");
        return sb.toString();
    }
}