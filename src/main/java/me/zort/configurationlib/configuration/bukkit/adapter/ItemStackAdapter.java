package me.zort.configurationlib.configuration.bukkit.adapter;

import me.zort.configurationlib.NodeContext;
import me.zort.configurationlib.NodeSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemStackAdapter implements NodeSerializer<ItemStack, ConfigurationSection> {

    @Override
    public void serialize(NodeContext<Object, ConfigurationSection> context, ItemStack item) {
        // TODO
    }

}
