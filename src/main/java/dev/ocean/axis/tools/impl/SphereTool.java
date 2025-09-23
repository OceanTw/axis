package dev.ocean.axis.tools.impl;

import dev.ocean.axis.history.HistoryService;
import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.utils.PlayerUtils;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SphereTool extends Tool
{
    public SphereTool() {
        super("sphere", "Sphere Tool", "Creates a ball for you!", Material.HEART_OF_THE_SEA);
    }

    @Override
    public boolean onLeftClick(@NonNull Player player, Location location, ToolSettings settings) {
        HistoryService.get().undo(player);
        PlayerUtils.sendInfo(player, "Undid 1 action");
        return true;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, Location location, ToolSettings settings) {

        return true;
    }

    @Override
    public boolean canUse(@NonNull Player player) {
        return false;
    }

    @Override
    public ToolSettings createDefaultSettings() {
        return null;
    }
}
