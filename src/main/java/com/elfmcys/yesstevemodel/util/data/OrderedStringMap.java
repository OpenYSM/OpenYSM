package com.elfmcys.yesstevemodel.util.data;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderedStringMap<K, V> implements Map<K, V> {

    private final ObjectList<K> keys;

    private final ObjectList<V> valuesList;

    private final Object2ObjectArrayMap<K, V> arrayMap;

    private final Object2ObjectOpenHashMap<K, V> hashMap;

    public OrderedStringMap(K[] kArr, V[] vArr) {
        this.keys = ObjectLists.unmodifiable(ObjectArrayList.wrap(kArr));
        this.valuesList = ObjectLists.unmodifiable(ObjectArrayList.wrap(vArr));
        this.arrayMap = new Object2ObjectArrayMap<>(kArr, vArr);
        this.hashMap = new Object2ObjectOpenHashMap<>(this.arrayMap);
    }

    public OrderedStringMap(Object2ObjectArrayMap<K, V> object2ObjectArrayMap) {
        Object[] array = object2ObjectArrayMap.keySet().toArray(new Object[0]);
        Object[] array2 = object2ObjectArrayMap.values().toArray(new Object[0]);
        this.keys = (ObjectList<K>) ObjectLists.unmodifiable(ObjectArrayList.wrap(array));
        this.valuesList = (ObjectList<V>) ObjectLists.unmodifiable(ObjectArrayList.wrap(array2));
        this.arrayMap = new Object2ObjectArrayMap<>(array, array2);
        this.hashMap = new Object2ObjectOpenHashMap<>(this.arrayMap);
    }

    @Override
    public int size() {
        return this.hashMap.size();
    }

    public K getKeyAt(int i) {
        return this.keys.get(i);
    }

    public List<K> getKeys() {
        return this.keys;
    }

    public V getValueAt(int i) {
        return this.valuesList.get(i);
    }

    public List<V> getValuesList() {
        return this.valuesList;
    }

    @Override
    public boolean isEmpty() {
        return this.hashMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object obj) {
        return this.hashMap.containsKey(obj);
    }

    @Override
    public boolean containsValue(Object obj) {
        return this.hashMap.containsValue(obj);
    }

    @Override
    public V get(Object obj) {
        return this.hashMap.get(obj);
    }

    @Override
    @Nullable
    public V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public Set<K> keySet() {
        return this.arrayMap.keySet();
    }

    @Override
    @NotNull
    public Collection<V> values() {
        return this.arrayMap.values();
    }

    @Override
    @NotNull
    public Set<Map.Entry<K, V>> entrySet() {
        return this.arrayMap.entrySet();
    }
}