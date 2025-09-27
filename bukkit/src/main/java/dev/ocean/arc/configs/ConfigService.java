package dev.ocean.arc.configs;

import lombok.Getter;


@Getter
public class ConfigService {
    private static ConfigService instance;
    private ConfigFile mainConfig;

    public static ConfigService get() {
        if (instance == null) instance = new ConfigService();

        return instance;
    }

    public void load() {
        mainConfig = new ConfigFile("settings");

        initialize();
    }

    public void initialize() {
    }
}
