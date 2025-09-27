package dev.ocean.arc.tools.menus;

import dev.ocean.arc.tools.Tool;
import dev.ocean.arc.tools.ToolSettings;
import dev.ocean.arc.utils.menu.AbstractMenu;
import dev.ocean.arc.utils.menu.MenuUtils;
import dev.ocean.arc.utils.menu.buttons.Button;
import dev.ocean.arc.utils.menu.buttons.impl.SimpleButton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static dev.ocean.arc.utils.ComponentUtils.smallText;

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
            p.sendMessage(smallText("Settings saved to item!").color(NamedTextColor.GREEN));
        });
        setupMenu();
    }

    private void setupMenu() {
        fillBorder(MenuUtils.createFillerButton(Material.BLUE_STAINED_GLASS_PANE));
        setButton(49, MenuUtils.createCloseButton());
        setButton(4, createToolInfoButton());
        setupSettingButtons();
        setButton(45, createResetButton());
    }

    private Button createToolInfoButton() {
        return SimpleButton.builder()
                .itemStack(tool.getItem())
                .clickable(false)
                .name(smallText(tool.getDisplayName()).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Tool Settings Configuration").color(NamedTextColor.GRAY),
                        Component.empty(),
                        smallText("Modify your tool settings below").color(NamedTextColor.BLUE)
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
                ? smallText("Enabled").color(NamedTextColor.GREEN)
                : smallText("Disabled").color(NamedTextColor.RED);

        return SimpleButton.builder()
                .itemStack(new ItemStack(material))
                .leftClick(p -> {
                    settings.set(key, !value);
                    tool.saveItemSettings(toolItem, settings);
                    setupMenu();
                })
                .name(smallText(formatKey(key)).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Current: ").color(NamedTextColor.GRAY).append(status),
                        Component.empty(),
                        smallText("Click to toggle").color(NamedTextColor.BLUE)
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
                .name(smallText(formatKey(key)).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Current: ").color(NamedTextColor.GRAY).append(smallText(String.valueOf(value)).color(NamedTextColor.WHITE)),
                        Component.empty(),
                        smallText("Left Click: +1").color(NamedTextColor.BLUE),
                        smallText("Right Click: -1").color(NamedTextColor.BLUE),
                        smallText("Shift Left: +10").color(NamedTextColor.BLUE),
                        smallText("Shift Right: -10").color(NamedTextColor.BLUE)
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
                .name(smallText(formatKey(key)).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Current: ").color(NamedTextColor.GRAY).append(smallText(String.format("%.1f", value)).color(NamedTextColor.WHITE)),
                        Component.empty(),
                        smallText("Left Click: +0.1").color(NamedTextColor.BLUE),
                        smallText("Right Click: -0.1").color(NamedTextColor.BLUE),
                        smallText("Shift Left: +1.0").color(NamedTextColor.BLUE),
                        smallText("Shift Right: -1.0").color(NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createMaterialListSetting(String key, List<Material> materials) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.CHEST))
                .leftClick(p -> {
                    new MaterialSelectionMenu(p, key, materials, this).open(p);
                })
                .name(smallText(formatKey(key)).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Materials: ").color(NamedTextColor.GRAY).append(smallText(String.valueOf(materials.size())).color(NamedTextColor.WHITE)),
                        Component.empty(),
                        smallText("Click to configure materials").color(NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createBlockPercentageSetting(String key, Map<Material, Double> blocks) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.COMPARATOR))
                .leftClick(p -> {
                    new BlockPercentageMenu(p, key, blocks, this).open(p);
                })
                .name(smallText(formatKey(key)).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Blocks: ").color(NamedTextColor.GRAY).append(smallText(String.valueOf(blocks.size())).color(NamedTextColor.WHITE)),
                        Component.empty(),
                        smallText("Click to configure blocks and percentages").color(NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createMapSetting(String key, Map<?, ?> map) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.MAP))
                .leftClick(p -> {
                    player.sendMessage(smallText("Map settings are not yet configurable in GUI").color(NamedTextColor.RED));
                })
                .name(smallText(formatKey(key)).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Map Size: ").color(NamedTextColor.GRAY).append(smallText(String.valueOf(map.size())).color(NamedTextColor.WHITE)),
                        Component.empty(),
                        smallText("Not yet configurable").color(NamedTextColor.RED)
                ))
                .build();
    }

    private Button createStringSetting(String key, String value) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.NAME_TAG))
                .leftClick(p -> {
                    player.sendMessage(smallText("String settings are not yet configurable in GUI").color(NamedTextColor.RED));
                })
                .name(smallText(formatKey(key)).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Value: ").color(NamedTextColor.GRAY).append(smallText(value).color(NamedTextColor.WHITE)),
                        Component.empty(),
                        smallText("Not yet configurable").color(NamedTextColor.RED)
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
                    player.sendMessage(smallText("Settings reset to defaults!").color(NamedTextColor.GREEN));
                })
                .name(smallText("Reset to Defaults").color(NamedTextColor.RED))
                .lore(List.of(
                        smallText("Click to reset all settings").color(NamedTextColor.GRAY),
                        smallText("to their default values").color(NamedTextColor.GRAY),
                        Component.empty(),
                        smallText("Click to reset").color(NamedTextColor.BLUE)
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