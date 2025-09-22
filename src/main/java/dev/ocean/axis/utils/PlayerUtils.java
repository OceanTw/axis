package dev.ocean.axis.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Utility;
import org.bukkit.entity.Player;

@UtilityClass
public class PlayerUtils {

    public void sendError(Player player, String message) {
        player.sendMessage(
                Component.text("ERROR ").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                        .append(Component.text(message).color(NamedTextColor.WHITE))
        );
    }

    public void sendWarning(Player player, String message) {
        player.sendMessage(
                Component.text("WARNING ").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                        .append(Component.text(message).color(NamedTextColor.WHITE))
        );
    }

    public void sendInfo(Player player, String message) {
        player.sendMessage(
                Component.text("INFO ").color(NamedTextColor.BLUE)
                        .append(Component.text(message).color(NamedTextColor.WHITE))
        );
    }

}
