package dev.ocean.axis.utils.menu;

import dev.ocean.axis.utils.menu.buttons.Button;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.Set;
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