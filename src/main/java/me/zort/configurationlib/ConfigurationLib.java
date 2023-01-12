package me.zort.configurationlib;

import me.zort.configurationlib.configuration.bukkit.BukkitFileConfigurationNode;

import java.io.File;

public final class ConfigurationLib {

    private ConfigurationLib() {
    }

    public static BukkitFileConfigurationNode bukkit(File file) {
        return ConfigurationLibBukkit.of(ConfigurationLibBukkit.prepareDataFile(file));
    }

}
