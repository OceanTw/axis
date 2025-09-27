package dev.ocean.arc.tools;

import org.bukkit.Material;

import java.util.*;

public class ToolSettings {

    private final Map<String, Object> settings = new HashMap<>();

    public ToolSettings() {}

    public ToolSettings(Map<String, Object> settings) {
        this.settings.putAll(settings);
    }

    public <T> T get(String key, T defaultValue) {
        return (T) settings.getOrDefault(key, defaultValue);
    }

    public void set(String key, Object value) {
        settings.put(key, value);
    }

    public boolean has(String key) {
        return settings.containsKey(key);
    }

    public void remove(String key) {
        settings.remove(key);
    }

    public Set<String> getKeys() {
        return settings.keySet();
    }

    public Map<String, Object> getAll() {
        return new HashMap<>(settings);
    }

    public List<Material> getMaterials(String key) {
        return get(key, new ArrayList<Material>());
    }

    public void setMaterials(String key, List<Material> materials) {
        set(key, materials);
    }

    public Map<Material, Double> getMaterialPercentages(String key) {
        return get(key, new HashMap<Material, Double>());
    }

    public void setMaterialPercentages(String key, Map<Material, Double> percentages) {
        set(key, percentages);
    }

    public ToolSettings copy() {
        return new ToolSettings(this.settings);
    }
}