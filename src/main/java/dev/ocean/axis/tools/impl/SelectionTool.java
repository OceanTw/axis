package dev.ocean.axis.tools.impl;

import dev.ocean.axis.region.SelectionService;
import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.utils.PlayerUtils;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SelectionTool extends Tool {
    public SelectionTool() {
        super("selection", "Selection Tool", "Selects a region in the world!", Material.LEAD);
    }

    SelectionService selection = SelectionService.get();

    @Override
    public boolean onLeftClick(@NonNull Player player, Location location, ToolSettings settings) {
        if (location == null) {
            selection.clear(player.getUniqueId());
            PlayerUtils.sendInfo(player, "Selection cleared");
            return true;
        };
        selection.setPos1(player.getUniqueId(), location);
        PlayerUtils.sendInfo(player, "Position 1 set!");
        return true;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, Location location, ToolSettings settings) {
        selection.setPos2(player.getUniqueId(), location);
        PlayerUtils.sendInfo(player, "Position 2 set!");
        return true;
    }

    @Override
    public boolean canUse(@NonNull Player player) {
        return true;
    }

    @Override
    public ToolSettings createDefaultSettings() {
        return null;
    }
}
