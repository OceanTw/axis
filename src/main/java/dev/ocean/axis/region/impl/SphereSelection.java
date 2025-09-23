package dev.ocean.axis.region.impl;

import dev.ocean.axis.region.Selection;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SphereSelection extends Selection {
    private final Location center;
    @Getter
    private final int radius;

    public SphereSelection(Location center, int radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public CompletableFuture<List<Location>> getBlocksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            if (center == null) {
                return Collections.emptyList();
            }

            List<Location> blocks = new ArrayList<>(
                    (int) Math.pow((radius * 2 + 1), 3)
            );

            int centerX = center.getBlockX();
            int centerY = center.getBlockY();
            int centerZ = center.getBlockZ();

            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                        double dx = x - centerX;
                        double dy = y - centerY;
                        double dz = z - centerZ;

                        if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                            blocks.add(new Location(center.getWorld(), x, y, z));
                        }
                    }
                }
            }

            return blocks;
        });
    }

    @Override
    public Location getCenter() {
        return center;
    }

}
