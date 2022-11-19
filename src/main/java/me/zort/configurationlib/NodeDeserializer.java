package me.zort.configurationlib;

import me.zort.configurationlib.util.Placeholders;

public interface NodeDeserializer<T, L> extends NodeAdapter<T, L> {

    void deserialize(T deserializeInto, NodeContext<Node<L>, L> context, Placeholders placeholders);

}
