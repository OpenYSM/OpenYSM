package com.elfmcys.yesstevemodel.util.data;

import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.util.function.Supplier;

public class LazySupplier<T> extends LazyInitializer<T> {

    private final Supplier<T> supplier;

    public LazySupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T initialize() {
        return this.supplier.get();
    }
}