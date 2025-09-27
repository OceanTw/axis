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

public class MaterialPercentagePattern implements BlockPattern {
    private final Map<Material, Double> materialPercentages;
    private final boolean replaceAirOnly;
    private final org.bukkit.World world;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Map<Material, Double> cumulativePercentages = new HashMap<>();
    private double totalPercentage = 0;

    public MaterialPercentagePattern(Map<Material, Double> materialPercentages, boolean replaceAirOnly, org.bukkit.World world) {
        this.materialPercentages = new HashMap<>(materialPercentages);
        this.replaceAirOnly = replaceAirOnly;
        this.world = world;
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
        if (replaceAirOnly) {
            Location loc = new Location(world, x, y, z);
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
}