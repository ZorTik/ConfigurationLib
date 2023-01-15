package me.zort.configurationlib;

import me.zort.configurationlib.configuration.bukkit.BukkitFileConfigurationNode;
import me.zort.configurationlib.configuration.virtual.VirtualSectionNode;
import org.bukkit.plugin.Plugin;

import java.io.File;

public final class ConfigurationLib {

    private ConfigurationLib() {
    }

    public static BukkitFileConfigurationNode bukkit(File file) {
        return ConfigurationLibBukkit.of(ConfigurationLibBukkit.prepareDataFile(file));
    }

    public static BukkitFileConfigurationNode bukkit(Plugin plugin, String name) {
        return ConfigurationLibBukkit.of(ConfigurationLibBukkit.prepareDataFile(plugin, name));
    }

    public static VirtualSectionNode virtual() {
        return new VirtualSectionNode();
    }

}
