package me.zort.configurationlib.configuration.types;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents a node context. Each given key represents name of
 * a node in configuration section.
 *
 * @author ZorTik
 */
@RequiredArgsConstructor
public class NodeContext<T> {

    @Getter
    private final Map<String, T> objects = Maps.newConcurrentMap();

    public void set(String key, T object) {
        objects.put(key, object);
    }

    @Nullable
    public T get(String key) {
        return objects.get(key);
    }

}
