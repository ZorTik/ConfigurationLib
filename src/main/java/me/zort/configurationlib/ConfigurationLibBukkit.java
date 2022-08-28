package me.zort.configurationlib;

import me.zort.configurationlib.configuration.bukkit.BukkitFileConfigurationNode;

import java.io.File;
import java.io.IOException;

public final class ConfigurationLibBukkit {

    public static BukkitFileConfigurationNode of(File file) {
        return of(file, true);
    }

    public static BukkitFileConfigurationNode of(File file, boolean create) {
        if(create && !file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return new BukkitFileConfigurationNode(file);
    }

}