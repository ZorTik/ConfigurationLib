package me.zort.configurationlib.configuration.bukkit;

import me.zort.configurationlib.SimpleNode;
import org.bukkit.configuration.ConfigurationSection;

public class BukkitSimpleNode implements SimpleNode<ConfigurationSection> {

    private final ConfigurationSection parent;
    private final String key;
    private Object value;

    public BukkitSimpleNode(ConfigurationSection parent, String key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    @Override
    public void set(Object value) {
        this.value = value;
        // This method should not save now, configuration should be saved
        // using global save method.
        // putSelf(parent);
    }

    @Override
    public void putSelf(ConfigurationSection location) {
        location.set(key, value);
    }

    @Override
    public String getPath() {
        return parent.getCurrentPath() + "." + key;
    }

    @Override
    public Object get() {
        return value;
    }

}
