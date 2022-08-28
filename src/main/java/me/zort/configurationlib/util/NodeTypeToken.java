package me.zort.configurationlib.util;

import me.zort.configurationlib.configuration.Node;

public class NodeTypeToken<T extends Node> {

    private final Class<T> typeClass;

    public NodeTypeToken(Class<T> typeClass) {
        this.typeClass = typeClass;
    }

    public Class<T> getTypeClass() {
        return typeClass;
    }

}
