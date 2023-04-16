package me.zort.configurationlib.configuration.bukkit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BukkitFileConfigurationNode extends BukkitSectionNode {

    private final File file;

    public BukkitFileConfigurationNode(File file) {
        super(null, YamlConfiguration.loadConfiguration(file));
        this.file = file;
    }

    public BukkitFileConfigurationNode(InputStream in) {
        super(null, YamlConfiguration.loadConfiguration(new InputStreamReader(in)));
        this.file = null;
    }

    public boolean save() {
        putSelf(getSection());

        if (file == null) return false;

        try {
            ((FileConfiguration) getSection()).save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
