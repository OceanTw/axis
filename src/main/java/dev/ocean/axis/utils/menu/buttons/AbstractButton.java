package dev.ocean.axis.utils.menu.buttons;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@Setter
public abstract class AbstractButton implements Button {
    protected ItemStack itemStack;
    protected boolean clickable;
    protected Consumer<Player> leftClick;
    protected Consumer<Player> rightClick;
    protected Consumer<Player> shiftLeftClick;
    protected Consumer<Player> shiftRightClick;
    protected Consumer<Player> middleClick;
    protected Consumer<Player> doubleClick;
    protected Consumer<Player> dropClick;
    protected Map<ClickType, Consumer<Player>> customActions;
    protected Component name;
    protected List<Component> lore;
    protected int amount;

    public AbstractButton(ItemStack itemStack, boolean clickable,
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
        this.itemStack = itemStack.clone();
        this.clickable = clickable;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftLeftClick = shiftLeftClick;
        this.shiftRightClick = shiftRightClick;
        this.middleClick = middleClick;
        this.doubleClick = doubleClick;
        this.dropClick = dropClick;
        this.customActions = customActions != null ? customActions : new HashMap<>();
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        applyMeta();
    }

    protected void applyMeta() {
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                if (name != null) meta.displayName(name);
                if (lore != null) meta.lore(lore);
                itemStack.setItemMeta(meta);
            }
        }
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    @Override
    public boolean isClickable() {
        return clickable;
    }

    @Override
    public void onClick(Player player, ClickType clickType) {
        if (!clickable) return;

        Consumer<Player> action = customActions.get(clickType);
        if (action != null) {
            action.accept(player);
            return;
        }

        switch (clickType) {
            case LEFT:
                if (leftClick != null) leftClick.accept(player);
                break;
            case RIGHT:
                if (rightClick != null) rightClick.accept(player);
                break;
            case SHIFT_LEFT:
                if (shiftLeftClick != null) shiftLeftClick.accept(player);
                break;
            case SHIFT_RIGHT:
                if (shiftRightClick != null) shiftRightClick.accept(player);
                break;
            case MIDDLE:
                if (middleClick != null) middleClick.accept(player);
                break;
            case DOUBLE_CLICK:
                if (doubleClick != null) doubleClick.accept(player);
                break;
            case DROP:
            case CONTROL_DROP:
                if (dropClick != null) dropClick.accept(player);
                break;
        }
    }
}
