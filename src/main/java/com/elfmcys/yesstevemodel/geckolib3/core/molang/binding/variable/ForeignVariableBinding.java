package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Variable;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
public class ForeignVariableBinding implements ObjectBinding, ResetVariable {

    private final Int2ReferenceOpenHashMap<ForeignVariable> variableMap = new Int2ReferenceOpenHashMap<>();

    @Override
    public Object getProperty(String name) {
        return this.variableMap.computeIfAbsent(StringPool.computeIfAbsent(name), ForeignVariable::new);
    }

    @Override
    public void reset() {
        this.variableMap.clear();
    }

    private record ForeignVariable(int name) implements Variable {
        @Override
        @SuppressWarnings("unchecked")
        public Object evaluate(@NotNull ExecutionContext<?> context) {
            IForeignVariableStorage storage = ((IContext<Object>) context.entity()).foreignStorage();
            if (storage != null) {
                return storage.getPublic(this.name);
            }
            return null;
        }
    }
}