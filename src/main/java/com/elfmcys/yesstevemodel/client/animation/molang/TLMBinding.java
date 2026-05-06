package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.util.data.LazySupplier;

public class TLMBinding extends ContextBinding {

    public static final LazySupplier<TLMBinding> INSTANCE = new LazySupplier<>(TLMBinding::new);

    public TLMBinding() {
        TouhouLittleMaidCompat.registerMaidAnimStates(this);
    }
}