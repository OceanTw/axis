package dev.ocean.axis.utils.menu;

import dev.ocean.axis.utils.menu.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractMenu implements Menu {
    protected final String title;
    protected final int size;
    protected final Map<Integer, Button> buttons;
    protected final Set<Player> viewers;
    protected Inventory inventory;
    protected boolean cancelAllClicks;
    protected Consumer<Player> onOpen;
    protected Consumer<Player> onClose;

    public AbstractMenu(String title, int size) {
        this.title = title;
        this.size = size;
        this.buttons = new HashMap<>();
        this.viewers = new HashSet<>();
        this.cancelAllClicks = true;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public void open(Player player) {
        viewers.add(player);
        if (onOpen != null) {
            onOpen.accept(player);
        }
        refresh();
        player.openInventory(inventory);
    }

    @Override
    public void close(Player player) {
        viewers.remove(player);
        if (onClose != null) {
            onClose.accept(player);
        }
        player.closeInventory();
    }

    @Override
    public void refresh() {
        inventory.clear();
        buttons.forEach((slot, button) -> {
            if (button != null) {
                inventory.setItem(slot, button.getItemStack());
            }
        });
    }

    @Override
    public void setButton(int slot, Button button) {
        if (slot < 0 || slot >= size) return;
        buttons.put(slot, button);
        if (button != null) {
            inventory.setItem(slot, button.getItemStack());
        } else {
            inventory.setItem(slot, null);
        }
    }

    @Override
    public void removeButton(int slot) {
        buttons.remove(slot);
        inventory.setItem(slot, null);
    }

    @Override
    public Button getButton(int slot) {
        return buttons.get(slot);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void setCancelAllClicks(boolean cancel) {
        this.cancelAllClicks = cancel;
    }

    @Override
    public boolean isCancelAllClicks() {
        return cancelAllClicks;
    }

    @Override
    public void setOnOpen(Consumer<Player> onOpen) {
        this.onOpen = onOpen;
    }

    @Override
    public void setOnClose(Consumer<Player> onClose) {
        this.onClose = onClose;
    }

    @Override
    public Set<Player> getViewers() {
        return new HashSet<>(viewers);
    }

    public void fill(Button button) {
        for (int i = 0; i < size; i++) {
            if (!buttons.containsKey(i)) {
                setButton(i, button);
            }
        }
    }

    public void fillBorder(Button button) {
        int rows = size / 9;
        for (int i = 0; i < 9; i++) {
            if (getButton(i) != null) continue;
            setButton(i, button);
        }
        for (int i = (rows - 1) * 9; i < size; i++) {
            if (getButton(i) != null) continue;
            setButton(i, button);
        }
    }

    public void setRow(int row, Button button) {
        if (row < 0 || row >= size / 9) return;
        for (int i = 0; i < 9; i++) {
            setButton(row * 9 + i, button);
        }
    }

    public void setColumn(int column, Button button) {
        if (column < 0 || column >= 9) return;
        for (int i = 0; i < size / 9; i++) {
            setButton(i * 9 + column, button);
        }
    }
}