package me.zort.configurationlib.configuration.bukkit.adapter;

import me.zort.configurationlib.Node;
import me.zort.configurationlib.NodeContext;
import me.zort.configurationlib.NodeDeserializer;
import me.zort.configurationlib.configuration.bukkit.BukkitSectionNode;
import me.zort.configurationlib.util.Placeholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DefaultItemAdapter implements NodeDeserializer<ItemStack, ConfigurationSection> {

    @Override
    public ItemStack deserialize(@NotNull ItemStack deserializeInto, NodeContext<Node<ConfigurationSection>, ConfigurationSection> context, Placeholders placeholders) {
        return ((BukkitSectionNode) context.getNode()).getAsItem(placeholders);
    }

}
