package dev.ocean.axis.tools.impl;

import dev.lrxh.blockChanger.BlockChanger;
import dev.lrxh.blockChanger.snapshot.CuboidSnapshot;
import dev.ocean.axis.history.HistoryService;
import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.utils.PlayerUtils;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SphereTool extends Tool {
    public SphereTool() {
        super("sphere", "Sphere Tool", "Creates a ball for you!", Material.HEART_OF_THE_SEA);
    }

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

        Map<Location, BlockData> blocks = new HashMap<>();
        int radiusSquared = radius * radius;
        int innerRadiusSquared = (radius - 1) * (radius - 1);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distanceSquared = x * x + y * y + z * z;

                    if (distanceSquared > radiusSquared) continue;
                    if (hollow && distanceSquared < innerRadiusSquared) continue;

                    Location loc = center.clone().add(x, y, z);

                    if (replaceAirOnly && !loc.getBlock().getType().isAir()) {
                        continue;
                    }

                    Material selected = selectMaterialByPercentage(blockSettings);
                    if (selected != null) {
                        blocks.put(loc, selected.createBlockData());
                    }
                }
            }
        }

        if (blocks.isEmpty()) {
            PlayerUtils.sendWarning(player, "No blocks were changed. Check your settings!");
            PlayerUtils.playSoundWarning(player);
            return false;
        }

        // save history for undo
        CuboidSnapshot.create(center.clone().add(-radius, -radius, -radius),
                        center.clone().add(radius, radius, radius))
                .thenAccept(snapshot -> HistoryService.get().add(player, snapshot));

        long startTime = System.currentTimeMillis();
        PlayerUtils.sendInfo(player, "Generating sphere with " + blocks.size() + " blocks...");

        BlockChanger.setBlocks(blocks, true).thenAccept(success -> {
            long duration = System.currentTimeMillis() - startTime;
            PlayerUtils.sendMessage(player, Component.text("§a§lSUCCESS! §rPlaced §d" + blocks.size() + "§r blocks in §e" + duration + "ms"));
            PlayerUtils.playSoundSuccess(player);
        });

        return true;
    }

    private Material selectMaterialByPercentage(Map<Material, Double> materialPercentages) {
        double total = materialPercentages.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) return null;

        double random = ThreadLocalRandom.current().nextDouble() * 100.0;
        double cumulative = 0.0;

        for (Map.Entry<Material, Double> entry : materialPercentages.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                return entry.getKey();
            }
        }

        // fallback
        return materialPercentages.keySet().iterator().next();
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
