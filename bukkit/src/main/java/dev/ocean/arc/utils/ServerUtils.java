package dev.ocean.arc.utils;

import dev.ocean.arc.ArcPlugin;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerUtils {

    public void info(String message) {
        ArcPlugin.getInstance().getLogger().info(message);
    }

    public void error(String message) {
        ArcPlugin.getInstance().getLogger().severe(message);
    }
}