package me.zort.configurationlib.configuration.virtual;

import lombok.Getter;
import me.zort.configurationlib.Node;
import me.zort.configurationlib.NodeTypes;
import me.zort.configurationlib.SectionNode;
import me.zort.configurationlib.util.NodeTypeToken;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualSectionNode extends SectionNode<Object> {

    @Getter
    private final String name;
    private final Map<String, Node<Object>> children;

    public VirtualSectionNode() {
        this(null, "", new ConcurrentHashMap<>());
    }

    private VirtualSectionNode(@Nullable SectionNode<Object> parent, String name, Map<String, Node<Object>> children) {
        super(parent);
        this.name = name;
        this.children = children;
    }

    @Override
    public void putSelf(Object location) {
        // Virtual configuration stays in memory.
    }

    @Override
    public Node<Object> createNode(String key, @Nullable Object value, NodeTypeToken<?> type) {
        if(type.getType().equals(NodeTypes.Type.SIMPLE)) {
            return new VirtualSimpleNode(this, key, value);
        } else if(type.getType().equals(NodeTypes.Type.SECTION)) {
            return new VirtualSectionNode(this, key, new ConcurrentHashMap<>());
        } else {
            throw new IllegalArgumentException("Unsupported node type: " + type.getType());
        }
    }

    @Override
    public void deleteNode(String key) {
        children.remove(key);
    }

    @Override
    public void set(String key, Node<Object> node) {
        children.put(key, node);
    }

    @Override
    public Collection<Node<Object>> getNodes() {
        return children.values();
    }

    @Override
    public String getPath() {
        String path = "";

        SectionNode<Object> parent = getParent();
        if(parent != null && !parent.isHighestLevel())
            path = parent.getPath() + ".";

        path += name;

        return path;
    }
}
