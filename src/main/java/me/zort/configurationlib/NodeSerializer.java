package me.zort.configurationlib;

/**
 * Node serializer class.
 *
 * @param <T> The type of the object to serialize.
 * @author ZorTik
 */
public interface NodeSerializer<T, L> extends NodeAdapter<T, L> {

    /**
     * Serializes the given object to a section node context.
     *
     * @param context The context to serialize to.
     * @param object The object to serialize.
     */
    void serialize(NodeContext<Object, L> context, T object);

}
