package me.zort.configurationlib.configuration;

import me.zort.configurationlib.util.NodeTypeToken;

public final class NodeTypes {

    public static final NodeTypeToken<SectionNode> SECTION = new NodeTypeToken<>(SectionNode.class);
    public static final NodeTypeToken<SimpleNode> SIMPLE = new NodeTypeToken<>(SimpleNode.class);

}
