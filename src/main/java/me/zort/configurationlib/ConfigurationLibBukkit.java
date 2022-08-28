package me.zort.configurationlib;

import me.zort.configurationlib.configuration.bukkit.BukkitFileConfigurationNode;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public final class ConfigurationLibBukkit {

    public static BukkitFileConfigurationNode of(File file) {
        return of(file, true);
    }

    public static BukkitFileConfigurationNode of(File file, boolean create) {
        if((create && prepareDataFile(file) == null) || (!create && !file.exists())) return null;
        return new BukkitFileConfigurationNode(file);
    }

    public static File prepareDataFile(Plugin plugin, String name) {
        File file = new File(plugin.getDataFolder(), name);
        if(!file.exists() && plugin.getResource(name) != null) {
            plugin.saveResource(name, false);
        }
        return prepareDataFile(file);
    }

    public static File prepareDataFile(File file) {
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

}