package dev.ocean.axis.utils.menu;

import dev.ocean.axis.AxisPlugin;
import dev.ocean.axis.utils.menu.buttons.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

public class MenuListener implements Listener {
    private final Plugin plugin;

    public MenuListener() {
        this.plugin = AxisPlugin.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu)) return;

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
