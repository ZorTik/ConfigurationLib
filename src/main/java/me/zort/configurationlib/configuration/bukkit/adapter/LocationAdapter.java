package me.zort.configurationlib.configuration.bukkit.adapter;

import me.zort.configurationlib.*;
import me.zort.configurationlib.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LocationAdapter implements NodeSerializer<Location, ConfigurationSection>, NodeDeserializer<Location, ConfigurationSection> {

    @Override
    public void serialize(NodeContext<Object, ConfigurationSection> context, Location object) {
        context.set("world", object.getWorld().getName());
        context.set("x", object.getX());
        context.set("y", object.getY());
        context.set("z", object.getZ());
        context.set("yaw", (double) object.getYaw());
        context.set("pitch", (double) object.getPitch());
    }

    @Override
    public Location deserialize(@NotNull Location deserializeInto, NodeContext<Node<ConfigurationSection>, ConfigurationSection> context, Placeholders placeholders) {
        String world = Objects.requireNonNull(((SimpleNode<?>) context.get("world"))).getAsString();
        double x = Objects.requireNonNull(((SimpleNode<?>) context.get("x"))).getAsDouble();
        double y = Objects.requireNonNull(((SimpleNode<?>) context.get("y"))).getAsDouble();
        double z = Objects.requireNonNull(((SimpleNode<?>) context.get("z"))).getAsDouble();
        double yaw = Objects.requireNonNull(((SimpleNode<?>) context.get("yaw"))).getAsDouble();
        double pitch = Objects.requireNonNull(((SimpleNode<?>) context.get("pitch"))).getAsDouble();

        return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
    }
}
