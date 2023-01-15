package me.zort.configurationlib.configuration.virtual;

import me.zort.configurationlib.Node;

public abstract class VirtualNode implements Node<Object> {

    @Override
    public void putSelf(Object location) {
        // Virtual configuration stays in memory.
    }

}
