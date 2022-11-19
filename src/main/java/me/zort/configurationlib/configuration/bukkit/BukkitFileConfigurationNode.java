package me.zort.configurationlib.configuration.bukkit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class BukkitFileConfigurationNode extends BukkitSectionNode {

    private final File file;

    public BukkitFileConfigurationNode(File file) {
        super(null, YamlConfiguration.loadConfiguration(file));
        this.file = file;
    }

    public boolean save() {
        putSelf(getSection());
        try {
            ((FileConfiguration) getSection()).save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
