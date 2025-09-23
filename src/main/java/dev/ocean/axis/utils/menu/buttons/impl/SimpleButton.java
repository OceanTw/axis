package dev.ocean.axis.utils.menu.buttons.impl;

import dev.ocean.axis.utils.menu.buttons.AbstractButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SimpleButton extends AbstractButton {
    public SimpleButton(ItemStack itemStack) {
        super(itemStack);
    }

    public SimpleButton(Material material) {
        super(new ItemStack(material));
    }

    public SimpleButton(Material material, String displayName) {
        super(new ItemStack(material));
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            itemStack.setItemMeta(meta);
        }
    }

    public SimpleButton(Material material, String displayName, List<String> lore) {
        super(new ItemStack(material));
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
    }
}
