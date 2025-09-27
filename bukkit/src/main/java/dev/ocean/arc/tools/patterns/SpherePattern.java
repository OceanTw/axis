package dev.ocean.arc.tools.patterns;

import dev.ocean.arc.utils.world.pattern.BlockPattern;
import net.minecraft.world.level.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SpherePattern implements BlockPattern {
    private final Map<Material, Double> materialPercentages;
    private final boolean replaceAirOnly;
    private final boolean hollow;
    private final int radius;
    private final Location center;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Map<Material, Double> cumulativePercentages = new HashMap<>();
    private double totalPercentage = 0;
    private final int radiusSquared;
    private final int innerRadiusSquared;

    public SpherePattern(Map<Material, Double> materialPercentages, boolean replaceAirOnly,
                         boolean hollow, int radius, Location center) {
        this.materialPercentages = new HashMap<>(materialPercentages);
        this.replaceAirOnly = replaceAirOnly;
        this.hollow = hollow;
        this.radius = radius;
        this.center = center;
        this.radiusSquared = radius * radius;
        this.innerRadiusSquared = hollow ? (radius - 1) * (radius - 1) : -1;
        calculateCumulativePercentages();
    }

    private void calculateCumulativePercentages() {
        cumulativePercentages.clear();
        totalPercentage = materialPercentages.values().stream().mapToDouble(Double::doubleValue).sum();

        if (totalPercentage <= 0) return;

        double cumulative = 0;
        for (Map.Entry<Material, Double> entry : materialPercentages.entrySet()) {
            cumulative += entry.getValue();
            cumulativePercentages.put(entry.getKey(), cumulative);
        }
    }

    @Override
    public char getBlockId(int x, int y, int z) {
        int dx = x - center.getBlockX();
        int dy = y - center.getBlockY();
        int dz = z - center.getBlockZ();
        int distanceSquared = dx * dx + dy * dy + dz * dz;

        if (distanceSquared > radiusSquared) {
            return 0;
        }

        if (hollow && distanceSquared < innerRadiusSquared) {
            return 0;
        }

        if (replaceAirOnly) {
            Location loc = new Location(center.getWorld(), x, y, z);
            if (!loc.getBlock().getType().isAir()) {
                return 0;
            }
        }

        Material selected = selectMaterial();
        if (selected != null) {
            BlockData blockData = selected.createBlockData();
            return (char) Block.getId(((CraftBlockData) blockData).getState());
        }

        return 0;
    }

    private Material selectMaterial() {
        if (cumulativePercentages.isEmpty()) return null;

        double randomValue = random.nextDouble() * totalPercentage;
        Material lastMaterial = null;

        for (Map.Entry<Material, Double> entry : cumulativePercentages.entrySet()) {
            lastMaterial = entry.getKey();
            if (randomValue <= entry.getValue()) {
                return entry.getKey();
            }
        }

        return lastMaterial;
    }

    @Override
    public boolean isAir() {
        return false;
    }
}