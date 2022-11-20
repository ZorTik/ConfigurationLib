package me.zort.configurationlib;

import me.zort.configurationlib.util.Placeholders;
import org.jetbrains.annotations.NotNull;

/**
 * Node deserializer class.
 *
 * @param <T> The type of the object to deserialize.
 * @author ZorTik
 */
public interface NodeDeserializer<T, L> extends NodeAdapter<T, L> {

    /**
     * Deserializes the given context to deserializeInto object.
     * Return value of this function should return deserializeInto
     * object, but it's optional to make a new one.
     *
     * @param deserializeInto The object to deserialize into.
     * @param context The context to deserialize from.
     * @param placeholders The placeholders to use.
     * @return The deserialized object.
     */
    T deserialize(@NotNull T deserializeInto, NodeContext<Node<L>, L> context, Placeholders placeholders);

}
