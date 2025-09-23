package dev.ocean.axis.utils.menu;

import dev.ocean.axis.utils.menu.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

public interface Menu extends InventoryHolder {
    void open(Player player);
    void close(Player player);
    void refresh();
    void setButton(int slot, Button button);
    void removeButton(int slot);
    Button getButton(int slot);
    String getTitle();
    int getSize();
    void setCancelAllClicks(boolean cancel);
    boolean isCancelAllClicks();
    void setOnOpen(Consumer<Player> onOpen);
    void setOnClose(Consumer<Player> onClose);
    Set<Player> getViewers();
}