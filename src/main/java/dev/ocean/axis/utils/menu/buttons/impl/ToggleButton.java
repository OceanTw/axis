package dev.ocean.axis.utils.menu.buttons.impl;

import dev.ocean.axis.utils.menu.buttons.AbstractButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ToggleButton extends AbstractButton {
    private boolean toggled;
    private ItemStack toggledItem;
    private Consumer<Player> onToggle;

    public ToggleButton(ItemStack defaultItem, ItemStack toggledItem) {
        super(defaultItem);
        this.toggledItem = toggledItem.clone();
        this.toggled = false;
    }

    public ToggleButton setOnToggle(Consumer<Player> onToggle) {
        this.onToggle = onToggle;
        return this;
    }

    public void toggle() {
        toggled = !toggled;
        itemStack = toggled ? toggledItem.clone() : getItemStack();
    }

    public boolean isToggled() {
        return toggled;
    }

    @Override
    public void onClick(Player player, ClickType clickType) {
        if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
            toggle();
            if (onToggle != null) {
                onToggle.accept(player);
            }
        }
        super.onClick(player, clickType);
    }
}
