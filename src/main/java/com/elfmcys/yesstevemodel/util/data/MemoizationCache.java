package com.elfmcys.yesstevemodel.util.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MemoizationCache<T, U> {

    private final Map<T, U> cache = new ConcurrentHashMap();

    private MemoizationCache() {
    }

    public static <T, U> Function<T, U> memoize(Function<T, U> function) {
        return new MemoizationCache().wrapFunction(function);
    }

    private Function<T, U> wrapFunction(Function<T, U> function) {
        return obj -> {
            return this.cache.computeIfAbsent(obj, function);
        };
    }
}