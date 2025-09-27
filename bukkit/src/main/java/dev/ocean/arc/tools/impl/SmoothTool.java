package dev.ocean.arc.tools.impl;

import dev.ocean.arc.tools.Tool;
import dev.ocean.arc.tools.ToolSettings;
import dev.ocean.arc.utils.PlayerUtils;
import dev.ocean.arc.utils.world.ArcWorldEditor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;

public class SmoothTool extends Tool {

    public SmoothTool() {
        super("smooth", "Smooth Tool", "Smooth out terrain in a selected area!", Material.FEATHER);
    }

    private final ArcWorldEditor worldEditor = ArcWorldEditor.get();

    @Override
    public boolean onLeftClick(@NonNull Player player, Location location, ToolSettings settings) {
        worldEditor.undo(player).thenRun(() -> PlayerUtils.sendActionBar(player, "Undo complete!"));
        PlayerUtils.playSoundInfo(player);
        return true;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, Location center, ToolSettings settings) {
        int radius = settings.get("radius", 5);
        int smoothFactor = Math.max(1, settings.get("smoothFactor", 1));

        center = PlayerUtils.raycast(player, 0, true).getLocation();
        Location min = center.clone().add(-radius, -radius - 10, -radius);
        Location max = center.clone().add(radius, radius + 10, radius);

        Location finalCenter = center;

        worldEditor.getBlocks(min, max).thenAccept(regionBlocks -> {
            Map<String, List<TerrainBlock>> columnData = new HashMap<>();

            for (Location loc : regionBlocks.keySet()) {
                BlockData data = regionBlocks.get(loc);
                if (data != null && !data.getMaterial().isAir() && isSolidBlock(data.getMaterial())) {
                    String key = loc.getBlockX() + "," + loc.getBlockZ();

                    if (!columnData.containsKey(key)) {
                        columnData.put(key, new ArrayList<>());
                    }

                    columnData.get(key).add(new TerrainBlock(loc.getBlockY(), data.getMaterial()));
                }
            }

            if (columnData.isEmpty()) {
                PlayerUtils.sendWarning(player, "No solid blocks found to smooth in the selected area!");
                return;
            }

            for (List<TerrainBlock> blocks : columnData.values()) {
                blocks.sort(Comparator.comparingInt(TerrainBlock::getY));
            }

            Map<String, Integer> topHeights = new HashMap<>();
            for (Map.Entry<String, List<TerrainBlock>> entry : columnData.entrySet()) {
                String[] coords = entry.getKey().split(",");
                int x = Integer.parseInt(coords[0]);
                int z = Integer.parseInt(coords[1]);

                if (Math.abs(x - finalCenter.getBlockX()) <= radius && Math.abs(z - finalCenter.getBlockZ()) <= radius) {
                    List<TerrainBlock> blocks = entry.getValue();
                    if (!blocks.isEmpty()) {
                        topHeights.put(entry.getKey(), blocks.get(blocks.size() - 1).getY());
                    }
                }
            }

            if (topHeights.isEmpty()) {
                PlayerUtils.sendWarning(player, "No blocks found within the smoothing radius!");
                return;
            }

            Map<String, Integer> smoothedTopHeights = smoothHeightMap(topHeights, smoothFactor);

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

                    List<TerrainBlock> columnBlocks = columnData.get(key);
                    if (columnBlocks == null || columnBlocks.isEmpty()) continue;

                    if (smoothedTopY < originalTopY) {
                        changesMade += handleLoweringTerrain(finalCenter, x, z, originalTopY, smoothedTopY,
                                columnBlocks, smoothedBlocks);
                    } else {
                        changesMade += handleRaisingTerrain(finalCenter, x, z, originalTopY, smoothedTopY,
                                columnBlocks, smoothedBlocks);
                    }
                }
            }

            changesMade += ensureFullTerrainIntegrity(columnData, smoothedTopHeights, smoothedBlocks, finalCenter, radius);

            if (changesMade == 0) {
                PlayerUtils.sendWarning(player, "No smoothing changes were made");
                return;
            }

            long startTime = System.currentTimeMillis();

            int finalChangesMade = changesMade;

            worldEditor.setBlocks(smoothedBlocks, player).thenAccept(success -> {
                long duration = System.currentTimeMillis() - startTime;
                PlayerUtils.sendActionBar(player, "Smoothed " + finalChangesMade + " blocks in " + duration + "ms");
            });
        });

        return true;
    }

    private int handleLoweringTerrain(Location center, int x, int z, int originalTopY, int smoothedTopY,
                                      List<TerrainBlock> columnBlocks, Map<Location, BlockData> smoothedBlocks) {
        int changesMade = 0;

        for (int y = originalTopY; y > smoothedTopY; y--) {
            Location removeLoc = new Location(center.getWorld(), x, y, z);
            smoothedBlocks.put(removeLoc, Material.AIR.createBlockData());
            changesMade++;
        }

        boolean hasSupportBelow = false;
        for (TerrainBlock block : columnBlocks) {
            if (block.getY() == smoothedTopY - 1) {
                hasSupportBelow = true;
                break;
            }
        }

        if (!hasSupportBelow && smoothedTopY > center.getWorld().getMinHeight() + 1) {
            for (int y = smoothedTopY - 1; y >= Math.max(smoothedTopY - 3, center.getWorld().getMinHeight()); y--) {
                Location supportLoc = new Location(center.getWorld(), x, y, z);
                boolean isCurrentlyAir = true;
                for (TerrainBlock block : columnBlocks) {
                    if (block.getY() == y) {
                        isCurrentlyAir = false;
                        break;
                    }
                }
                if (isCurrentlyAir) {
                    Material supportMaterial = findSupportMaterial(columnBlocks);
                    smoothedBlocks.put(supportLoc, supportMaterial.createBlockData());
                    changesMade++;
                }
            }
        }

        return changesMade;
    }

    private int handleRaisingTerrain(Location center, int x, int z, int originalTopY, int smoothedTopY,
                                     List<TerrainBlock> columnBlocks, Map<Location, BlockData> smoothedBlocks) {
        int changesMade = 0;

        int highestExistingY = originalTopY;
        for (TerrainBlock block : columnBlocks) {
            if (block.getY() > highestExistingY) {
                highestExistingY = block.getY();
            }
        }

        if (smoothedTopY > highestExistingY) {
            Material newMaterial = findBestMaterialForColumn(columnBlocks);
            for (int y = originalTopY + 1; y <= smoothedTopY; y++) {
                Location addLoc = new Location(center.getWorld(), x, y, z);
                smoothedBlocks.put(addLoc, newMaterial.createBlockData());
                changesMade++;
            }
        } else {
            Material newMaterial = findBestMaterialForColumn(columnBlocks);
            for (int y = originalTopY + 1; y <= smoothedTopY; y++) {
                Location replaceLoc = new Location(center.getWorld(), x, y, z);
                smoothedBlocks.put(replaceLoc, newMaterial.createBlockData());
                changesMade++;
            }

            for (int y = smoothedTopY + 1; y <= highestExistingY; y++) {
                Location removeLoc = new Location(center.getWorld(), x, y, z);
                smoothedBlocks.put(removeLoc, Material.AIR.createBlockData());
                changesMade++;
            }
        }

        return changesMade;
    }

    private int ensureFullTerrainIntegrity(Map<String, List<TerrainBlock>> columnData,
                                           Map<String, Integer> smoothedTopHeights,
                                           Map<Location, BlockData> smoothedBlocks,
                                           Location center, int radius) {
        int changesMade = 0;

        for (Map.Entry<String, Integer> entry : smoothedTopHeights.entrySet()) {
            String key = entry.getKey();
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);
            int smoothedY = entry.getValue();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    String neighborKey = (x + dx) + "," + (z + dz);
                    Integer neighborY = smoothedTopHeights.get(neighborKey);

                    if (neighborY != null) {
                        int heightDiff = smoothedY - neighborY;

                        if (heightDiff > 2) {
                            int newY = Math.max(neighborY + 1, smoothedY - 1);
                            if (newY != smoothedY) {
                                smoothedTopHeights.put(key, newY);

                                if (newY < smoothedY) {
                                    for (int y = smoothedY; y > newY; y--) {
                                        Location removeLoc = new Location(center.getWorld(), x, y, z);
                                        smoothedBlocks.put(removeLoc, Material.AIR.createBlockData());
                                        changesMade++;
                                    }
                                } else {
                                    Material material = findBestMaterialForColumn(columnData.get(key));
                                    for (int y = smoothedY + 1; y <= newY; y++) {
                                        Location addLoc = new Location(center.getWorld(), x, y, z);
                                        smoothedBlocks.put(addLoc, material.createBlockData());
                                        changesMade++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return changesMade;
    }

    private Material findSupportMaterial(List<TerrainBlock> columnBlocks) {
        for (TerrainBlock block : columnBlocks) {
            Material material = block.getMaterial();
            if (material == Material.STONE || material == Material.DEEPSLATE ||
                    material == Material.ANDESITE || material == Material.DIORITE ||
                    material == Material.GRANITE) {
                return material;
            }
        }

        return Material.STONE;
    }

    private Material findBestMaterialForColumn(List<TerrainBlock> columnBlocks) {
        if (columnBlocks.isEmpty()) return Material.GRASS_BLOCK;

        TerrainBlock topBlock = columnBlocks.get(columnBlocks.size() - 1);
        if (isGoodSurfaceMaterial(topBlock.getMaterial())) {
            return topBlock.getMaterial();
        }

        Map<Material, Integer> materialCount = new HashMap<>();
        for (TerrainBlock block : columnBlocks) {
            Material material = block.getMaterial();
            if (isGoodSurfaceMaterial(material)) {
                materialCount.put(material, materialCount.getOrDefault(material, 0) + 1);
            }
        }

        if (!materialCount.isEmpty()) {
            return materialCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get().getKey();
        }

        return Material.GRASS_BLOCK;
    }

    private boolean isGoodSurfaceMaterial(Material material) {
        return material == Material.GRASS_BLOCK || material == Material.DIRT ||
                material == Material.STONE || material == Material.SAND ||
                material == Material.GRAVEL || material == Material.COARSE_DIRT ||
                material == Material.PODZOL || material == Material.MYCELIUM;
    }

    private Map<String, Integer> smoothHeightMap(Map<String, Integer> heightMap, int smoothFactor) {
        Map<String, Integer> smoothed = new HashMap<>();

        for (Map.Entry<String, Integer> entry : heightMap.entrySet()) {
            String key = entry.getKey();
            int currentHeight = entry.getValue();
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);

            int sum = 0;
            int totalWeight = 0;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    String neighborKey = (x + dx) + "," + (z + dz);
                    Integer neighborHeight = heightMap.get(neighborKey);

                    if (neighborHeight != null) {
                        int weight = (dx == 0 && dz == 0) ? 2 : 1;
                        sum += neighborHeight * weight;
                        totalWeight += weight;
                    }
                }
            }

            if (totalWeight > 0) {
                int averageHeight = Math.round(sum / (float) totalWeight);
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
        return player.hasPermission("arc.tools.smooth") || player.isOp();
    }

    @Override
    public ToolSettings createDefaultSettings() {
        ToolSettings settings = new ToolSettings();
        settings.set("radius", 5);
        settings.set("smooth_factor", 1);
        return settings;
    }

    @Override
    public Set<String> getConfigurableSettings() {
        return Set.of("radius", "smooth_factor");
    }

    @Override
    protected String getLeftClickDescription() {
        return "Undo last action";
    }

    @Override
    protected String getRightClickDescription() {
        return "Smooth terrain in selected area";
    }

    private static class TerrainBlock {
        private final int y;
        private final Material material;

        public TerrainBlock(int y, Material material) {
            this.y = y;
            this.material = material;
        }

        public int getY() { return y; }
        public Material getMaterial() { return material; }
    }
}