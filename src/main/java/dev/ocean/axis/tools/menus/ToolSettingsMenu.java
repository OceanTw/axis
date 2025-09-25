package dev.ocean.axis.tools.menus;

import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.utils.ComponentUtils;
import dev.ocean.axis.utils.menu.AbstractMenu;
import dev.ocean.axis.utils.menu.MenuUtils;
import dev.ocean.axis.utils.menu.buttons.Button;
import dev.ocean.axis.utils.menu.buttons.impl.SimpleButton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ToolSettingsMenu extends AbstractMenu {

    final Tool tool;
    protected final ToolSettings settings;
    private final Player player;
    final ItemStack toolItem;

    public ToolSettingsMenu(Player player, Tool tool, ItemStack toolItem) {
        super("Tool Settings - " + tool.getDisplayName(), 54);
        this.player = player;
        this.tool = tool;
        this.toolItem = toolItem;
        this.settings = tool.getItemSettings(toolItem);
        setOnClose(p -> {
            tool.saveItemSettings(toolItem, settings);
            p.sendMessage(ComponentUtils.colored("Settings saved to item!", NamedTextColor.GREEN));
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
        return SimpleButton.builder()
                .itemStack(tool.getItem())
                .clickable(false)
                .name(ComponentUtils.colored(tool.getDisplayName(), NamedTextColor.AQUA))
                .lore(List.of(
                        ComponentUtils.colored("Tool Settings Configuration", NamedTextColor.GRAY),
                        Component.empty(),
                        ComponentUtils.colored("Modify your tool settings below", NamedTextColor.BLUE)
                ))
                .build();
    }

    private void setupSettingButtons() {
        int[] itemSlots = new int[getSize() - 18];
        int index = 0;

        int rows = getSize() / 9;
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 0; col < 9; col++) {
                itemSlots[index++] = row * 9 + col;
            }
        }

        int slotIndex = 0;
        for (String key : tool.getConfigurableSettings()) {
            if (slotIndex >= itemSlots.length) break;

            Object value = settings.get(key, tool.createDefaultSettings().get(key, null));
            Button button = null;

            if (value instanceof Boolean) {
                button = createBooleanSetting(key, (Boolean) value);
            } else if (value instanceof Integer) {
                button = createIntegerSetting(key, (Integer) value);
            } else if (value instanceof Double) {
                button = createDoubleSetting(key, (Double) value);
            } else if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Material) {
                button = createMaterialListSetting(key, (List<Material>) list);
            } else if (value instanceof Map<?, ?> map) {
                if (!map.isEmpty() && map.keySet().iterator().next() instanceof Material) {
                    button = createBlockPercentageSetting(key, (Map<Material, Double>) map);
                } else {
                    button = createMapSetting(key, map);
                }
            } else if (value instanceof String) {
                button = createStringSetting(key, (String) value);
            }

            if (button != null) {
                setButton(itemSlots[slotIndex++], button);
            }
        }
    }


    private Button createBooleanSetting(String key, Boolean value) {
        Material material = value ? Material.GREEN_CONCRETE : Material.RED_CONCRETE;
        Component status = value
                ? ComponentUtils.colored("Enabled", NamedTextColor.GREEN)
                : ComponentUtils.colored("Disabled", NamedTextColor.RED);

        return SimpleButton.builder()
                .itemStack(new ItemStack(material))
                .leftClick(p -> {
                    settings.set(key, !value);
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .name(ComponentUtils.colored(formatKey(key), NamedTextColor.AQUA))
                .lore(List.of(
                        ComponentUtils.colored("Current: ", NamedTextColor.GRAY).append(status),
                        Component.empty(),
                        ComponentUtils.colored("Click to toggle", NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createIntegerSetting(String key, Integer value) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.PAPER))
                .leftClick(p -> {
                    settings.set(key, Math.min(value + 1, getMaxValue(key)));
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .rightClick(p -> {
                    settings.set(key, Math.max(value - 1, getMinValue(key)));
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .shiftLeftClick(p -> {
                    settings.set(key, Math.min(value + 10, getMaxValue(key)));
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .shiftRightClick(p -> {
                    settings.set(key, Math.max(value - 10, getMinValue(key)));
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .name(ComponentUtils.colored(formatKey(key), NamedTextColor.AQUA))
                .lore(List.of(
                        ComponentUtils.colored("Current: ", NamedTextColor.GRAY).append(ComponentUtils.colored(String.valueOf(value), NamedTextColor.WHITE)),
                        Component.empty(),
                        ComponentUtils.colored("Left Click: +1", NamedTextColor.BLUE),
                        ComponentUtils.colored("Right Click: -1", NamedTextColor.BLUE),
                        ComponentUtils.colored("Shift Left: +10", NamedTextColor.BLUE),
                        ComponentUtils.colored("Shift Right: -10", NamedTextColor.BLUE)
                ))
                .amount(Math.min(64, Math.max(1, value)))
                .build();
    }

    private Button createDoubleSetting(String key, Double value) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.PAPER))
                .leftClick(p -> {
                    settings.set(key, Math.min(value + 0.1, getMaxDoubleValue(key)));
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .rightClick(p -> {
                    settings.set(key, Math.max(value - 0.1, getMinDoubleValue(key)));
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .shiftLeftClick(p -> {
                    settings.set(key, Math.min(value + 1.0, getMaxDoubleValue(key)));
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .shiftRightClick(p -> {
                    settings.set(key, Math.max(value - 1.0, getMinDoubleValue(key)));
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .name(ComponentUtils.colored(formatKey(key), NamedTextColor.AQUA))
                .lore(List.of(
                        ComponentUtils.colored("Current: ", NamedTextColor.GRAY).append(ComponentUtils.colored(String.format("%.1f", value), NamedTextColor.WHITE)),
                        Component.empty(),
                        ComponentUtils.colored("Left Click: +0.1", NamedTextColor.BLUE),
                        ComponentUtils.colored("Right Click: -0.1", NamedTextColor.BLUE),
                        ComponentUtils.colored("Shift Left: +1.0", NamedTextColor.BLUE),
                        ComponentUtils.colored("Shift Right: -1.0", NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createMaterialListSetting(String key, List<Material> materials) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.CHEST))
                .leftClick(p -> {
                    new MaterialSelectionMenu(p, key, materials, this).open(p);
                })
                .name(ComponentUtils.colored(formatKey(key), NamedTextColor.AQUA))
                .lore(List.of(
                        ComponentUtils.colored("Materials: ", NamedTextColor.GRAY).append(ComponentUtils.colored(String.valueOf(materials.size()), NamedTextColor.WHITE)),
                        Component.empty(),
                        ComponentUtils.colored("Click to configure materials", NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createBlockPercentageSetting(String key, Map<Material, Double> blocks) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.COMPARATOR))
                .leftClick(p -> {
                    new BlockPercentageMenu(p, key, blocks, this).open(p);
                })
                .name(ComponentUtils.colored(formatKey(key), NamedTextColor.AQUA))
                .lore(List.of(
                        ComponentUtils.colored("Blocks: ", NamedTextColor.GRAY).append(ComponentUtils.colored(String.valueOf(blocks.size()), NamedTextColor.WHITE)),
                        Component.empty(),
                        ComponentUtils.colored("Click to configure blocks and percentages", NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createMapSetting(String key, Map<?, ?> map) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.MAP))
                .leftClick(p -> {
                    player.sendMessage(ComponentUtils.colored("Map settings are not yet configurable in GUI", NamedTextColor.RED));
                })
                .name(ComponentUtils.colored(formatKey(key), NamedTextColor.AQUA))
                .lore(List.of(
                        ComponentUtils.colored("Map Size: ", NamedTextColor.GRAY).append(ComponentUtils.colored(String.valueOf(map.size()), NamedTextColor.WHITE)),
                        Component.empty(),
                        ComponentUtils.colored("Not yet configurable", NamedTextColor.RED)
                ))
                .build();
    }

    private Button createStringSetting(String key, String value) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.NAME_TAG))
                .leftClick(p -> {
                    player.sendMessage(ComponentUtils.colored("String settings are not yet configurable in GUI", NamedTextColor.RED));
                })
                .name(ComponentUtils.colored(formatKey(key), NamedTextColor.AQUA))
                .lore(List.of(
                        ComponentUtils.colored("Value: ", NamedTextColor.GRAY).append(ComponentUtils.colored(value, NamedTextColor.WHITE)),
                        Component.empty(),
                        ComponentUtils.colored("Not yet configurable", NamedTextColor.RED)
                ))
                .build();
    }

    private Button createResetButton() {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.BARRIER))
                .leftClick(p -> {
                    ToolSettings defaultSettings = tool.createDefaultSettings();
                    for (String key : tool.getConfigurableSettings()) {
                        Object defaultValue = defaultSettings.get(key, null);
                        if (defaultValue != null) {
                            settings.set(key, defaultValue);
                        }
                    }
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                    player.sendMessage(ComponentUtils.colored("Settings reset to defaults!", NamedTextColor.GREEN));
                })
                .name(ComponentUtils.colored("Reset to Defaults", NamedTextColor.RED))
                .lore(List.of(
                        ComponentUtils.colored("Click to reset all settings", NamedTextColor.GRAY),
                        ComponentUtils.colored("to their default values", NamedTextColor.GRAY),
                        Component.empty(),
                        ComponentUtils.colored("Click to reset", NamedTextColor.BLUE)
                ))
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