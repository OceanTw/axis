package dev.ocean.arc.tools;

import dev.ocean.arc.Main;
import dev.ocean.arc.utils.ComponentUtils;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

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

    public abstract boolean onLeftClick(@NonNull Player player, Location location, ToolSettings settings);
    public abstract boolean onRightClick(@NonNull Player player, Location location, ToolSettings settings);
    public abstract boolean canUse(@NonNull Player player);
    public abstract ToolSettings createDefaultSettings();
    public abstract Set<String> getConfigurableSettings();

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(ComponentUtils.colored(displayName, NamedTextColor.AQUA));
            meta.lore(createItemLore());

            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(getToolIdKey(), PersistentDataType.STRING, name);

            saveSettingsToItem(meta, defaultSettings);
            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack getItem() {
        return new ItemStack(icon);
    }

    public boolean matches(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        String stored = meta.getPersistentDataContainer().get(getToolIdKey(), PersistentDataType.STRING);
        return stored != null && stored.equals(name);
    }

    public ToolSettings getItemSettings(ItemStack item) {
        if (!matches(item)) return createDefaultSettings();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return createDefaultSettings();
        return loadSettingsFromItem(meta);
    }

    public void saveItemSettings(ItemStack item, ToolSettings settings) {
        if (!matches(item)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        saveSettingsToItem(meta, settings);
        item.setItemMeta(meta);
    }

    private void saveSettingsToItem(ItemMeta meta, ToolSettings settings) {
        PersistentDataContainer data = meta.getPersistentDataContainer();

        for (String key : getConfigurableSettings()) {
            Object value = settings.get(key, null);
            if (value == null) continue;

            NamespacedKey settingKey = new NamespacedKey(Main.getInstance(), "setting_" + key);

            if (value instanceof Boolean) {
                data.set(settingKey, PersistentDataType.BYTE, (byte) (((Boolean) value) ? 1 : 0));
            } else if (value instanceof Integer) {
                data.set(settingKey, PersistentDataType.INTEGER, (Integer) value);
            } else if (value instanceof Double) {
                data.set(settingKey, PersistentDataType.DOUBLE, (Double) value);
            } else if (value instanceof String) {
                data.set(settingKey, PersistentDataType.STRING, (String) value);
            } else if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty() && list.get(0) instanceof Material) {
                    @SuppressWarnings("unchecked")
                    List<Material> materials = (List<Material>) list;
                    String serialized = String.join(",", materials.stream().map(Material::name).toList());
                    data.set(settingKey, PersistentDataType.STRING, serialized);
                }
            } else if (value instanceof Map<?, ?> map) {
                if (!map.isEmpty() && map.keySet().iterator().next() instanceof Material) {
                    @SuppressWarnings("unchecked")
                    Map<Material, Double> materialMap = (Map<Material, Double>) map;
                    StringBuilder serialized = new StringBuilder();
                    for (Map.Entry<Material, Double> entry : materialMap.entrySet()) {
                        if (!serialized.isEmpty()) serialized.append(";");
                        serialized.append(entry.getKey().name()).append(":").append(entry.getValue());
                    }
                    data.set(settingKey, PersistentDataType.STRING, serialized.toString());
                }
            }
        }
    }

    private ToolSettings loadSettingsFromItem(ItemMeta meta) {
        ToolSettings settings = createDefaultSettings();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        for (String key : getConfigurableSettings()) {
            NamespacedKey settingKey = new NamespacedKey(Main.getInstance(), "setting_" + key);
            Object defaultValue = settings.get(key, null);

            if (defaultValue instanceof Boolean) {
                Byte stored = data.get(settingKey, PersistentDataType.BYTE);
                if (stored != null) settings.set(key, stored == 1);
            } else if (defaultValue instanceof Integer) {
                Integer stored = data.get(settingKey, PersistentDataType.INTEGER);
                if (stored != null) settings.set(key, stored);
            } else if (defaultValue instanceof Double) {
                Double stored = data.get(settingKey, PersistentDataType.DOUBLE);
                if (stored != null) settings.set(key, stored);
            } else if (defaultValue instanceof String) {
                String stored = data.get(settingKey, PersistentDataType.STRING);
                if (stored != null) settings.set(key, stored);
            } else if (defaultValue instanceof List<?>) {
                String stored = data.get(settingKey, PersistentDataType.STRING);
                if (stored != null && !stored.isEmpty()) {
                    List<Material> materials = Arrays.stream(stored.split(","))
                            .map(Material::valueOf)
                            .toList();
                    settings.set(key, materials);
                }
            } else if (defaultValue instanceof Map<?, ?>) {
                String stored = data.get(settingKey, PersistentDataType.STRING);
                if (stored != null && !stored.isEmpty()) {
                    Map<Material, Double> materialMap = new HashMap<>();
                    String[] entries = stored.split(";");
                    for (String entry : entries) {
                        String[] parts = entry.split(":");
                        if (parts.length == 2) {
                            try {
                                Material material = Material.valueOf(parts[0]);
                                Double percentage = Double.valueOf(parts[1]);
                                materialMap.put(material, percentage);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    settings.set(key, materialMap);
                }
            }
        }

        return settings;
    }

    private NamespacedKey getToolIdKey() {
        return new NamespacedKey(Main.getInstance(), "tool-id");
    }

    protected List<Component> createItemLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(ComponentUtils.smallText("&7✦ " + description));
        lore.add(Component.empty());

        lore.add(ComponentUtils.smallText("&b⬅ LEFT CLICK ").append(ComponentUtils.smallText("&f" + getLeftClickDescription())));
        lore.add(ComponentUtils.smallText("&b➡ RIGHT CLICK ").append(ComponentUtils.smallText("&f" + getRightClickDescription())));
        lore.add(ComponentUtils.smallText("&b⚙ SWAP TO OFFHAND ").append(ComponentUtils.smallText("&fᴏᴘᴇɴ ꜱᴇᴛᴛɪɴɢꜱ ᴍᴇɴᴜ")));
        return lore;
    }

    protected String getLeftClickDescription() {
        return "Primary action";
    }

    protected String getRightClickDescription() {
        return "Secondary action";
    }

    public Component getDisplayComponent() {
        return ComponentUtils.colored(displayName, NamedTextColor.AQUA);
    }

    public Component getDescriptionComponent() {
        return ComponentUtils.smallText("&7" + description);
    }
}