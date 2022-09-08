package me.zort.configurationlib.configuration;

import com.google.common.primitives.Primitives;
import me.zort.configurationlib.annotation.NodeName;
import me.zort.configurationlib.util.NodeTypeToken;
import me.zort.configurationlib.util.Placeholders;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
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
        return map(typeClass, new Placeholders());
    }

    public <T> T map(Class<T> typeClass, Placeholders placeholders) {
        try {
            if(Primitives.isWrapperType(Primitives.wrap(typeClass))) {
                // We cannot map sections to primitive types since
                // sections are not leaf nodes.
                return null;
            }
            return map(typeClass.getDeclaredConstructor().newInstance(), placeholders);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T map(T obj) {
        return map(obj, new Placeholders());
    }

    /**
     * Tries top map this section to provided object. This method assigns values to the fields
     * according to the node types and rules specified by child classes.
     *
     * @param obj The object to map to.
     * @return The mapped object.
     * @param <T> The type of the object to map to.
     */
    public <T> T map(T obj, Placeholders placeholders) {
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
                Object value = buildValue(field, node, placeholders);
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

    /**
     * This method is used for building value for specific field in mapping class
     * according to node. Field is found by getting field from class by name of the
     * node.
     * <p>
     * After building (returning) the value in this method, the value is assigned
     * to the field if is not null.
     * <p>
     * Reason for this method being separated from method where is used is that
     * I want to allow child classes to override it and define their own field type
     * rules.
     *
     * @param field Field to build value for.
     * @param node Node to build value from.
     * @return Value to assign to field.
     */
    public Object buildValue(Field field, Node<L> node, Placeholders placeholders) {
        Object value = null;
        if(node instanceof SimpleNode && isPrimitive(field.getClass())) {
            value = ((SimpleNode<L>) node).get();
        } else if(node instanceof SectionNode) {
            Class<?> contentType;
            if(List.class.isAssignableFrom(field.getType())
            && !isPrimitive(contentType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])) {
                List list = new ArrayList();
                ((SectionNode<Object>) node).getNodes(NodeTypes.SECTION)
                        .forEach(sn -> {
                            list.add(sn.map(contentType));
                        });
                return list;
            } else {
                value = ((SectionNode<L>) node).map(field.getType());
            }
        }
        return value;
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
