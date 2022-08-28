package me.zort.configurationlib.configuration.bukkit;

import lombok.Getter;
import me.zort.configurationlib.configuration.Node;
import me.zort.configurationlib.configuration.SectionNode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitSectionNode extends SectionNode<ConfigurationSection> {

    @Getter
    private final ConfigurationSection section;
    private final Map<String, Node<ConfigurationSection>> children;

    public BukkitSectionNode(ConfigurationSection section) {
        this.section = section;
        this.children = new ConcurrentHashMap<>();
        init();
    }

    @Override
    public void putSelf(ConfigurationSection location) {
        for(Node<ConfigurationSection> node : children.values()) {
            node.putSelf(location);
        }
    }

    @Nullable
    @Override
    public Node<ConfigurationSection> get(String path) {
        Node<ConfigurationSection> current = this;
        for(String key : path.split("\\.")) {
            if(!(current instanceof SectionNode)) {
                // Path points nowhere.
                return null;
            }
            current = current == this
                    ? children.get(key)
                    : ((SectionNode<ConfigurationSection>) current).get(key);
        }
        return current;
    }

    @Override
    public String getPath() {
        return section.getCurrentPath();
    }

    @Override
    public Collection<Node<ConfigurationSection>> getNodes() {
        return children.values();
    }

    private void init() {
        children.clear();
        for(String key : section.getKeys(false)) {
            children.put(key, section.isConfigurationSection(key)
                    ? new BukkitSectionNode(section.getConfigurationSection(key))
                    : new BukkitSimpleNode(section, key, section.get(key)));
        }
    }

}
