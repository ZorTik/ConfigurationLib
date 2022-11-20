package me.zort.configurationlib.configuration.bukkit;

import lombok.Getter;
import me.zort.configurationlib.Node;
import me.zort.configurationlib.NodeTypes;
import me.zort.configurationlib.SectionNode;
import me.zort.configurationlib.configuration.bukkit.adapter.ItemStackAdapter;
import me.zort.configurationlib.util.Colorizer;
import me.zort.configurationlib.util.ItemValidator;
import me.zort.configurationlib.util.NodeTypeToken;
import me.zort.configurationlib.util.Placeholders;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

        //registerAdapter(ItemStack.class, new ItemStackAdapter());
    }

    @Override
    public void clear() {
        children.clear();
    }

    @Override
    public Node<ConfigurationSection> createNode(String key, Object value, NodeTypeToken<?> type) {
        Node<ConfigurationSection> node;
        if(type.equals(NodeTypes.SIMPLE)) {
            node = new BukkitSimpleNode(section, key, value);
        } else if(type.equals(NodeTypes.SECTION)) {
            node = new BukkitSectionNode(this, section.createSection(key));
        } else throw new IllegalArgumentException("Unknown node type: " + type.getTypeClass());

        return node;
    }

    @Override
    public void set(String key, Node<ConfigurationSection> node) {
        children.put(key, node);
    }

    @Override
    public void putSelf(ConfigurationSection location) {
        for(Node<ConfigurationSection> node : children.values()) {
            node.putSelf(location);
        }
    }

    /**
     * @see SectionNode#buildValue(Field, Node, Placeholders)
     */
    @Override
    public Object buildValue(Field field, Node<ConfigurationSection> node, Placeholders placeholders) {
        // I'm specifying new field types for mapped objects.
        if(field.getType().equals(ItemStack.class) && node instanceof BukkitSectionNode) {
            return ((BukkitSectionNode) node).getAsItem(placeholders);
        } else if(field.getType().equals(List.class) && node instanceof BukkitSimpleNode) {
            Object listCandidate = ((BukkitSimpleNode) node).get();
            if(listCandidate instanceof List) {
                return listCandidate;
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

    public boolean has(String path) {
        return get(path) != null;
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
