package com.elfmcys.yesstevemodel.molang.runtime.binding;

/**
 * Represents an object-like binding,
 * these objects can have properties
 * (or fields) that can be read and
 * sometimes written
 */
public interface ObjectBinding {
    ObjectBinding EMPTY = name -> null;

    /**
     * Gets the property value in this
     * object with the given {@code name}
     */
    Object getProperty(String name);
}