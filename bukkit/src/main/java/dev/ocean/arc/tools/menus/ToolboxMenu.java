package dev.ocean.arc.tools.menus;

import dev.ocean.arc.tools.Tool;
import dev.ocean.arc.tools.ToolService;
import dev.ocean.arc.utils.PlayerUtils;
import dev.ocean.arc.utils.menu.MenuUtils;
import dev.ocean.arc.utils.menu.buttons.Button;
import dev.ocean.arc.utils.menu.buttons.impl.SimpleButton;
import dev.ocean.arc.utils.menu.impl.PaginatedMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.ocean.arc.utils.ComponentUtils.smallText;

public class ToolboxMenu extends PaginatedMenu {

    private static final int TOOLS_PER_PAGE = 36;
    private final List<Tool> allTools;

    public ToolboxMenu() {
        super("Arc Toolbox", 54, new ArrayList<>());
        this.allTools = ToolService.get().getTools().values().stream()
                .sorted(Comparator.comparing(Tool::getName))
                .collect(Collectors.toList());
        setupMenu();
    }

    private void setupMenu() {
        List<Button> toolButtons = new ArrayList<>();
        for (Tool tool : allTools) {
            toolButtons.add(createToolButton(tool));
        }
        setItems(toolButtons);

        fillBorder(MenuUtils.createFillerButton(Material.GRAY_STAINED_GLASS_PANE));
        setButton(45, MenuUtils.createBackButton(null));
        setPaginationButtons(
                MenuUtils.createPreviousPageButton(this),
                MenuUtils.createNextPageButton(this),
                48,
                50
        );
    }

    private Button createToolButton(Tool tool) {
        ItemStack displayItem = tool.createItemStack();

        List<Component> displayLore = displayItem.lore();

        displayLore.add(Component.empty());
        displayLore.add(smallText("&9&lClick to get!"));

        return SimpleButton.builder()
                .itemStack(displayItem)
                .leftClick(p -> {
                    p.getInventory().addItem(tool.createItemStack());
                    PlayerUtils.sendInfo(p, "You have received &b" + tool.getDisplayName());
                })
                .lore(displayLore)
                .build();
    }
}