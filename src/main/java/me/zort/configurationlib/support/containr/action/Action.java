package me.zort.configurationlib.support.containr.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Action {
    void run(Player player, String value);

    class Registry {
        private static final Map<String, Action> ACTIONS = new ConcurrentHashMap<>();

        static {
            ACTIONS.put("player", (info, value) -> {
                info.getPlayer().performCommand(value);
            });
            ACTIONS.put("console", (info, value) -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), value);
            });
            ACTIONS.put("message", (info, value) -> {
                info.getPlayer().sendMessage(value);
            });
            ACTIONS.put("close", (info, value) -> {
                info.getPlayer().closeInventory();
            });
            ACTIONS.put("broadcast", (info, value) -> {
                Bukkit.broadcastMessage(value);
            });
        }

        public static Action get(String id) {
            return ACTIONS.get(id);
        }
    }
}
