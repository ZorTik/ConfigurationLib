package me.zort.configurationlib.configuration.virtual;

import me.zort.configurationlib.SimpleNode;

import java.util.Objects;

public class VirtualSimpleNode implements SimpleNode<Object> {

    private final VirtualSectionNode parent;
    private final String name;
    private Object value;

    public VirtualSimpleNode(VirtualSectionNode parent, String name, Object value) {
        this.parent = parent;
        this.name = name;
        this.value = value;

        Objects.requireNonNull(parent);
    }

    @Override
    public void putSelf(Object location) {
        // Virtual configuration stays in memory.
    }

    @Override
    public void set(Object value) {
        this.value = value;
    }

    @Override
    public Object get() {
        return value;
    }

    @Override
    public String getPath() {
        return parent.getPath() + "." + name;
    }
}
