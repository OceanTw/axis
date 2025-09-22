package dev.ocean.axis.tools;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    public abstract boolean onLeftClick(@NonNull Player player, @NonNull Location location, @NonNull ToolSettings settings);
    public abstract boolean onRightClick(@NonNull Player player, @NonNull Location location, @NonNull ToolSettings settings);
    public abstract boolean canUse(@NonNull Player player);
    public abstract ToolSettings createDefaultSettings();

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set display name using Components API
            meta.displayName(Component.text(displayName).color(NamedTextColor.AQUA));

            // Set lore using Components API
            meta.lore(createItemLore());

            item.setItemMeta(meta);
        }

        return item;
    }

    protected List<Component> createItemLore() {
        return List.of(
                Component.text(description).color(NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Left Click: ").color(NamedTextColor.YELLOW)
                        .append(Component.text(getLeftClickDescription()).color(NamedTextColor.WHITE)),
                Component.text("Right Click: ").color(NamedTextColor.YELLOW)
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