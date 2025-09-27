package dev.ocean.api.tools;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public interface ArcTool {

    String getName();
    String getDisplayName();
    String getDescription();
    Material getIcon();
    ArcToolSettings getDefaultSettings();

    boolean onLeftClick(Player player, Location location, ArcToolSettings settings);
    boolean onRightClick(Player player, Location location, ArcToolSettings settings);
    boolean canUse(Player player);

    ArcToolSettings createDefaultSettings();
    Set<String> getConfigurableSettings();

    ItemStack createItemStack();
    ItemStack getItem();
    boolean matches(ItemStack item);
    ArcToolSettings getItemSettings(ItemStack item);
    void saveItemSettings(ItemStack item, ArcToolSettings settings);

    Component getDisplayComponent();
    Component getDescriptionComponent();
}
