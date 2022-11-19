package me.zort.configurationlib.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.zort.configurationlib.Node;
import me.zort.configurationlib.NodeTypes;

@RequiredArgsConstructor
@Getter
public class NodeTypeToken<T extends Node> {

    private final Class<T> typeClass;
    private final NodeTypes.Type type;

}
