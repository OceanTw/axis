package dev.ocean.axis.tools.menus;

import dev.ocean.axis.utils.PlayerUtils;
import dev.ocean.axis.utils.menu.MenuUtils;
import dev.ocean.axis.utils.menu.buttons.Button;
import dev.ocean.axis.utils.menu.buttons.impl.SimpleButton;
import dev.ocean.axis.utils.menu.impl.PaginatedMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static dev.ocean.axis.utils.ComponentUtils.smallText;

public class BlockPercentageMenu extends PaginatedMenu {

    private final Player player;
    private final String settingKey;
    private final Map<Material, Double> blocks;
    private final ToolSettingsMenu parentMenu;
    private final List<Material> allMaterials;
    private static final int MATERIALS_PER_PAGE = 36;

    public BlockPercentageMenu(Player player, String settingKey, Map<Material, Double> blocks, ToolSettingsMenu parentMenu) {
        super("Block Percentages - " + settingKey, 54, new ArrayList<>());
        this.player = player;
        this.settingKey = settingKey;
        this.blocks = new HashMap<>(blocks);
        this.parentMenu = parentMenu;
        this.allMaterials = Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .filter(mat -> !mat.isAir())
                .filter(Material::isItem)
                .sorted((mat1, mat2) -> {
                    boolean mat1HasPercentage = blocks.containsKey(mat1) && blocks.get(mat1) > 0;
                    boolean mat2HasPercentage = blocks.containsKey(mat2) && blocks.get(mat2) > 0;
                    if (mat1HasPercentage && !mat2HasPercentage) {
                        return -1;
                    } else if (!mat1HasPercentage && mat2HasPercentage) {
                        return 1;
                    } else if (mat1HasPercentage && mat2HasPercentage) {
                        return Double.compare(blocks.get(mat2), blocks.get(mat1));
                    } else {
                        return mat1.name().compareTo(mat2.name());
                    }
                })
                .toList();
        setOnClose(p -> {
            parentMenu.settings.set(settingKey, blocks);
            parentMenu.tool.saveItemSettings(parentMenu.toolItem, parentMenu.settings);
        });
        setupMenu();
    }

    private void setupMenu() {
        List<Button> materialButtons = new ArrayList<>();
        for (Material material : allMaterials) {
            double percentage = blocks.getOrDefault(material, 0.0);
            Button button = createMaterialButton(material, percentage);
            if (button != null) materialButtons.add(button);
        }
        setItems(materialButtons);

        fillBorder(MenuUtils.createFillerButton(Material.BLUE_STAINED_GLASS_PANE));
        setButton(45, createBackButton());
        setButton(53, createClearAllButton());
        setButton(49, createNormalizeButton());
        setPaginationButtons(
                MenuUtils.createPreviousPageButton(this),
                MenuUtils.createNextPageButton(this),
                46,
                52
        );
    }

    private Button createMaterialButton(Material material, double percentage) {
        ItemStack item;
        try {
            item = new ItemStack(material);
        } catch (IllegalArgumentException e) {
            return null;
        }
        boolean hasPercentage = percentage > 0;
        return SimpleButton.builder()
                .itemStack(item)
                .leftClick(p -> {
                    if (hasPercentage) {
                        blocks.put(material, Math.min(percentage + 1.0, 100.0));
                    } else {
                        blocks.put(material, 1.0);
                    }
                    parentMenu.settings.set(settingKey, blocks);
                    parentMenu.tool.saveItemSettings(parentMenu.toolItem, parentMenu.settings);
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
                    parentMenu.settings.set(settingKey, blocks);
                    parentMenu.tool.saveItemSettings(parentMenu.toolItem, parentMenu.settings);
                    setupMenu();
                })
                .shiftLeftClick(p -> {
                    if (hasPercentage) {
                        blocks.put(material, Math.min(percentage + 10.0, 100.0));
                    } else {
                        blocks.put(material, 10.0);
                    }
                    parentMenu.settings.set(settingKey, blocks);
                    parentMenu.tool.saveItemSettings(parentMenu.toolItem, parentMenu.settings);
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
                    parentMenu.settings.set(settingKey, blocks);
                    parentMenu.tool.saveItemSettings(parentMenu.toolItem, parentMenu.settings);
                    setupMenu();
                })
                .name(smallText(material.name()).color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Percentage: ").color(NamedTextColor.GRAY)
                                .append(smallText(String.format("%.1f%%", percentage)).color(NamedTextColor.WHITE)),
                        Component.empty(),
                        smallText("Left Click: +1%").color(NamedTextColor.BLUE),
                        smallText("Right Click: -1%").color(NamedTextColor.BLUE),
                        smallText("Shift Left Click: +10%").color(NamedTextColor.BLUE),
                        smallText("Shift Right Click: -10%").color(NamedTextColor.BLUE)
                ))
                .amount(Math.max(1, (int) percentage))
                .build();
    }

    private Button createClearAllButton() {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.RED_CONCRETE))
                .leftClick(p -> {
                    blocks.clear();
                    parentMenu.settings.set(settingKey, blocks);
                    parentMenu.tool.saveItemSettings(parentMenu.toolItem, parentMenu.settings);
                    setupMenu();
                })
                .name(smallText("Clear All").color(NamedTextColor.RED))
                .lore(List.of(
                        smallText("Remove all blocks").color(NamedTextColor.GRAY),
                        Component.empty(),
                        smallText("Click to clear").color(NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createNormalizeButton() {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.EMERALD_BLOCK))
                .leftClick(p -> {
                    if (!blocks.isEmpty()) {
                        double equalPercentage = 100.0 / blocks.size();
                        blocks.replaceAll((material, per) -> equalPercentage);
                        parentMenu.settings.set(settingKey, blocks);
                        parentMenu.tool.saveItemSettings(parentMenu.toolItem, parentMenu.settings);
                        setupMenu();
                        PlayerUtils.sendInfo(player, "Percentages normalized to equal distribution!");
                    }
                })
                .name(smallText("Normalize Percentages").color(NamedTextColor.AQUA))
                .lore(List.of(
                        smallText("Distributes 100% equally").color(NamedTextColor.GRAY),
                        smallText("among all selected blocks").color(NamedTextColor.GRAY),
                        Component.empty(),
                        smallText("Click to normalize").color(NamedTextColor.BLUE)
                ))
                .build();
    }

    private Button createBackButton() {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(p -> {
                    new ToolSettingsMenu(player, parentMenu.tool, parentMenu.toolItem).open(player);
                })
                .name(smallText("Back").color(NamedTextColor.BLUE))
                .lore(List.of(
                        smallText("Return to tool settings").color(NamedTextColor.GRAY)
                ))
                .build();
    }
}