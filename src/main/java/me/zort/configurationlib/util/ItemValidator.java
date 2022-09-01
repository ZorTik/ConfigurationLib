package me.zort.configurationlib.util;

import org.bukkit.configuration.ConfigurationSection;

public class ItemValidator {

    public static boolean validate(ConfigurationSection section) {
        return section.contains("type");
    }

}
