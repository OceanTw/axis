package dev.ocean.axis.utils.menu;

import dev.ocean.axis.utils.ComponentUtils;
import dev.ocean.axis.utils.menu.buttons.Button;
import dev.ocean.axis.utils.menu.buttons.impl.SimpleButton;
import dev.ocean.axis.utils.menu.buttons.impl.ToggleButton;
import dev.ocean.axis.utils.menu.impl.PaginatedMenu;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MenuUtils {

    public Button createFillerButton(Material material) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(material))
                .clickable(false)
                .name(ComponentUtils.plain(" "))
                .build();
    }

    public Button createCloseButton() {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.BARRIER))
                .leftClick(Player::closeInventory)
                .name(ComponentUtils.colored("Close", NamedTextColor.RED))
                .build();
    }

    public Button createBackButton(Menu previousMenu) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(player -> {
                    player.closeInventory();
                    previousMenu.open(player);
                })
                .name(ComponentUtils.colored("← Back", NamedTextColor.GRAY))
                .build();
    }

    public Button createNextPageButton(PaginatedMenu menu) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(player -> {
                    menu.nextPage();
                    menu.refresh();
                })
                .name(ComponentUtils.colored("Next Page", NamedTextColor.GREEN))
                .build();
    }

    public Button createPreviousPageButton(PaginatedMenu menu) {
        return SimpleButton.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(player -> {
                    menu.previousPage();
                    menu.refresh();
                })
                .name(ComponentUtils.colored("Previous Page", NamedTextColor.GREEN))
                .build();
    }

    public ToggleButton createToggleButton(ItemStack defaultItem, ItemStack toggledItem, boolean toggled, String nameDefault, String nameToggled, Runnable toggleAction) {
        return ToggleButton.builder()
                .itemStack(defaultItem)
                .toggledItem(toggledItem)
                .toggled(toggled)
                .clickable(true)
                .name(ComponentUtils.colored(toggled ? nameToggled : nameDefault, NamedTextColor.BLUE))
                .onToggle(player -> toggleAction.run())
                .build();
    }

    public int[] getSlots(int startRow, int endRow, int startCol, int endCol) {
        List<Integer> slots = new ArrayList<>();
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    public int[] getBorderSlots(int size) {
        List<Integer> slots = new ArrayList<>();
        int rows = size / 9;

        for (int i = 0; i < 9; i++) {
            slots.add(i);
        }
        for (int i = (rows - 1) * 9; i < size; i++) {
            slots.add(i);
        }
        for (int i = 1; i < rows - 1; i++) {
            slots.add(i * 9);
            slots.add(i * 9 + 8);
        }

        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    public void playClickSound(Player player) {
        player.playSound(player.getLocation(), "ui.button.click", 0.5f, 1.0f);
    }
}