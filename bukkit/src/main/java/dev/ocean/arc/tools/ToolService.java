package dev.ocean.arc.tools;

import dev.ocean.arc.ArcPlugin;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
public class ToolService {

    private static ToolService instance;

    private final Map<String, Tool> tools = new HashMap<>();
    private final JavaPlugin plugin;

    private ToolService() {
        this.plugin = ArcPlugin.getInstance();
        loadTools();
    }

    public static ToolService get() {
        if (instance == null) {
            instance = new ToolService();
        }
        return instance;
    }

    private void loadTools() {
        Reflections reflections = new Reflections("dev.ocean.arc.tools.impl");

        Set<Class<? extends Tool>> toolClasses = reflections.getSubTypesOf(Tool.class);

        for (Class<? extends Tool> clazz : toolClasses) {
            if (Modifier.isAbstract(clazz.getModifiers())) continue;
            try {
                Tool tool = clazz.getDeclaredConstructor().newInstance();
                tools.put(tool.getName(), tool);
                plugin.getLogger().info("Loaded tool: " + tool.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load tool: " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

    public Optional<Tool> getToolByName(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Optional<Tool> getToolFromItem(ItemStack item) {
        return tools.values().stream()
                .filter(tool -> tool.matches(item))
                .findFirst();
    }
}
