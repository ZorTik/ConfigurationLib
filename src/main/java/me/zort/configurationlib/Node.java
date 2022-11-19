package me.zort.configurationlib;

/**
 * Represents a node in configuration. This can be either
 * a section or leaf node.
 * We're using dots as path separators.
 *
 * @param <L> Location type. This can be for example section.
 * @author ZorTik
 */
public interface Node<L> {

    /**
     * Puts this node to configuration location. Simply fills
     * provided location with data stored in this node.
     * Provided location is real node of the engine used.
     *
     * @param location Location origin node.
     */
    void putSelf(L location);
    void set(Object from);
    String getPath();

    default String getName() {
        return getPath().substring(getPath().lastIndexOf('.') + 1);
    }

}
