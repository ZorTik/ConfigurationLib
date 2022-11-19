package me.zort.configurationlib.exception;

import lombok.Getter;
import me.zort.configurationlib.Node;

public class ConfigurationException extends RuntimeException {

    @Getter
    private final Node<?> node;

    public ConfigurationException(Node<?> node, String message) {
        super(message);
        this.node = node;
    }

}
