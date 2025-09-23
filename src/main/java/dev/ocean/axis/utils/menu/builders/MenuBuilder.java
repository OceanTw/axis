package dev.ocean.axis.utils.menu.builders;

import dev.ocean.axis.utils.menu.Menu;
import dev.ocean.axis.utils.menu.buttons.Button;
import dev.ocean.axis.utils.menu.impl.SimpleMenu;
import lombok.Builder;
import lombok.Singular;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Builder
public class MenuBuilder {
    private String title;
    private int size;
    @Builder.Default
    private boolean cancelAllClicks = true;
    private Consumer<Player> onOpen;
    private Consumer<Player> onClose;
    @Singular
    private Map<Integer, Button> buttons;

    public MenuBuilder fill(Button button) {
        for (int i = 0; i < size; i++) {
            if (!buttons.containsKey(i)) {
                buttons.put(i, button);
            }
        }
        return this;
    }

    public MenuBuilder border(Button button) {
        int rows = size / 9;
        for (int i = 0; i < 9; i++) {
            buttons.put(i, button);
        }
        for (int i = (rows - 1) * 9; i < size; i++) {
            buttons.put(i, button);
        }
        for (int i = 1; i < rows - 1; i++) {
            buttons.put(i * 9, button);
            buttons.put(i * 9 + 8, button);
        }
        return this;
    }

    public Menu build() {
        SimpleMenu menu = new SimpleMenu(title, size);
        menu.setCancelAllClicks(cancelAllClicks);
        menu.setOnOpen(onOpen);
        menu.setOnClose(onClose);

        buttons.forEach(menu::setButton);

        return menu;
    }
}
