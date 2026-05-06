package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashMap;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashSet;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import it.unimi.dsi.fastutil.ints.IntIterator;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class VariableStorage implements IScopedVariableStorage, IForeignVariableStorage {

    private static final int SCOPED_INIT_CAPACITY = 16;

    private final TempVariableStorage localVariables = new TempVariableStorage();

    private final PooledStringHashMap<VariableValueHolder> scopedMap = new PooledStringHashMap<>(SCOPED_INIT_CAPACITY);

    private PooledStringHashMap<VariableValueHolder> publicMap = new PooledStringHashMap<>();

    @Override
    public Object getScoped(int name) {
        VariableValueHolder valueHolder = scopedMap.computeIfAbsent(name, n -> new VariableValueHolder());
        return valueHolder.value;
    }

    @Override
    public void setScoped(int name, Object value) {
        VariableValueHolder valueHolder = scopedMap.computeIfAbsent(name, n -> new VariableValueHolder());
        valueHolder.value = value;
    }

    @Override
    public Object getPublic(int name) {
        VariableValueHolder valueHolder = publicMap.get(name);
        if (valueHolder != null) {
            return valueHolder.value;
        } else {
            return null;
        }
    }

    public TempVariableStorage getLocalVariables() {
        return this.localVariables;
    }

    // 注意this.publicMap线程安全
    public void initialize(@Nullable PooledStringHashSet publicVariableNames) {
        if (publicVariableNames != null) {
            PooledStringHashMap<VariableValueHolder> newPublicMap = new PooledStringHashMap<>();
            for (int publicVariableName : publicVariableNames) {
                VariableValueHolder value = new VariableValueHolder();
                scopedMap.put(publicVariableName, value);
                newPublicMap.put(publicVariableName, value);
            }
            this.publicMap = newPublicMap;
            return;
        }
        this.publicMap = new PooledStringHashMap<>(0);
    }

    public void forEachPropertyName(Consumer<String> consumer) {
        IntIterator it = this.scopedMap.keySet().iterator();
        while (it.hasNext()) {
            consumer.accept(StringPool.getString(it.next().intValue()));
        }
    }

    private static class VariableValueHolder {
        public Object value = null;
    }
}