package dev.ocean.arc.utils.menu;

import dev.ocean.arc.utils.menu.buttons.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.PlayerInventory;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu)) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() instanceof PlayerInventory) return;

        Menu menu = (Menu) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        if (menu.isCancelAllClicks()) {
            event.setCancelled(true);
        }

        Button button = menu.getButton(event.getSlot());
        if (button != null) {
            button.onClick(player, event.getClick());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu)) return;

        Menu menu = (Menu) event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();

        if (menu instanceof AbstractMenu) {
            ((AbstractMenu) menu).getViewers().remove(player);
        }
    }
}
