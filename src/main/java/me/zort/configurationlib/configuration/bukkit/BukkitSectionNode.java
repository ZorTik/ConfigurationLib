package me.zort.configurationlib.configuration.bukkit;

import lombok.Getter;
import me.zort.configurationlib.configuration.Node;
import me.zort.configurationlib.configuration.SectionNode;
import me.zort.configurationlib.util.Colorizer;
import me.zort.configurationlib.util.ItemValidator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    public boolean has(String path) {
        return get(path) != null;
    }

    @Nullable
    public ItemStack getAsItem() {
        if(!ItemValidator.validate(section)) {
            return null;
        }
        Material type = Material.matchMaterial(section.getString("type"));
        if(type == null) {
            return null;
        }
        short data = (short) section.getInt("data");
        int amount = section.getInt("amount", 1);
        ItemStack item = new ItemStack(type, amount, data);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            if(section.contains("name")) {
                meta.setDisplayName(Colorizer.colorize(section.getString("display-name")));
            }
            if(section.contains("lore")) {
                meta.setLore(Colorizer.colorize(section.getStringList("lore")));
            }
            if(section.contains("enchanted") && section.getBoolean("enchanted")) {
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                meta.addItemFlags(ItemFlag.values());
            }
            if(section.contains("enchantments")) {
                for(String key : section.getConfigurationSection("enchantments").getKeys(false)) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase()));
                    if(enchantment == null) {
                        continue;
                    }
                    meta.addEnchant(enchantment, section.getInt("enchantments." + key), true);
                }
            }
            if(section.contains("flags")) {
                for(String flag : section.getStringList("flags")) {
                    try {
                        ItemFlag itemFlag = ItemFlag.valueOf(flag.toUpperCase());
                        meta.addItemFlags(itemFlag);
                    } catch(Exception e) {
                        continue;
                    }
                }
            }
            item.setItemMeta(meta);
        }
        return item;
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
