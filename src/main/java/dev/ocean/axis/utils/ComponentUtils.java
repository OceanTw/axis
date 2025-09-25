package dev.ocean.axis.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

    public TextComponent convertLegacy(String input) {
        Component legacyComponent = LEGACY.deserialize(input);
        String miniMsg = MINI.serialize(legacyComponent);
        return (TextComponent) MINI.deserialize(miniMsg).decoration(TextDecoration.ITALIC, false);
    }

    public TextComponent colored(String text, NamedTextColor color) {
        return Component.text(text).color(color).decoration(TextDecoration.ITALIC, false);
    }

    public TextComponent colored(String text, NamedTextColor color, boolean italic) {
        return Component.text(text).color(color).decoration(TextDecoration.ITALIC, italic);
    }

    public TextComponent plain(String text) {
        return Component.text(text).decoration(TextDecoration.ITALIC, false);
    }

    public Component mini(String mini) {
        return MINI.deserialize(mini).decoration(TextDecoration.ITALIC, false);
    }

    public Component legacy(String legacy) {
        return LEGACY.deserialize(legacy).decoration(TextDecoration.ITALIC, false);
    }

    public static String toSmallText(String input) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '&' && i + 1 < input.length()) {
                sb.append(c);
                sb.append(input.charAt(i + 1));
                i += 2;
            } else {
                char lower = Character.toLowerCase(c);
                int index = -1;
                for (int j = 0; j < NORMAL.length; j++) {
                    if (NORMAL[j] == lower) {
                        index = j;
                        break;
                    }
                }
                if (index != -1) {
                    sb.append(SMALL[index]);
                } else {
                    sb.append(c);
                }
                i++;
            }
        }
        return sb.toString();
    }

    public static TextComponent smallText(String text) {
        String smallLegacy = toSmallText(text);
        return LEGACY.deserialize(smallLegacy).decoration(TextDecoration.ITALIC, false);
    }
}