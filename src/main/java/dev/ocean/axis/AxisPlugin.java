package dev.ocean.axis;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class AxisPlugin extends JavaPlugin {

    @Getter
    private static AxisPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {

    }
}
