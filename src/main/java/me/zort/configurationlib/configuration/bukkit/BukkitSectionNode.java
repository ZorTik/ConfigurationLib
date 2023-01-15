package me.zort.configurationlib.configuration.bukkit;

import lombok.Getter;
import me.zort.configurationlib.*;
import me.zort.configurationlib.configuration.bukkit.adapter.DefaultItemAdapter;
import me.zort.configurationlib.util.*;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitSectionNode extends SectionNode<ConfigurationSection> {

    @Getter
    private final ConfigurationSection section;
    private final Map<String, Node<ConfigurationSection>> children;

    public BukkitSectionNode(@Nullable SectionNode<ConfigurationSection> parent, ConfigurationSection section) {
        super(parent);
        this.section = section;
        this.children = new ConcurrentHashMap<>();
        init();

        if(parent == null) {
            registerAdapter(ItemStack.class, new DefaultItemAdapter());
        }
    }

    @Override
    public void clear() {
        children.clear();
    }

    @Override
    public Node<ConfigurationSection> createNode(String key, @Nullable Object value, NodeTypeToken<?> type) {
        Node<ConfigurationSection> node;
        if(type.equals(NodeTypes.SIMPLE)) {
            node = new BukkitSimpleNode(section, key, value);
        } else if(type.equals(NodeTypes.SECTION)) {
            node = new BukkitSectionNode(this, section.createSection(key));
        } else throw new IllegalArgumentException("Unknown node type: " + type.getTypeClass());

        return node;
    }

    @Override
    public void deleteNode(String key) {
        section.set(key, null);
        children.remove(key);
    }

    public boolean save() {
        if(getParent() == null || !(getParent() instanceof BukkitSectionNode)) {
            return false;
        }
        return ((BukkitSectionNode) getParent()).save();
    }

    @Override
    public void set(String key, Object value) {
        if(value.getClass().equals(String[].class)) {
            // String arrays are passed as simple nodes.
            set(key, createNode(key, value, NodeTypes.SIMPLE));
            return;
        }
        super.set(key, value);
    }

    @Override
    public void set(String key, Node<ConfigurationSection> node) {
        children.put(key, node);
    }

    @Override
    public void putSelf(ConfigurationSection location) {
        for(Node<ConfigurationSection> node : children.values()) {
            ConfigurationSection locationToPutIn = location;
            if(node instanceof SectionNode && !location.contains(node.getName())) {
                locationToPutIn = location.createSection(node.getName());
            } else if(node instanceof SectionNode && location.contains(node.getName())) {
                locationToPutIn = location.getConfigurationSection(node.getName());
            }
            node.putSelf(locationToPutIn);
        }
    }

    /**
     * @see SectionNode#buildValue(Field, Node, Placeholders)
     */
    @Override
    public Object buildValue(Field field, Node<ConfigurationSection> node, Placeholders placeholders) {
        // I'm specifying new field types for mapped objects.
        if(field.getType().equals(List.class) && node instanceof BukkitSimpleNode) {
            Object listCandidate = ((BukkitSimpleNode) node).get();
            if(listCandidate instanceof List) {
                return listCandidate;
            }
        } else if(field.getType().equals(String[].class) && node instanceof BukkitSimpleNode) {
            Object arrayCandidate = ((BukkitSimpleNode) node).get();
            if(arrayCandidate instanceof String[]) {
                return arrayCandidate;
            }
        }
        // TODO: Add support for other types.
        return super.buildValue(field, node, placeholders);
    }

    @Override
    public BukkitSimpleNode getSimple(String path) {
        // Just to simplify the usage.
        if(!has(path)) return new BukkitSimpleNode(section, path, null);
        return (BukkitSimpleNode) super.getSimple(path);
    }

    @Override
    public BukkitSectionNode getSection(String path) {
        return (BukkitSectionNode) super.getSection(path);
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

    @Nullable
    public ItemStack getAsItem() {
        return getAsItem(new Placeholders());
    }

    @Nullable
    public ItemStack getAsItem(Placeholders placeholders) {
        if(!ItemValidator.validate(section)) {
            return null;
        }
        ItemStack item;
        ItemMeta meta;
        int amount = section.getInt("amount", 1);
        String typeString = section.getString("type");
        if(typeString.startsWith("head-")) {
            typeString = typeString.replaceFirst("head-", "");
            // Bigger than 1.12
            item = verMajor() > 112
                    ? new ItemStack(Material.matchMaterial("PLAYER_HEAD"), amount)
                    : new ItemStack(Material.matchMaterial("SKULL_ITEM"), amount);
            meta = item.getItemMeta();
            assignTextures(meta, typeString);
        } else {
            Material type = Material.matchMaterial(typeString);
            if(type == null) {
                return null;
            }
            short data = (short) section.getInt("data");
            item = new ItemStack(type, amount, data);
            meta = item.getItemMeta();
        }
        if(meta != null) {
            if(section.contains("name")) meta.setDisplayName(
                    placeholders.replace(Colorizer.colorize(section.getString("name")))
            );
            if(section.contains("lore")) meta.setLore(
                    placeholders.replace(Colorizer.colorize(section.getStringList("lore")))
            );
            if(section.contains("enchanted") && section.getBoolean("enchanted")) {
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                meta.addItemFlags(ItemFlag.values());
            }
            if(section.contains("enchantments")) {
                for(String key : section.getConfigurationSection("enchantments").getKeys(false)) {
                    //Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase()));
                    Enchantment enchantment = Enchantment.getByName(key.toUpperCase());
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

    private void assignTextures(ItemMeta unknownMeta, String value) {
        SkullMeta meta = (SkullMeta) unknownMeta;
        try {
            Object profile = Class.forName("com.mojang.authlib.GameProfile")
                    .getDeclaredConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), "");

            Object propertyMap = profile.getClass().getDeclaredMethod("getProperties").invoke(profile);
            Object property = Class.forName("com.mojang.authlib.properties.Property")
                    .getDeclaredConstructor(String.class, String.class).newInstance("textures", value);
            propertyMap.getClass().getMethod("put", Object.class, Object.class).invoke(propertyMap, "textures", property);

            Field metaProfileField = meta.getClass().getDeclaredField("profile");
            metaProfileField.setAccessible(true);
            metaProfileField.set(meta, profile);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPath() {
        return section.getCurrentPath();
    }

    @Override
    public Collection<Node<ConfigurationSection>> getNodes() {
        return children.values();
    }

    public void setDebug(boolean debug, boolean reload) {
        super.setDebug(debug);
        if(reload) init();
    }

    private void init() {
        children.clear();
        for(String key : section.getKeys(false)) {
            children.put(key, section.isConfigurationSection(key)
                    ? new BukkitSectionNode(this, section.getConfigurationSection(key))
                    : new BukkitSimpleNode(section, key, section.get(key)));
        }
    }

    private static int verMajor() {
        String verString = String.join("", (String[]) ArrayUtils.subarray(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\."), 0, 2));
        return Integer.parseInt(verString);
    }

}
