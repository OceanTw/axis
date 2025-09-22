package dev.ocean.axis.tools;

import dev.ocean.axis.AxisPlugin;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@Getter
public abstract class Tool {

    private final String name;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final ToolSettings defaultSettings;

    public Tool(@NonNull String name, @NonNull String displayName, @NonNull String description, @NonNull Material icon) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.defaultSettings = createDefaultSettings();
    }

    public abstract boolean onLeftClick(@NonNull Player player, @NonNull Location location, ToolSettings settings);
    public abstract boolean onRightClick(@NonNull Player player, @NonNull Location location, ToolSettings settings);
    public abstract boolean canUse(@NonNull Player player);
    public abstract ToolSettings createDefaultSettings();

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(displayName).color(NamedTextColor.AQUA));
            meta.lore(createItemLore());

            // Mark this item as belonging to this Tool
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(getKey(), PersistentDataType.STRING, name);

            item.setItemMeta(meta);
        }

        return item;
    }

    public boolean matches(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        String stored = meta.getPersistentDataContainer().get(getKey(), PersistentDataType.STRING);
        return stored != null && stored.equals(name);
    }

    private NamespacedKey getKey() {
        return new NamespacedKey(AxisPlugin.getInstance(), "tool-id");
    }
    protected List<Component> createItemLore() {
        return List.of(
                Component.text(description).color(NamedTextColor.GRAY),
                Component.empty(),
                Component.text("LEFT CLICK: ").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                        .append(Component.text(getLeftClickDescription()).color(NamedTextColor.WHITE)),
                Component.empty(),
                Component.text("RIGHT CLICK: ").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                        .append(Component.text(getRightClickDescription()).color(NamedTextColor.WHITE))
        );
    }

    protected String getLeftClickDescription() {
        return "Primary action";
    }

    protected String getRightClickDescription() {
        return "Secondary action";
    }

    public Component getDisplayComponent() {
        return Component.text(displayName).color(NamedTextColor.AQUA);
    }

    public Component getDescriptionComponent() {
        return Component.text(description).color(NamedTextColor.GRAY);
    }
}