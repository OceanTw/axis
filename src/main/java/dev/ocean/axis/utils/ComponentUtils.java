package dev.ocean.axis.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@UtilityClass
public class ComponentUtils {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public Component convertLegacy(String input) {
        Component legacyComponent = LEGACY.deserialize(input);
        String miniMsg = MINI.serialize(legacyComponent);
        return MINI.deserialize(miniMsg).decoration(TextDecoration.ITALIC, false);
    }

    public Component colored(String text, NamedTextColor color) {
        return Component.text(text).color(color).decoration(TextDecoration.ITALIC, false);
    }

    public Component colored(String text, NamedTextColor color, boolean italic) {
        return Component.text(text).color(color).decoration(TextDecoration.ITALIC, italic);
    }

    public Component plain(String text) {
        return Component.text(text).decoration(TextDecoration.ITALIC, false);
    }

    public Component mini(String mini) {
        return MINI.deserialize(mini).decoration(TextDecoration.ITALIC, false);
    }

    public Component legacy(String legacy) {
        return LEGACY.deserialize(legacy).decoration(TextDecoration.ITALIC, false);
    }
}