package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.MathBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.QueryBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.ControllerVariableBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.ScopedVariableBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.TempVariableRegistry;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import com.elfmcys.yesstevemodel.molang.runtime.binding.StandardBindings;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrimaryBinding implements ObjectBinding {

    public final Object2ReferenceOpenHashMap<String, Object> bindings = new Object2ReferenceOpenHashMap<>();

    public final ScopedVariableBinding scopedBinding = new ScopedVariableBinding();

    public final ControllerVariableBinding foreignBinding = new ControllerVariableBinding();

    public final TempVariableRegistry tempBinding = new TempVariableRegistry();

    private final List<CloseVariable> closeables;

    private final List<ResetVariable> resettables;

    public PrimaryBinding(@Nullable Map<String, Object> map) {
        if (map != null) {
            this.bindings.putAll(map);
        }
        this.bindings.put("math", MathBinding.INSTANCE);
        this.bindings.put("query", QueryBinding.INSTANCE);
        this.bindings.put("q", QueryBinding.INSTANCE);
        this.bindings.put("loop", StandardBindings.LOOP_FUNC);
        this.bindings.put("for_each", StandardBindings.FOR_EACH_FUNC);
        this.bindings.put("variable", this.scopedBinding);
        this.bindings.put("v", this.scopedBinding);
        this.bindings.put("context", this.foreignBinding);
        this.bindings.put("c", this.foreignBinding);
        this.bindings.put("temp", this.tempBinding);
        this.bindings.put("t", this.tempBinding);
        this.closeables = this.bindings.values().stream().filter(obj -> obj instanceof CloseVariable).map(obj2 -> (CloseVariable) obj2).collect(Collectors.toList());
        this.resettables = this.bindings.values().stream().filter(obj3 -> obj3 instanceof ResetVariable).map(obj4 -> (ResetVariable) obj4).collect(Collectors.toList());
    }

    @Override
    public Object getProperty(String str) {
        return this.bindings.get(str);
    }

    public void reset() {
        for (ResetVariable resetVariable : this.resettables) {
            resetVariable.reset();
        }
    }

    public void dispose() {
        for (CloseVariable closeVariable : this.closeables) {
            closeVariable.dispose();
        }
    }
}