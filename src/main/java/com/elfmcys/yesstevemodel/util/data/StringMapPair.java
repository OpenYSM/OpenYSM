package com.elfmcys.yesstevemodel.util.data;

public class StringMapPair {

    private final String key;

    private final OrderedStringMap<String, String> valueMap;

    public StringMapPair(String key, OrderedStringMap<String, String> valueMap) {
        this.key = key;
        this.valueMap = valueMap;
    }

    public String getKey() {
        return this.key;
    }

    public OrderedStringMap<String, String> getValueMap() {
        return this.valueMap;
    }
}