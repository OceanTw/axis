package dev.ocean.axis.utils.menu.buttons;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface Button {
    ItemStack getItemStack();
    void onClick(Player player, ClickType clickType);
    boolean isClickable();
}