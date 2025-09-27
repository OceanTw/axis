package dev.ocean.arc.tools.impl;

import dev.ocean.arc.region.SelectionService;
import dev.ocean.arc.tools.Tool;
import dev.ocean.arc.tools.ToolSettings;
import dev.ocean.arc.utils.PlayerUtils;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Set;

public class SelectionTool extends Tool {
    public SelectionTool() {
        super("selection", "Selection Tool", "Selects a region in the world!", Material.LEAD);
    }

    SelectionService selection = SelectionService.get();

    @Override
    public boolean onLeftClick(@NonNull Player player, Location location, ToolSettings settings) {
        if (location == null) {
            selection.clear(player.getUniqueId());
            PlayerUtils.sendActionBar(player, "Selection cleared");
            return true;
        };
        selection.setPos1(player.getUniqueId(), location);
        PlayerUtils.sendActionBar(player, "Position &b1 &rset!");
        return true;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, Location location, ToolSettings settings) {
        if (location == null) {
            return true;
        }
        selection.setPos2(player.getUniqueId(), location);
        PlayerUtils.sendActionBar(player, "Position &b2 &rset!");
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

    @Override
    public Set<String> getConfigurableSettings() {
        return Set.of();
    }
}
