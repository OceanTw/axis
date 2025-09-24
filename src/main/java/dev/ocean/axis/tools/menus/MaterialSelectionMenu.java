package dev.ocean.axis.tools.menus;

import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.utils.ComponentUtils;
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

public class MaterialSelectionMenu extends PaginatedMenu {

    private final Player player;
    private final String settingKey;
    private final List<Material> selectedMaterials;
    private final ToolSettingsMenu parentMenu;
    private final List<Material> allMaterials;
    private static final int MATERIALS_PER_PAGE = 36;

    public MaterialSelectionMenu(Player player, String settingKey, List<Material> selectedMaterials, ToolSettingsMenu parentMenu) {
        super("Select Materials - " + settingKey, 54, new ArrayList<>());
        this.player = player;
        this.settingKey = settingKey;
        this.selectedMaterials = new ArrayList<>(selectedMaterials);
        this.parentMenu = parentMenu;
        this.allMaterials = Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .filter(mat -> !mat.isAir())
                .filter(Material::isItem)
                .sorted(Comparator.comparing(Material::name))
                .toList();
        setOnClose(p -> {
            parentMenu.settings.setMaterials(settingKey, selectedMaterials);
            parentMenu.tool.saveItemSettings(parentMenu.toolItem, parentMenu.settings);
        });
        setupMenu();
    }

    private void setupMenu() {
        List<Button> materialButtons = new ArrayList<>();
        for (Material material : allMaterials) {
            boolean selected = selectedMaterials.contains(material);
            materialButtons.add(createMaterialButton(material, selected));
        }
        setItems(materialButtons);

        fillBorder(MenuUtils.createFillerButton(Material.BLACK_STAINED_GLASS_PANE));
        setButton(45, MenuUtils.createBackButton(parentMenu));
        setButton(53, createClearAllButton());
        // Paginated navigation
        setPaginationButtons(
                MenuUtils.createPreviousPageButton(this),
                MenuUtils.createNextPageButton(this),
                48,
                50
        );
    }

    private Button createMaterialButton(Material material, boolean selected) {
        ItemStack item;
        try {
            item = new ItemStack(material);
        } catch (IllegalArgumentException e) {
            return null;
        }
        Component status = selected
                ? ComponentUtils.colored("Selected", NamedTextColor.GREEN)
                : ComponentUtils.colored("Not Selected", NamedTextColor.GRAY);

        return SimpleButton.builder()
                .itemStack(item)
                .leftClick(p -> {
                    if (selected) {
                        selectedMaterials.remove(material);
                    } else {
                        selectedMaterials.add(material);
                    }
                    setupMenu();
                })
                .name(ComponentUtils.colored(material.name(), NamedTextColor.GOLD))
                .lore(List.of(
                        ComponentUtils.colored("Status: ", NamedTextColor.GRAY).append(status),
                        Component.empty(),
                        ComponentUtils.colored("Click to toggle", NamedTextColor.YELLOW)
                ))
                .build();
    }

    private Button createClearAllButton() {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.RED_CONCRETE))
                .leftClick(p -> {
                    selectedMaterials.clear();
                    refresh();
                    setupMenu();
                })
                .name(ComponentUtils.colored("Clear All", NamedTextColor.RED))
                .lore(List.of(
                        ComponentUtils.colored("Remove all selected materials", NamedTextColor.GRAY),
                        Component.empty(),
                        ComponentUtils.colored("Click to clear", NamedTextColor.YELLOW)
                ))
                .build();
    }
}