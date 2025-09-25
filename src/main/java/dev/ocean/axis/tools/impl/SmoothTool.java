package dev.ocean.axis.tools.impl;

import dev.lrxh.blockChanger.BlockChanger;
import dev.lrxh.blockChanger.snapshot.CuboidSnapshot;
import dev.ocean.axis.history.HistoryService;
import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.utils.PlayerUtils;
import dev.ocean.axis.utils.WorldUtils;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SmoothTool extends Tool {

    public SmoothTool() {
        super("smooth", "Smooth Tool", "Smooth out terrain in a selected area!", Material.FEATHER);
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
        int radius = settings.get("radius", 5);
        int smoothFactor = Math.max(1, settings.get("smoothFactor", 1));

        center = PlayerUtils.raycast(player, 0, true).getLocation();

        Location min = center.clone().add(-radius, -radius, -radius);
        Location max = center.clone().add(radius, radius, radius);

        Location finalCenter = center;
        WorldUtils.getBlocksAsync(min, max).thenAccept(regionBlocks -> {
            // Create a complete height map of the terrain (not just top layer)
            Map<String, List<Integer>> columnHeights = new HashMap<>();
            Map<String, List<Material>> columnMaterials = new HashMap<>();

            // Organize blocks by column (x,z) and store all solid blocks from bottom to top
            for (Location loc : regionBlocks.keySet()) {
                BlockData data = regionBlocks.get(loc);
                if (data != null && !data.getMaterial().isAir() && isSolidBlock(data.getMaterial())) {
                    String key = loc.getBlockX() + "," + loc.getBlockZ();

                    if (!columnHeights.containsKey(key)) {
                        columnHeights.put(key, new ArrayList<>());
                        columnMaterials.put(key, new ArrayList<>());
                    }

                    // Insert in sorted order (lowest Y first)
                    int y = loc.getBlockY();
                    List<Integer> heights = columnHeights.get(key);
                    List<Material> materials = columnMaterials.get(key);

                    int insertIndex = 0;
                    for (; insertIndex < heights.size(); insertIndex++) {
                        if (y < heights.get(insertIndex)) {
                            break;
                        }
                    }
                    heights.add(insertIndex, y);
                    materials.add(insertIndex, data.getMaterial());
                }
            }

            if (columnHeights.isEmpty()) {
                PlayerUtils.sendWarning(player, "No solid blocks found to smooth in the selected area!");
                PlayerUtils.playSoundWarning(player);
                return;
            }

            // Get the top height map for smoothing calculations
            Map<String, Integer> topHeights = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : columnHeights.entrySet()) {
                List<Integer> heights = entry.getValue();
                if (!heights.isEmpty()) {
                    topHeights.put(entry.getKey(), heights.get(heights.size() - 1)); // Highest Y
                }
            }

            // Apply smoothing to the top height map
            Map<String, Integer> smoothedTopHeights = smoothHeightMap(topHeights, smoothFactor);

            // Calculate changes needed for the entire terrain volume
            Map<Location, BlockData> smoothedBlocks = new HashMap<>();
            int changesMade = 0;

            for (Map.Entry<String, Integer> entry : topHeights.entrySet()) {
                String key = entry.getKey();
                int originalTopY = entry.getValue();
                int smoothedTopY = smoothedTopHeights.get(key);

                if (originalTopY != smoothedTopY) {
                    String[] coords = key.split(",");
                    int x = Integer.parseInt(coords[0]);
                    int z = Integer.parseInt(coords[1]);

                    List<Integer> originalHeights = columnHeights.get(key);
                    List<Material> originalMaterials = columnMaterials.get(key);

                    // Case 1: Lowering terrain (smoothedTopY < originalTopY)
                    if (smoothedTopY < originalTopY) {
                        // Remove blocks above the new top level
                        for (int y = originalTopY; y > smoothedTopY; y--) {
                            Location removeLoc = new Location(finalCenter.getWorld(), x, y, z);
                            smoothedBlocks.put(removeLoc, Material.AIR.createBlockData());
                            changesMade++;
                        }
                    }
                    // Case 2: Raising terrain (smoothedTopY > originalTopY)
                    else {
                        // Find the most appropriate material to use for new blocks
                        Material newMaterial = findBestMaterialForColumn(originalMaterials, originalHeights);

                        // Add blocks up to the new top level
                        for (int y = originalTopY + 1; y <= smoothedTopY; y++) {
                            Location addLoc = new Location(finalCenter.getWorld(), x, y, z);
                            smoothedBlocks.put(addLoc, newMaterial.createBlockData());
                            changesMade++;
                        }
                    }

                    // Ensure the terrain below maintains its integrity
                    // If we removed blocks, make sure we're not leaving floating blocks
                    ensureTerrainIntegrity(columnHeights, smoothedTopHeights, smoothedBlocks,
                            finalCenter, changesMade);
                }
            }

            if (changesMade == 0) {
                PlayerUtils.sendWarning(player, "No smoothing was needed - terrain is already smooth!");
                PlayerUtils.playSoundWarning(player);
                return;
            }

            CuboidSnapshot.create(min, max).thenAccept(snapshot ->
                    HistoryService.get().add(player, snapshot));

            long startTime = System.currentTimeMillis();
            PlayerUtils.sendInfo(player, "Smoothing " + changesMade + " blocks...");

            int finalChangesMade = changesMade;
            BlockChanger.setBlocks(smoothedBlocks, true).thenAccept(success -> {
                long duration = System.currentTimeMillis() - startTime;
                PlayerUtils.sendMessage(player,
                        Component.text("§a§lSUCCESS! §rSmoothed §d" + finalChangesMade +
                                "§r blocks in §e" + duration + "ms"));
                PlayerUtils.playSoundSuccess(player);
            });
        });

        return true;
    }

    private void ensureTerrainIntegrity(Map<String, List<Integer>> columnHeights,
                                        Map<String, Integer> smoothedTopHeights,
                                        Map<Location, BlockData> smoothedBlocks,
                                        Location center, int changesMade) {
        // Check neighboring columns to ensure we're not creating overhangs or floating blocks
        for (Map.Entry<String, List<Integer>> entry : columnHeights.entrySet()) {
            String key = entry.getKey();
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);

            int currentTopY = smoothedTopHeights.get(key);

            // Check if this column is significantly higher than its neighbors
            // which would create floating blocks
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    String neighborKey = (x + dx) + "," + (z + dz);
                    Integer neighborTopY = smoothedTopHeights.get(neighborKey);

                    if (neighborTopY != null && currentTopY > neighborTopY + 2) {
                        // This column is too high compared to neighbor, lower it gradually
                        int newTopY = Math.max(neighborTopY + 1, currentTopY - 1);
                        if (newTopY != currentTopY) {
                            smoothedTopHeights.put(key, newTopY);

                            // Remove excess blocks
                            for (int y = currentTopY; y > newTopY; y--) {
                                Location removeLoc = new Location(center.getWorld(), x, y, z);
                                smoothedBlocks.put(removeLoc, Material.AIR.createBlockData());
                            }
                        }
                    }
                }
            }
        }
    }

    private Material findBestMaterialForColumn(List<Material> materials, List<Integer> heights) {
        if (materials.isEmpty()) return Material.GRASS_BLOCK;

        // Prefer the top material if it's suitable
        Material topMaterial = materials.get(materials.size() - 1);
        if (isGoodBuildingMaterial(topMaterial)) {
            return topMaterial;
        }

        // Otherwise find the most common suitable material in the column
        Map<Material, Integer> materialCount = new HashMap<>();
        for (Material material : materials) {
            if (isGoodBuildingMaterial(material)) {
                materialCount.put(material, materialCount.getOrDefault(material, 0) + 1);
            }
        }

        if (!materialCount.isEmpty()) {
            return materialCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get().getKey();
        }

        // Default to grass block if no suitable materials found
        return Material.GRASS_BLOCK;
    }

    private boolean isGoodBuildingMaterial(Material material) {
        return material.isSolid() &&
                material != Material.WATER &&
                material != Material.LAVA &&
                material != Material.AIR &&
                material != Material.SAND && // Avoid sandy materials for building up
                material != Material.GRAVEL &&
                material != Material.DIRT; // Prefer stone-like materials
    }

    private Map<String, Integer> smoothHeightMap(Map<String, Integer> heightMap, int smoothFactor) {
        Map<String, Integer> smoothed = new HashMap<>();

        for (Map.Entry<String, Integer> entry : heightMap.entrySet()) {
            String key = entry.getKey();
            int currentHeight = entry.getValue();
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);

            // Calculate weighted average height of neighboring blocks
            int sum = 0;
            int totalWeight = 0;

            // Use a 3x3 area with distance-based weighting
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    String neighborKey = (x + dx) + "," + (z + dz);
                    Integer neighborHeight = heightMap.get(neighborKey);

                    if (neighborHeight != null) {
                        int weight = (dx == 0 && dz == 0) ? 2 : 1; // Center has more weight
                        sum += neighborHeight * weight;
                        totalWeight += weight;
                    }
                }
            }

            if (totalWeight > 0) {
                int averageHeight = Math.round(sum / (float) totalWeight);
                // Apply smooth factor limitation
                int difference = averageHeight - currentHeight;
                int maxChange = Math.min(Math.abs(difference), smoothFactor);

                if (difference > 0) {
                    smoothed.put(key, currentHeight + maxChange);
                } else if (difference < 0) {
                    smoothed.put(key, currentHeight - maxChange);
                } else {
                    smoothed.put(key, currentHeight);
                }
            } else {
                smoothed.put(key, currentHeight);
            }
        }

        return smoothed;
    }

    private boolean isSolidBlock(Material material) {
        return material.isSolid() &&
                material != Material.WATER &&
                material != Material.LAVA &&
                material != Material.AIR;
    }

    @Override
    public boolean canUse(@NonNull Player player) {
        return player.hasPermission("axis.tools.smooth") || player.isOp();
    }

    @Override
    public ToolSettings createDefaultSettings() {
        ToolSettings settings = new ToolSettings();
        settings.set("radius", 5);
        settings.set("smoothFactor", 1);
        return settings;
    }

    @Override
    public Set<String> getConfigurableSettings() {
        return Set.of("radius", "smoothFactor");
    }

    @Override
    protected String getLeftClickDescription() {
        return "Undo last action";
    }

    @Override
    protected String getRightClickDescription() {
        return "Smooth terrain in selected area";
    }
}