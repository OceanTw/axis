package dev.ocean.api.tools;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ArcToolSettings {

    <T> T get(String key, T defaultValue);
    void set(String key, Object value);
    boolean has(String key);
    void remove(String key);
    Set<String> getKeys();
    Map<String, Object> getAll();

    List<Material> getMaterials(String key);
    void setMaterials(String key, List<Material> materials);

    Map<Material, Double> getMaterialPercentages(String key);
    void setMaterialPercentages(String key, Map<Material, Double> percentages);

    ArcToolSettings copy();
}
