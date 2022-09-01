package me.zort.configurationlib.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Colorizer {

    public static String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> colorize(List<String> list) {
        List<String> result = new ArrayList<>();
        for(String s : list) {
            result.add(colorize(s));
        }
        return result;
    }

}
