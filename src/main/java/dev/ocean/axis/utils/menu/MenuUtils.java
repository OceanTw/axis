package dev.ocean.axis.utils.menu;

import dev.ocean.axis.utils.menu.builders.ButtonBuilder;
import dev.ocean.axis.utils.menu.buttons.Button;
import dev.ocean.axis.utils.menu.impl.PaginatedMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MenuUtils {
    public static Button createFillerButton(Material material) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(material))
                .clickable(false)
                .build()
                .name(" ")
                .build();
    }

    public static Button createCloseButton() {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.BARRIER))
                .leftClick(Player::closeInventory)
                .build()
                .name("§cClose")
                .build();
    }

    public static Button createBackButton(Menu previousMenu) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(player -> {
                    player.closeInventory();
                    previousMenu.open(player);
                })
                .build()
                .name("§7← Back")
                .build();
    }

    public static Button createNextPageButton(PaginatedMenu menu) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(player -> {
                    menu.nextPage();
                    menu.refresh();
                })
                .build()
                .name("§aNext Page")
                .build();
    }

    public static Button createPreviousPageButton(PaginatedMenu menu) {
        return ButtonBuilder.builder()
                .itemStack(new ItemStack(Material.ARROW))
                .leftClick(player -> {
                    menu.previousPage();
                    menu.refresh();
                })
                .build()
                .name("§aPrevious Page")
                .build();
    }

    public static int[] getSlots(int startRow, int endRow, int startCol, int endCol) {
        List<Integer> slots = new ArrayList<>();
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    public static int[] getBorderSlots(int size) {
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

    public static void playClickSound(Player player) {
        player.playSound(player.getLocation(), "ui.button.click", 0.5f, 1.0f);
    }
}