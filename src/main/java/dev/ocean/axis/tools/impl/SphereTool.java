package dev.ocean.axis.tools.impl;

import dev.ocean.axis.history.HistoryService;
import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.tools.patterns.SpherePattern;
import dev.ocean.axis.utils.PlayerUtils;
import dev.ocean.axis.utils.world.AxisWorldEditor;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SphereTool extends Tool {
    public SphereTool() {
        super("sphere", "Sphere Tool", "Creates a ball for you!", Material.HEART_OF_THE_SEA);
    }

    private final AxisWorldEditor worldEditor = AxisWorldEditor.get();

    @Override
    public boolean onLeftClick(@NonNull Player player, Location location, ToolSettings settings) {
        if (HistoryService.get().getHistory(player).isEmpty()) {
            PlayerUtils.sendActionBar(player, "No actions to undo!");
            PlayerUtils.playSoundError(player);
            return true;
        }

        HistoryService.get().undo(player).restore(true);
        PlayerUtils.sendActionBar(player, "Undid 1 action");
        PlayerUtils.playSoundInfo(player);
        return true;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, Location center, ToolSettings settings) {
        Map<Material, Double> blockSettings = settings.getMaterialPercentages("blocks");
        if (blockSettings.isEmpty()) {
            PlayerUtils.sendError(player, "No sphere materials configured!");
            PlayerUtils.playSoundError(player);
            return false;
        }

        boolean replaceAirOnly = settings.get("replace_air_only", false);
        boolean hollow = settings.get("hollow", false);
        int radius = settings.get("radius", 5);

        if (settings.get("brush_mode", false)) {
            center = PlayerUtils.raycast(player, 160, true).getLocation();
        } else {
            center = PlayerUtils.raycast(player, settings.get("distance", 8), false).getLocation();
        }

        SpherePattern pattern = new SpherePattern(blockSettings, replaceAirOnly, hollow, radius, center);

        Location pos1 = center.clone().add(-radius, -radius, -radius);
        Location pos2 = center.clone().add(radius, radius, radius);

        long startTime = System.currentTimeMillis();
        PlayerUtils.sendInfo(player, "Generating sphere with material percentages...");

        worldEditor.fill(pos1, pos2, pattern).thenAccept(blocksPlaced -> {
            long duration = System.currentTimeMillis() - startTime;
            PlayerUtils.sendMessage(player,
                    Component.text("§a§lSUCCESS! §rPlaced §d" + blocksPlaced + "§r blocks in §e" + duration + "ms"));
            PlayerUtils.playSoundSuccess(player);

            worldEditor.save(player.getWorld()).thenRun(() -> {
                PlayerUtils.sendActionBar(player, "Sphere saved to world!");
            });
        });

        return true;
    }

    @Override
    public boolean canUse(@NonNull Player player) {
        return player.hasPermission("axis.tools.sphere") || player.isOp();
    }

    @Override
    public ToolSettings createDefaultSettings() {
        ToolSettings settings = new ToolSettings();
        settings.set("radius", 5);
        settings.set("distance", 8);
        settings.set("hollow", false);
        settings.set("replace_air_only", false);
        settings.set("brush_mode", true);

        Map<Material, Double> defaultBlocks = new HashMap<>();
        defaultBlocks.put(Material.STONE, 100.0);
        settings.setMaterialPercentages("blocks", defaultBlocks);

        return settings;
    }

    @Override
    public Set<String> getConfigurableSettings() {
        return Set.of("blocks", "replace_air_only", "hollow", "radius", "distance", "brush_mode");
    }

    @Override
    protected String getLeftClickDescription() {
        return "Undo last action";
    }

    @Override
    protected String getRightClickDescription() {
        return "Create sphere with configured settings";
    }
}