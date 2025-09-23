package dev.ocean.axis.tools.menus;

import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.utils.menu.*;
import dev.ocean.axis.utils.menu.builders.ButtonBuilder;
import dev.ocean.axis.utils.menu.buttons.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ToolSettingsMenu extends AbstractMenu {

    private final Tool tool;
    protected final ToolSettings settings;
    private final Player player;
    private final ItemStack toolItem;

    public ToolSettingsMenu(Player player, Tool tool, ItemStack toolItem) {
        super("§8Tool Settings - " + tool.getName(), 54);
        this.player = player;
        this.tool = tool;
        this.toolItem = toolItem;
        this.settings = tool.getItemSettings(toolItem);
        setOnClose(p -> {
            tool.saveItemSettings(toolItem, settings);
            p.sendMessage("§aSettings saved to item!");
        });
        setupMenu();
    }

    private void setupMenu() {
        fillBorder(MenuUtils.createFillerButton(Material.BLACK_STAINED_GLASS_PANE));

        setButton(49, MenuUtils.createCloseButton());

        setButton(4, createToolInfoButton());

        setupSettingButtons();

        setButton(45, createResetButton());
    }

    private Button createToolInfoButton() {
        return ButtonBuilder.builder()
                .itemStack(tool.getItem())
                .clickable(false)
                .build()
                .name("§6" + tool.getName())
                .lore("§7Tool Settings Configuration", "", "§eModify your tool settings below")
                .build();
    }

    private void setupSettingButtons() {
        int slot = 10;

        for (String key : tool.getConfigurableSettings()) {
            Object value = settings.get(key, tool.createDefaultSettings().get(key, null));

            if (value instanceof Boolean) {
                setButton(slot, createBooleanSetting(key, (Boolean) value));
            } else if (value instanceof Integer) {
                setButton(slot, createIntegerSetting(key, (Integer) value));
            } else if (value instanceof Double) {
                setButton(slot, createDoubleSetting(key, (Double) value));
            } else if (value instanceof List<?> && !((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof Material) {
                setButton(slot, createMaterialListSetting(key, (List<Material>) value));
            } else if (value instanceof Map<?, ?>) {
                // Check if it's a Map<Material, Double> (blocks with percentages)
                Map<?, ?> map = (Map<?, ?>) value;
                if (!map.isEmpty() && map.keySet().iterator().next() instanceof Material) {
                    setButton(slot, createBlockPercentageSetting(key, (Map<Material, Double>) value));
                } else {
                    setButton(slot, createMapSetting(key, map));
                }
            } else if (value instanceof String) {
                setButton(slot, createStringSetting(key, (String) value));
            }

            slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 44) break;
        }
    }

    private Button createBooleanSetting(String key, Boolean value) {
        Material material = value ? Material.GREEN_CONCRETE : Material.RED_CONCRETE;
        String status = value ? "§aEnabled" : "§cDisabled";

        return ButtonBuilder.builder()
                .itemStack(new ItemStack(material))
                .leftClick(p -> {
                    settings.set(key, !value);
                    refresh();
                    setupSettingButtons();
                })
                .build()
                .name("§6" + formatKey(key))
                .lore("§7Current: " + status, "", "§eClick to toggle")
                .build();
    }

    private Button createIntegerSetting(String key, Integer value) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.PAPER))
                .leftClick(p -> {
                    settings.set(key, Math.min(value + 1, getMaxValue(key)));
                    refresh();
                    setupSettingButtons();
                })
                .rightClick(p -> {
                    settings.set(key, Math.max(value - 1, getMinValue(key)));
                    refresh();
                    setupSettingButtons();
                })
                .shiftLeftClick(p -> {
                    settings.set(key, Math.min(value + 10, getMaxValue(key)));
                    refresh();
                    setupSettingButtons();
                })
                .shiftRightClick(p -> {
                    settings.set(key, Math.max(value - 10, getMinValue(key)));
                    refresh();
                    setupSettingButtons();
                })
                .build()
                .name("§6" + formatKey(key))
                .lore("§7Current: §f" + value, "", "§eLeft Click: +1", "§eRight Click: -1", "§eShift Left: +10", "§eShift Right: -10")
                .amount(Math.min(64, Math.max(1, value)))
                .build();
    }

    private Button createDoubleSetting(String key, Double value) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.PAPER))
                .leftClick(p -> {
                    settings.set(key, Math.min(value + 0.1, getMaxDoubleValue(key)));
                    refresh();
                    setupSettingButtons();
                })
                .rightClick(p -> {
                    settings.set(key, Math.max(value - 0.1, getMinDoubleValue(key)));
                    refresh();
                    setupSettingButtons();
                })
                .shiftLeftClick(p -> {
                    settings.set(key, Math.min(value + 1.0, getMaxDoubleValue(key)));
                    refresh();
                    setupSettingButtons();
                })
                .shiftRightClick(p -> {
                    settings.set(key, Math.max(value - 1.0, getMinDoubleValue(key)));
                    refresh();
                    setupSettingButtons();
                })
                .build()
                .name("§6" + formatKey(key))
                .lore("§7Current: §f" + String.format("%.1f", value), "", "§eLeft Click: +0.1", "§eRight Click: -0.1", "§eShift Left: +1.0", "§eShift Right: -1.0")
                .build();
    }

    private Button createMaterialListSetting(String key, List<Material> materials) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.CHEST))
                .leftClick(p -> {
                    new MaterialSelectionMenu(p, key, materials, this).open(p);
                })
                .build()
                .name("§6" + formatKey(key))
                .lore("§7Materials: §f" + materials.size(), "", "§eClick to configure materials")
                .build();
    }

    private Button createBlockPercentageSetting(String key, Map<Material, Double> blocks) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.COMPARATOR))
                .leftClick(p -> {
                    new BlockPercentageMenu(p, key, blocks, this).open(p);
                })
                .build()
                .name("§6" + formatKey(key))
                .lore("§7Blocks: §f" + blocks.size(), "", "§eClick to configure blocks and percentages")
                .build();
    }

    private Button createMapSetting(String key, Map<?, ?> map) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.MAP))
                .leftClick(p -> {
                    player.sendMessage("§cMap settings are not yet configurable in GUI");
                })
                .build()
                .name("§6" + formatKey(key))
                .lore("§7Map Size: §f" + map.size(), "", "§cNot yet configurable")
                .build();
    }

    private Button createStringSetting(String key, String value) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.NAME_TAG))
                .leftClick(p -> {
                    player.sendMessage("§cString settings are not yet configurable in GUI");
                })
                .build()
                .name("§6" + formatKey(key))
                .lore("§7Value: §f" + value, "", "§cNot yet configurable")
                .build();
    }

    private Button createResetButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.BARRIER))
                .leftClick(p -> {
                    ToolSettings defaultSettings = tool.createDefaultSettings();
                    for (String key : tool.getConfigurableSettings()) {
                        Object defaultValue = defaultSettings.get(key, null);
                        if (defaultValue != null) {
                            settings.set(key, defaultValue);
                        }
                    }
                    refresh();
                    setupSettingButtons();
                    player.sendMessage("§aSettings reset to defaults!");
                })
                .build()
                .name("§cReset to Defaults")
                .lore("§7Click to reset all settings", "§7to their default values", "", "§eClick to reset")
                .build();
    }

    private String formatKey(String key) {
        return Arrays.stream(key.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(key);
    }

    private int getMinValue(String key) {
        return switch (key.toLowerCase()) {
            case "radius", "size", "amount" -> 1;
            case "delay" -> 0;
            default -> 0;
        };
    }

    private int getMaxValue(String key) {
        return switch (key.toLowerCase()) {
            case "radius" -> 50;
            case "size" -> 100;
            case "amount" -> 64;
            case "delay" -> 100;
            default -> 100;
        };
    }

    private double getMinDoubleValue(String key) {
        return switch (key.toLowerCase()) {
            case "chance", "percentage" -> 0.0;
            case "multiplier" -> 0.1;
            default -> 0.0;
        };
    }

    private double getMaxDoubleValue(String key) {
        return switch (key.toLowerCase()) {
            case "chance", "percentage" -> 1.0;
            case "multiplier" -> 10.0;
            default -> 100.0;
        };
    }
}

class MaterialSelectionMenu extends AbstractMenu {

    private final Player player;
    private final String settingKey;
    private final List<Material> selectedMaterials;
    private final ToolSettingsMenu parentMenu;
    private final List<Material> allMaterials;
    private int currentPage = 0;
    private final int materialsPerPage = 36;

    public MaterialSelectionMenu(Player player, String settingKey, List<Material> selectedMaterials, ToolSettingsMenu parentMenu) {
        super("§8Select Materials - " + settingKey, 54);
        this.player = player;
        this.settingKey = settingKey;
        this.selectedMaterials = new ArrayList<>(selectedMaterials);
        this.parentMenu = parentMenu;
        this.allMaterials = Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .filter(mat -> !mat.isAir())
                .filter(Material::isItem) // Only materials that can be items
                .sorted(Comparator.comparing(Material::name))
                .toList();
        setupMenu();
    }

    public ToolSettings getSettings() {
        return parentMenu.settings;
    }

    private void setupMenu() {
        fillBorder(MenuUtils.createFillerButton(Material.BLACK_STAINED_GLASS_PANE));

        setButton(45, MenuUtils.createBackButton(parentMenu));
        setButton(49, createSaveButton());
        setButton(53, createClearAllButton());

        if (currentPage > 0) {
            setButton(48, createPreviousPageButton());
        }

        if ((currentPage + 1) * materialsPerPage < allMaterials.size()) {
            setButton(50, createNextPageButton());
        }

        setupMaterialButtons();
    }

    private void setupMaterialButtons() {
        int startIndex = currentPage * materialsPerPage;
        int endIndex = Math.min(startIndex + materialsPerPage, allMaterials.size());

        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            Material material = allMaterials.get(i);
            boolean selected = selectedMaterials.contains(material);

            setButton(slot, createMaterialButton(material, selected));

            slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 44) break;
        }
    }

    private Button createMaterialButton(Material material, boolean selected) {
        ItemStack item = new ItemStack(material);
        String status = selected ? "§aSelected" : "§7Not Selected";

        return ButtonBuilder.builder()
                .itemStack(item)
                .leftClick(p -> {
                    if (selected) {
                        selectedMaterials.remove(material);
                    } else {
                        selectedMaterials.add(material);
                    }
                    refresh();
                    setupMenu();
                })
                .build()
                .name("§6" + material.name())
                .lore("§7Status: " + status, "", "§eClick to toggle")
                .build();
    }

    private Button createSaveButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.GREEN_CONCRETE))
                .leftClick(p -> {
                    parentMenu.settings.setMaterials(settingKey, selectedMaterials);
                    parentMenu.open(player);
                })
                .build()
                .name("§aSave Selection")
                .lore("§7Selected: §f" + selectedMaterials.size() + " materials", "", "§eClick to save and return")
                .build();
    }

    private Button createClearAllButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.RED_CONCRETE))
                .leftClick(p -> {
                    selectedMaterials.clear();
                    refresh();
                    setupMenu();
                })
                .build()
                .name("§cClear All")
                .lore("§7Remove all selected materials", "", "§eClick to clear")
                .build();
    }

    private Button createPreviousPageButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(p -> {
                    currentPage--;
                    refresh();
                    setupMenu();
                })
                .build()
                .name("§ePrevious Page")
                .lore("§7Page " + currentPage + "/" + (allMaterials.size() / materialsPerPage))
                .build();
    }

    private Button createNextPageButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(p -> {
                    currentPage++;
                    refresh();
                    setupMenu();
                })
                .build()
                .name("§eNext Page")
                .lore("§7Page " + (currentPage + 2) + "/" + ((allMaterials.size() / materialsPerPage) + 1))
                .build();
    }
}

class BlockPercentageMenu extends AbstractMenu {

    private final Player player;
    private final String settingKey;
    private final Map<Material, Double> blocks;
    private final ToolSettingsMenu parentMenu;
    private final List<Material> allMaterials;
    private int currentPage = 0;
    private final int materialsPerPage = 36;

    public BlockPercentageMenu(Player player, String settingKey, Map<Material, Double> blocks, ToolSettingsMenu parentMenu) {
        super("§8Block Percentages - " + settingKey, 54);
        this.player = player;
        this.settingKey = settingKey;
        this.blocks = new HashMap<>(blocks);
        this.parentMenu = parentMenu;
        this.allMaterials = Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .filter(mat -> !mat.isAir())
                .filter(Material::isItem) // Only materials that can be items
                .sorted(Comparator.comparing(Material::name))
                .toList();
        setupMenu();
    }

    private void setupMenu() {
        fillBorder(MenuUtils.createFillerButton(Material.BLACK_STAINED_GLASS_PANE));

        setButton(45, MenuUtils.createBackButton(parentMenu));
        setButton(49, createSaveButton());
        setButton(53, createClearAllButton());
        setButton(47, createNormalizeButton());

        if (currentPage > 0) {
            setButton(46, createPreviousPageButton());
        }

        if ((currentPage + 1) * materialsPerPage < allMaterials.size()) {
            setButton(52, createNextPageButton());
        }

        setupMaterialButtons();
    }

    private void setupMaterialButtons() {
        int startIndex = currentPage * materialsPerPage;
        int endIndex = Math.min(startIndex + materialsPerPage, allMaterials.size());

        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            Material material = allMaterials.get(i);
            double percentage = blocks.getOrDefault(material, 0.0);

            if (percentage > 0 || currentPage == 0) { // Show on first page or if has percentage
                Button button = createMaterialButton(material, percentage);
                if (button != null) { // Only set button if it was created successfully
                    setButton(slot, button);
                }

                slot++;
                if (slot % 9 == 8) slot += 2;
                if (slot >= 44) break;
            }
        }
    }

    private Button createMaterialButton(Material material, double percentage) {
        ItemStack item;
        try {
            item = new ItemStack(material);
        } catch (IllegalArgumentException e) {
            // Skip materials that can't be items
            return null;
        }

        boolean hasPercentage = percentage > 0;

        return ButtonBuilder.builder()
                .itemStack(item)
                .leftClick(p -> {
                    if (hasPercentage) {
                        blocks.put(material, Math.min(percentage + 1.0, 100.0));
                    } else {
                        blocks.put(material, 1.0);
                    }
                    refresh();
                    setupMenu();
                })
                .rightClick(p -> {
                    if (hasPercentage) {
                        double newPercentage = Math.max(percentage - 1.0, 0.0);
                        if (newPercentage == 0.0) {
                            blocks.remove(material);
                        } else {
                            blocks.put(material, newPercentage);
                        }
                    }
                    refresh();
                    setupMenu();
                })
                .shiftLeftClick(p -> {
                    if (hasPercentage) {
                        blocks.put(material, Math.min(percentage + 10.0, 100.0));
                    } else {
                        blocks.put(material, 10.0);
                    }
                    refresh();
                    setupMenu();
                })
                .shiftRightClick(p -> {
                    if (hasPercentage) {
                        double newPercentage = Math.max(percentage - 10.0, 0.0);
                        if (newPercentage == 0.0) {
                            blocks.remove(material);
                        } else {
                            blocks.put(material, newPercentage);
                        }
                    }
                    refresh();
                    setupMenu();
                })
                .build()
                .name("§6" + material.name())
                .lore(
                        "§7Percentage: §f" + String.format("%.1f%%", percentage),
                        "",
                        "§eLeft Click: +1%",
                        "§eRight Click: -1%",
                        "§eShift Left: +10%",
                        "§eShift Right: -10%"
                )
                .amount(Math.max(1, Math.min(64, (int) percentage)))
                .build();
    }

    private Button createSaveButton() {
        double totalPercentage = blocks.values().stream().mapToDouble(Double::doubleValue).sum();

        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.GREEN_CONCRETE))
                .leftClick(p -> {
                    parentMenu.settings.setMaterialPercentages(settingKey, blocks);
                    parentMenu.open(player);
                })
                .build()
                .name("§aSave Configuration")
                .lore(
                        "§7Blocks: §f" + blocks.size(),
                        "§7Total: §f" + String.format("%.1f%%", totalPercentage),
                        "",
                        "§eClick to save and return"
                )
                .build();
    }

    private Button createClearAllButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.RED_CONCRETE))
                .leftClick(p -> {
                    blocks.clear();
                    refresh();
                    setupMenu();
                })
                .build()
                .name("§cClear All")
                .lore("§7Remove all blocks", "", "§eClick to clear")
                .build();
    }

    private Button createNormalizeButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.REDSTONE_BLOCK))
                .leftClick(p -> {
                    if (!blocks.isEmpty()) {
                        double equalPercentage = 100.0 / blocks.size();
                        blocks.replaceAll((material, percentage) -> equalPercentage);
                        refresh();
                        setupMenu();
                        player.sendMessage("§aPercentages normalized to equal distribution!");
                    }
                })
                .build()
                .name("§6Normalize Percentages")
                .lore(
                        "§7Distributes 100% equally",
                        "§7among all selected blocks",
                        "",
                        "§eClick to normalize"
                )
                .build();
    }

    private Button createPreviousPageButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(p -> {
                    currentPage--;
                    refresh();
                    setupMenu();
                })
                .build()
                .name("§ePrevious Page")
                .lore("§7Page " + currentPage + "/" + (allMaterials.size() / materialsPerPage))
                .build();
    }

    private Button createNextPageButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(p -> {
                    currentPage++;
                    refresh();
                    setupMenu();
                })
                .build()
                .name("§eNext Page")
                .lore("§7Page " + (currentPage + 2) + "/" + ((allMaterials.size() / materialsPerPage) + 1))
                .build();
    }
}