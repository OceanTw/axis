package dev.ocean.arc.tools.impl;

import dev.ocean.api.tools.ArcToolSettings;
import dev.ocean.arc.tools.Tool;
import dev.ocean.arc.tools.ToolSettings;
import dev.ocean.arc.tools.patterns.SpherePattern;
import dev.ocean.arc.utils.PlayerUtils;
import dev.ocean.arc.utils.world.ArcWorldEditor;
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

    private final ArcWorldEditor worldEditor = ArcWorldEditor.get();

    @Override
    public boolean onLeftClick(@NonNull Player player, Location location, ArcToolSettings settings) {
        worldEditor.undo(player).thenRun(() -> PlayerUtils.sendActionBar(player, "Undo complete!"));
        PlayerUtils.playSoundInfo(player);
        return true;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, Location center, ArcToolSettings settings) {
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

        worldEditor.fill(pos1, pos2, pattern, player).thenAccept(blocksPlaced -> {
            long duration = System.currentTimeMillis() - startTime;
            PlayerUtils.sendMessage(player,
                    Component.text("§a§lSUCCESS! §rPlaced §d" + blocksPlaced + "§r blocks in §e" + duration + "ms"));
            PlayerUtils.playSoundSuccess(player);
        });

        return true;
    }

    @Override
    public boolean canUse(@NonNull Player player) {
        return player.hasPermission("arc.tools.sphere") || player.isOp();
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