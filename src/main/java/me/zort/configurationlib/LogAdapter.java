package me.zort.configurationlib;

import java.util.logging.Level;

public interface LogAdapter {

    LogAdapter DEFAULT = (level, message) -> System.out.println("[" + level.getName() + "] " + message);

    void log(Level level, String message);

}
