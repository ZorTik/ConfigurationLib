package me.zort.configurationlib.configuration;

import com.google.common.primitives.Primitives;
import me.zort.configurationlib.annotation.NodeName;
import me.zort.configurationlib.util.NodeTypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SectionNode<L> implements Node<L> {

    public abstract Node<L> get(String path);
    public abstract Collection<Node<L>> getNodes();

    /**
     * Updates this node's values from the provided mapped
     * object.
     * This method does not create new nodes, only updates
     * them!
     *
     * @param from The mapped object to update from.
     */
    public void set(Object from) {
        if(isPrimitive(from.getClass())) {
            // Primitive values can't be passed to sections!
            return;
        }
        for(Field field : from.getClass().getDeclaredFields()) {
            if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(from);
                if(value == null) {
                    continue;
                }
                String name;
                if(field.isAnnotationPresent(NodeName.class)) {
                    name = field.getAnnotation(NodeName.class).value();
                } else {
                    name = field.getName();
                }
                getNodes().stream()
                        .filter(n -> n.getName().equals(name))
                        .findFirst()
                        .ifPresent(node -> node.set(value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T map(Class<T> typeClass) {
        try {
            if(Primitives.isWrapperType(Primitives.wrap(typeClass))) {
                // We cannot map sections to primitive types since
                // sections are not leaf nodes.
                return null;
            }
            return map(typeClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T map(T obj) {
        Class<?> typeClass = obj.getClass();
        if(Primitives.isWrapperType(Primitives.wrap(typeClass))) {
            return null;
        }
        for(Node<L> node : getNodes()) {
            try {
                Field field = typeClass.getDeclaredField(node.getName());
                if(Modifier.isTransient(field.getModifiers())) {
                    // Transient fields are skipped.
                    continue;
                }
                field.setAccessible(true);
                Object value = null;
                if(node instanceof SimpleNode && isPrimitive(field.getClass())) {
                    value = ((SimpleNode<L>) node).get();
                } else if(node instanceof SectionNode) {
                    value = ((SectionNode<L>) node).map(field.getType());
                }
                if(value != null) {
                    // Null values are skipped.
                    try {
                        field.set(obj, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NoSuchFieldException ignored) {}
        }
        return obj;
    }

    public SimpleNode<L> getSimple(String path) {
        return (SimpleNode<L>) get(path);
    }

    public SectionNode<L> getSection(String path) {
        return (SectionNode<L>) get(path);
    }

    public <T extends Node<?>> List<T> getNodes(NodeTypeToken<T> type) {
        Class<T> typeClass = type.getTypeClass();
        return getNodes()
                .stream()
                .filter(typeClass::isInstance)
                .map(typeClass::cast)
                .collect(Collectors.toList());
    }

    private boolean isPrimitive(Class<?> clazz) {
        return Primitives.isWrapperType(Primitives.wrap(clazz));
    }

}
