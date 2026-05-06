package com.elfmcys.yesstevemodel;

import io.netty.util.internal.ObjectCleaner;

import java.lang.ref.PhantomReference;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ResourceCleanupHelper {

    private static final WeakHashMap<Object, PhantomReference<Object>> cleanupReferences = new WeakHashMap<>();

    public static <T> void registerCleanup(Object obj, T t, Consumer<T> consumer) {
        ObjectCleaner.register(obj, () -> {
            consumer.accept(t);
        });
    }

    public static <T0, T1> void registerBiCleanup(Object obj, T0 t0, T1 t1, BiConsumer<T0, T1> biConsumer) {
        ObjectCleaner.register(obj, () -> {
            biConsumer.accept(t0, t1);
        });
    }
}