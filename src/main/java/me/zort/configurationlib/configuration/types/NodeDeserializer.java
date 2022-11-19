package me.zort.configurationlib.configuration.types;

import me.zort.configurationlib.configuration.Node;
import me.zort.configurationlib.util.Placeholders;

public interface NodeDeserializer<T, L> extends NodeAdapter<T, L> {

    void deserialize(T deserializeInto, NodeContext<Node<L>> context, Placeholders placeholders);

}
