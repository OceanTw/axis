package dev.ocean.axis.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@UtilityClass
public class ComponentUtils {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private static final char[] NORMAL = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] SMALL =  "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡxʏᴢ".toCharArray();

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

    private static String toSmallText(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            char lower = Character.toLowerCase(c);
            int index = -1;
            for (int i = 0; i < NORMAL.length; i++) {
                if (NORMAL[i] == lower) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                sb.append(SMALL[index]);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Component smallText(String text) {
        return Component.text(toSmallText(text));
    }
}