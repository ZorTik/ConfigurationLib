package me.zort.configurationlib;

import me.zort.configurationlib.configuration.bukkit.BukkitFileConfigurationNode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Random;

public class Test extends JavaPlugin {

    public void example() {
        File file = new File(getDataFolder(), "config.yml");

        ConfigurationLibBukkit.prepareDataFile(file);

        BukkitFileConfigurationNode config = ConfigurationLibBukkit.of(file);

        int id = config.getSimple("id").getAsInt();
        id += 5;

        config.set("id", id);
        config.getSimple("id").set(id);
        config.getSection("anySection").set("anyKey", "anyValue");


        AnyObject object = new AnyObject(1, "Hello World");
        config.set(object);

        AnyObject.AnySubObject subObject = config.getSection("one.subObject").map(AnyObject.AnySubObject.class);
        int anyField = subObject.getAnyField(); // 8

        ItemStack item = config.getSection("item").getAsItem();
    }

    public class AnyObject {
        private int id;
        private String name;
        private AnySubObject subObject = new AnySubObject(new Random().nextInt());

        public AnyObject(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public class AnySubObject {
            private int anyField;

            public AnySubObject(int anyField) {
                this.anyField = anyField;
            }

            public int getAnyField() {
                return anyField;
            }
        }
    }

}
