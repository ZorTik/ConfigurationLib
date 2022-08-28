package me.zort.configurationlib.configuration;

import me.zort.configurationlib.exception.ConfigurationException;

public interface SimpleNode<L> extends Node<L> {

    Object get();

    default String getAsString() throws ConfigurationException {
        if(!isString()) throw new ConfigurationException(this, "Node " + getName() + " is not string!");
        return (String) get();
    }

    default int getAsInt() {
        return (int) getAsDouble();
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
