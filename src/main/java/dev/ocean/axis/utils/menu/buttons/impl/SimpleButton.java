package dev.ocean.axis.utils.menu.buttons.impl;

import dev.ocean.axis.utils.menu.buttons.AbstractButton;
import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleButton extends AbstractButton {

    @Builder
    public SimpleButton(ItemStack itemStack,
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
                        int amount) {
        super(itemStack, clickable, leftClick, rightClick, shiftLeftClick, shiftRightClick, middleClick, doubleClick, dropClick, customActions, name, lore, amount);
    }

    public SimpleButton(ItemStack itemStack) {
        super(itemStack, true, null, null, null, null, null, null, null, null, null, null, 1);
    }

    public SimpleButton(Material material) {
        this(new ItemStack(material));
    }
}