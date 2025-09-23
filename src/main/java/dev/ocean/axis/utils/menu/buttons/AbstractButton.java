package dev.ocean.axis.utils.menu.buttons;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

    public AbstractButton(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.clickable = true;
        this.customActions = new HashMap<>();
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    @Override
    public boolean isClickable() {
        return clickable;
    }

    public AbstractButton setClickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }

    public AbstractButton setLeftClick(Consumer<Player> action) {
        this.leftClick = action;
        return this;
    }

    public AbstractButton setRightClick(Consumer<Player> action) {
        this.rightClick = action;
        return this;
    }

    public AbstractButton setShiftLeftClick(Consumer<Player> action) {
        this.shiftLeftClick = action;
        return this;
    }

    public AbstractButton setShiftRightClick(Consumer<Player> action) {
        this.shiftRightClick = action;
        return this;
    }

    public AbstractButton setMiddleClick(Consumer<Player> action) {
        this.middleClick = action;
        return this;
    }

    public AbstractButton setDoubleClick(Consumer<Player> action) {
        this.doubleClick = action;
        return this;
    }

    public AbstractButton setDropClick(Consumer<Player> action) {
        this.dropClick = action;
        return this;
    }

    public AbstractButton setCustomAction(ClickType clickType, Consumer<Player> action) {
        customActions.put(clickType, action);
        return this;
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