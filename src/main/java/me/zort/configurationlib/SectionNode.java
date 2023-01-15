package me.zort.configurationlib;

import com.google.common.base.Defaults;
import com.google.common.base.Strings;
import com.google.common.primitives.Primitives;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.zort.configurationlib.annotation.NodeName;
import me.zort.configurationlib.annotation.ThisNodeId;
import me.zort.configurationlib.util.NodeTypeToken;
import me.zort.configurationlib.util.Placeholders;
import me.zort.configurationlib.util.Validator;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Represents a section node in configuration.
 *
 * @param <L> The type of the source.
 * @author ZorTik
 */
public abstract class SectionNode<L> implements Node<L> {

    private final Map<Class<?>, NodeAdapter<?, L>> adapters = new ConcurrentHashMap<>();

    @Getter(onMethod_ = {@Nullable})
    private final SectionNode<L> parent;
    private LogAdapter logAdapter;
    @Setter
    @Getter
    private boolean debug;

    public SectionNode(@Nullable SectionNode<L> parent) {
        this(parent, LogAdapter.DEFAULT);
    }

    public SectionNode(@Nullable SectionNode<L> parent, LogAdapter logAdapter) {
        this.parent = parent;
        this.logAdapter = logAdapter;
        this.debug = false;

        registerAdapter(Collection.class, new DefaultCollectionSerializer<>(this));
    }

    @ApiStatus.Internal
    public abstract Node<L> createNode(String key, @Nullable Object value, NodeTypeToken<?> type);
    // Key is always definitive.
    public abstract void deleteNode(String key);
    public abstract void set(String key, Node<L> node);
    public abstract Collection<Node<L>> getNodes();

    public void clear() {
        getNodes().clear();
    }

    public void setLogAdapter(@NotNull LogAdapter logAdapter) {
        Objects.requireNonNull(logAdapter);
        this.logAdapter = logAdapter;
    }

    /**
     * This method allows users to define their own adapters,
     * which are used to serialize and deserialize objects.
     *
     * @see NodeSerializer
     * @see NodeDeserializer
     *
     * @param type The (super)class of the object to be serialized/deserialized.
     * @param adapter The adapter to be used.
     * @param <T> The type of the serialized object.
     */
    public <T> void registerAdapter(Class<T> type, NodeAdapter<T, L> adapter) {
        if(type.equals(Object.class)) {
            // This would cause adapter usage for every type, which is unacceptable
            // for internal functionality.
            throw new IllegalArgumentException("Cannot register adapter for Object.class!");
        }
        adapters.put(type, adapter);
    }

    /**
     * This method unregisters an already registered adapter.
     *
     * @param type The (super)class of the serialized/deserialized object.
     */
    public void unregisterAdapter(Class<?> type) {
        adapters.remove(type);
    }

    /**
     * Updates this node's values from the provided mapped
     * object.
     * This method does not create new nodes, only updates
     * them!
     *
     * @param from The mapped object to update from.
     */
    @SuppressWarnings("rawtypes, unchecked")
    public void set(Object from) {
        if(isPrimitive(from.getClass())) {
            // Primitive values can't be passed to sections!
            throw new RuntimeException("Cannot set primitive value to section!");
        }

        clear();

        // SectionNode#getContext is not used here because I want
        // empty context with Object types.
        NodeContext<Object, L> context = new NodeContext<>(this);
        NodeSerializer nodeSerializer = obtainAdapter(from, NodeSerializer.class);
        // Serializer is never null since there is a default one.
        assert nodeSerializer != null;
        nodeSerializer.serialize(context, from);

        Map<String, Object> content = context.getObjects();
        content.forEach(this::set);
    }

    public void set(String key, Object value) {
        if(isContextDebug() && isHighestLevel()) {
            doLog("Highest level inspection:");
            doLog("Before update:");
            doLog("------------------------");
            printStructure();
        }
        doSet(key, value);
        if(isContextDebug() && isHighestLevel()) {
            doLog("------------------------");
            printStructure();
        }
    }

    private void doSet(String key, Object value) {
        Objects.requireNonNull(key, "Path must not be null!");

        String[] split = key.split("\\.");
        if(value == null) {
            if(split.length > 1) {
                ofNullable(get(split[1]))
                        .ifPresent(node -> {
                            if(node instanceof SectionNode) {
                                ((SectionNode<L>) node).set(String.join(".", (String[]) ArrayUtils.subarray(split, 1, split.length)), null);
                            }
                        });
                return;
            }
            deleteNode(key);
            return;
        } else if(key.contains(".")) {
            // This is not our target.
            String sub = split[0];
            Node<L> node = get(sub);
            if(node instanceof SimpleNode) {
                set(sub, null);
            }
            if(node == null) {
                node = createNode(sub, null, NodeTypes.SECTION);
                set(sub, node);
            }
            ((SectionNode<L>) node).set(
                    String.join(".", (String[]) ArrayUtils.subarray(split, 1, split.length)), value);
            return;
        }

        Node<L> lNode = get(key);
        if(lNode == null) {
            lNode = createNode(key, value, NodeTypes.recognize(value));
            set(key, (Node) lNode);
        }
        lNode.set(value);
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
            Constructor<T> declaredConstructor = typeClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            return map(declaredConstructor.newInstance(), placeholders);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T map(T obj) {
        return map(obj, new Placeholders());
    }

    public <T> T map(T obj, Placeholders placeholders) {
        return map(obj, placeholders, true);
    }

    /**
     * Tries top map this section to provided object. This method assigns values to the fields
     * according to the node types and rules specified by child classes.
     *
     * @param obj The object to map to.
     * @return The mapped object.
     * @param <T> The type of the object to map to.
     */
    @SuppressWarnings("rawtypes, unchecked")
    public <T> T map(T obj, Placeholders placeholders, boolean useCustomAdapters) {
        Class<?> typeClass = obj.getClass();
        if(Primitives.isWrapperType(Primitives.wrap(typeClass))) {
            debug(String.format("Cannot map section to primitive type for %s", typeClass.getName()));
            return null;
        }

        NodeDeserializer nodeDeserializer = useCustomAdapters ? obtainAdapter(obj, NodeDeserializer.class) : null;
        if(nodeDeserializer != null) {
            NodeContext<Node<L>, L> context = getContext();
            Object deserialized = nodeDeserializer.deserialize(obj, context, placeholders);
            if(!obj.getClass().isAssignableFrom(deserialized.getClass())) {
                throw new RuntimeException(String.format("Deserialized object is not assignable to the provided type! (%s -> %s)",
                        obj.getClass().getName(),
                        deserialized.getClass().getName()));
            }
            debug(String.format("Deserialized object %s using adapter %s", deserialized, nodeDeserializer.getClass().getName()));
            return (T) deserialized;
        }

        Map<String, Node<L>> nodeCandidates = getNodes()
                .stream()
                .collect(Collectors.toMap(Node::getName, Function.identity()));
        for(Field field : typeClass.getDeclaredFields()) {
            if(Modifier.isTransient(field.getModifiers())) {
                // Transient fields are skipped.
                debug(String.format("Skipping transient field %s", field.getName()));
                continue;
            }
            field.setAccessible(true);
            Object value = Defaults.defaultValue(field.getType());
            if(String.class.equals(field.getType()) && field.isAnnotationPresent(ThisNodeId.class)) {
                value = getName();
                debug(String.format("Field %s is mapped to this node id %s", field.getName(), value));
            } else if(nodeCandidates.containsKey(field.getName())) {
                debug("Found node for field " + field.getName());
                Object builtValue = buildValue(field, nodeCandidates.get(field.getName()), placeholders);
                if(builtValue != null) {
                    value = builtValue;
                }
            }
            try {
                if(field.get(obj) == null) {
                    field.set(obj, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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
    @ApiStatus.OverrideOnly
    public Object buildValue(Field field, Node<L> node, Placeholders placeholders) {
        Object value = null;

        debug("Building value for field " + field.getName());

        if(node instanceof SimpleNode && isPrimitive(field.getType())) {
            value = ((SimpleNode<L>) node).get();
            debug(String.format("Field %s is mapped to simple node %s", field.getName(), value));
        } else if(node instanceof SectionNode) {
            Class<?> contentType;
            if(List.class.isAssignableFrom(field.getType())
            && !isPrimitive(contentType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])) {
                List list = new ArrayList();
                ((SectionNode<Object>) node).getNodes(NodeTypes.SECTION)
                        .forEach(sn -> {
                            list.add(sn.map(contentType));
                        });
                debug(String.format("Field %s is mapped to list of sections %s", field.getName(), list));
                return list;
            } else {
                value = ((SectionNode<L>) node).map(field.getType());
                debug(String.format("Field %s is mapped to section node %s", field.getName(), value));
            }
        }
        return value;
    }

    @Nullable
    public Node<L> get(String path) {
        Map<String, Node<L>> children = new HashMap<>();

        for (Node<L> node : getNodes()) {
            children.put(node.getName(), node);
        }

        Node<L> current = this;
        for(String key : path.split("\\.")) {
            if(!(current instanceof SectionNode)) {
                // Path points nowhere.
                return null;
            }
            current = current == this
                    ? children.get(key)
                    : ((SectionNode<L>) current).get(key);
        }
        return current;
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

    public boolean has(String path) {
        return get(path) != null;
    }

    public NodeContext<Node<L>, L> getContext() {
        NodeContext<Node<L>, L> context = new NodeContext<>(this);
        getNodes().forEach(node -> context.set(node.getName(), node));
        return context;
    }

    public void printStructure() {
        if(parent != null) {
            parent.printStructure();
            return;
        }
        printStructureInternal(0);
    }

    private void printStructureInternal(int level) {
        String prefix = Strings.repeat("-", level);
        if(parent != null) {
            doLog(String.format("%s %s:", prefix, getName()));
        }
        for (Node<L> node : getNodes()) {
            if(node instanceof SectionNode) {
                ((SectionNode<L>) node).printStructureInternal(level + 1);
            } else if(node instanceof SimpleNode) {
                doLog(String.format("%s %s: %s", prefix + "-", node.getName(), ((SimpleNode<L>) node).get()));
            }
        }
    }

    private void doLog(String text) {
        LogAdapter logAdapter = getContextLogAdapter();
        logAdapter.log(Level.INFO, text);
    }

    // This includes also String, so it is not really primitive,
    // but I named it like that, so I don't want to change it. :D
    private boolean isPrimitive(Class<?> clazz) {
        return Primitives.isWrapperType(Primitives.wrap(clazz)) || String.class.equals(clazz);
    }

    @SuppressWarnings("rawtypes, unchecked")
    private <T extends NodeAdapter> T obtainAdapter(Object toBeSerialized, Class<T> adapterTypeClass) {
        Validator.requireAnyType(adapterTypeClass, NodeSerializer.class, NodeDeserializer.class);

        // I allow users to define their own serializers.
        // @see NodeSerializer
        for (Class<?> aClass : adapters.keySet()) {
            NodeAdapter<?, L> nodeAdapter = adapters.get(aClass);
            if(adapterTypeClass.isAssignableFrom(nodeAdapter.getClass()) && aClass.isAssignableFrom(toBeSerialized.getClass())) {
                return (T) nodeAdapter;
            }
        }

        if(parent != null) {
            // I check if there is any adapter already set in the context.
            T parentAdapter = parent.obtainAdapter(toBeSerialized, adapterTypeClass);
            if(parentAdapter != null) {
                return parentAdapter;
            }
        }
        return adapterTypeClass.equals(NodeSerializer.class)
                ? (T) new DefaultNodeSerializer()
                : null;
    }

    public boolean isContextDebug() {
        return makeContextCheck(SectionNode::isDebug);
    }

    public boolean isHighestLevel() {
        return parent == null;
    }

    private void debug(String message) {
        if (isContextDebug()) {
            getContextLogAdapter().log(Level.INFO, message);
        }
    }

    private LogAdapter getContextLogAdapter() {
        LogAdapter adapter = logAdapter;
        if(adapter == LogAdapter.DEFAULT && parent != null) {
            LogAdapter contextLogAdapter = parent.getContextLogAdapter();
            if(contextLogAdapter != LogAdapter.DEFAULT) {
                adapter = contextLogAdapter;
            }
        }
        return adapter;
    }

    private boolean makeContextCheck(Predicate<SectionNode<L>> test) {
        if(test.test(this))
            return true;
        return parent != null && parent.makeContextCheck(test);
    }

    public static class DefaultNodeSerializer<L> implements NodeSerializer<Object, L> {

        @Override
        public void serialize(NodeContext context, Object from) {
            for(Field field : from.getClass().getDeclaredFields()) {
                if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || field.isAnnotationPresent(ThisNodeId.class)) {
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
                    context.set(name, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @RequiredArgsConstructor
    public static class DefaultCollectionSerializer<L> implements NodeSerializer<Collection, L> {

        private final SectionNode<L> holder;

        @Override
        public void serialize(NodeContext<Object, L> context, Collection object) {
            for (Object obj : object) {
                String nodeId = ThisNodeId.Parser.parse(obj);
                if(nodeId == null) {
                    holder.debug("Node ID is null for object " + obj + " in collection of type " + object.getClass());
                    continue;
                }
                context.set(nodeId, obj);
            }
        }
    }

}
