package dev.ocean.axis.utils.menu.buttons.impl;

import dev.ocean.axis.utils.menu.buttons.AbstractButton;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@Setter
public class ToggleButton extends AbstractButton {
    private boolean toggled;
    private ItemStack toggledItem;
    private Consumer<Player> onToggle;

    @Builder
    public ToggleButton(ItemStack itemStack,
                        ItemStack toggledItem,
                        boolean toggled,
                        boolean clickable,
                        Consumer<Player> leftClick,
                        Consumer<Player> rightClick,
                        Consumer<Player> shiftLeftClick,
                        Consumer<Player> shiftRightClick,
                        Consumer<Player> middleClick,
                        Consumer<Player> doubleClick,
                        Consumer<Player> dropClick,
                        Map<ClickType, Consumer<Player>> customActions,
                        Component name,
                        List<Component> lore,
                        Consumer<Player> onToggle,
                        int amount) {
        super(itemStack, clickable, leftClick, rightClick, shiftLeftClick, shiftRightClick, middleClick, doubleClick, dropClick, customActions, name, lore, amount);
        this.toggledItem = toggledItem != null ? toggledItem.clone() : null;
        this.toggled = toggled;
        this.onToggle = onToggle;
        if (toggled) {
            this.itemStack = this.toggledItem != null ? this.toggledItem.clone() : this.itemStack;
        }
    }

    public void toggle() {
        toggled = !toggled;
        itemStack = toggled ? (toggledItem != null ? toggledItem.clone() : itemStack) : (itemStack != null ? itemStack.clone() : null);
        applyMeta();
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