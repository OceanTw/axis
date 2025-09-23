package dev.ocean.axis.tools.impl;

import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
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
        if (player.isSneaking() && settings.get("radius", 3) > 1) {
            settings.set("radius", settings.get("radius", 3) - 1);
        } else {
            settings.set("radius", settings.get("radius", 3) + 1);
        }

        return true;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, Location location, ToolSettings settings) {
        return false;
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
