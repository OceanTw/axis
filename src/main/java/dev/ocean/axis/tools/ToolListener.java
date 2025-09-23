package dev.ocean.axis.tools;

import dev.ocean.axis.tools.menus.ToolSettingsMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.Objects;

public class ToolListener implements Listener {
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) return;

        ToolService.get().getToolFromItem(item).ifPresent(tool -> {
            ToolSettings settings = tool.getItemSettings(item); // Get settings from item
            Action action = event.getAction();

            event.setCancelled(true);

            switch (action) {
                case LEFT_CLICK_BLOCK, LEFT_CLICK_AIR -> {
                    if (tool.canUse(player)) {
                        if (event.getClickedBlock() == null) {
                            tool.onLeftClick(player, null, settings);
                            break;
                        }
                        tool.onLeftClick(player, event.getClickedBlock().getLocation(), settings);
                    }
                }
                case RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR -> {
                    if (tool.canUse(player)) {
                        if (event.getClickedBlock() == null) {
                            tool.onRightClick(player, player.getLocation(), settings);
                            break;
                        }
                        tool.onRightClick(player, event.getClickedBlock().getLocation(), settings);
                    }
                }
                default -> {}
            }
        });
    }

    @EventHandler
    public void onPlayerSwapItemToOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();

        if (mainHandItem == null || mainHandItem.getType().isAir()) return;

        ToolService.get().getToolFromItem(mainHandItem).ifPresent(tool -> {
            event.setCancelled(true);
            new ToolSettingsMenu(player, tool, mainHandItem).open(player);
        });
    }
}
