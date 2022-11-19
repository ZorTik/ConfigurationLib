package me.zort.configurationlib.configuration;

import com.google.common.primitives.Primitives;
import me.zort.configurationlib.util.NodeTypeToken;

public final class NodeTypes {

    public static final NodeTypeToken<SectionNode> SECTION = new NodeTypeToken<>(SectionNode.class, Type.SECTION);
    public static final NodeTypeToken<SimpleNode> SIMPLE = new NodeTypeToken<>(SimpleNode.class, Type.SIMPLE);

    public static NodeTypeToken<?> recognize(Object object) {
        return Primitives.isWrapperType(Primitives.wrap(object.getClass())) ? SIMPLE : SECTION;
    }

    public enum Type {
        SECTION,
        SIMPLE
    }

}
