package me.zort.configurationlib;

import me.zort.configurationlib.exception.ConfigurationException;

public interface SimpleNode<L> extends Node<L> {

    void set(Object value);
    Object get();

    default Object orElse(Object other) {
        Object value = get();
        return value != null ? value : other;
    }

    default String getAsString() throws ConfigurationException {
        if(!isString()) throw new ConfigurationException(this, "Node " + getName() + " is not string!");
        return (String) get();
    }

    default int getAsInt() {
        Object obj = get();
        try {
            return (int) obj;
        } catch(Exception ex) {
            return Integer.parseInt(getAsString());
        }
    }

    default double getAsDouble() {
        Object obj = get();
        try {
            return (double) obj;
        } catch(Exception ex) {
            return Double.parseDouble(getAsString());
        }
    }

    default boolean getAsBoolean() {
        Object obj = get();
        try {
            return (boolean) obj;
        } catch(Exception ex) {
            return Boolean.parseBoolean(getAsString());
        }
    }

    default boolean isString() {
        return get() instanceof String;
    }

}
