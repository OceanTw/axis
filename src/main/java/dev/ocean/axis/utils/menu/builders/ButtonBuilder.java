package dev.ocean.axis.utils.menu.builders;

import dev.ocean.axis.utils.menu.buttons.Button;
import dev.ocean.axis.utils.menu.buttons.impl.SimpleButton;
import lombok.Builder;
import lombok.Singular;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Builder
public class ButtonBuilder {
    private ItemStack itemStack;
    @Builder.Default
    private boolean clickable = true;
    private Consumer<Player> leftClick;
    private Consumer<Player> rightClick;
    private Consumer<Player> shiftLeftClick;
    private Consumer<Player> shiftRightClick;
    private Consumer<Player> middleClick;
    private Consumer<Player> doubleClick;
    private Consumer<Player> dropClick;
    @Singular
    private Map<ClickType, Consumer<Player>> customActions;

    public ButtonBuilder material(Material material) {
        this.itemStack = new ItemStack(material);
        return this;
    }

    public ButtonBuilder name(String name) {
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                itemStack.setItemMeta(meta);
            }
        }
        return this;
    }

    public ButtonBuilder lore(List<String> lore) {
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
            }
        }
        return this;
    }

    public ButtonBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ButtonBuilder amount(int amount) {
        if (itemStack != null) {
            itemStack.setAmount(amount);
        }
        return this;
    }

    public Button build() {
        SimpleButton button = new SimpleButton(itemStack);
        button.setClickable(clickable);
        button.setLeftClick(leftClick);
        button.setRightClick(rightClick);
        button.setShiftLeftClick(shiftLeftClick);
        button.setShiftRightClick(shiftRightClick);
        button.setMiddleClick(middleClick);
        button.setDoubleClick(doubleClick);
        button.setDropClick(dropClick);

        customActions.forEach(button::setCustomAction);

        return button;
    }
}