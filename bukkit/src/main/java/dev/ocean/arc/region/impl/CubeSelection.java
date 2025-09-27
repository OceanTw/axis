package dev.ocean.arc.region.impl;

import dev.ocean.arc.region.Selection;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO: API
@Getter
@Setter
@Accessors(chain = true)
public class CubeSelection extends Selection {
    private Location pos1;
    private Location pos2;

    @Override
    public CompletableFuture<List<Location>> getBlocksAsync() {
        return CompletableFuture.supplyAsync(() -> {
            if (pos1 == null || pos2 == null || !pos1.getWorld().equals(pos2.getWorld())) {
                return Collections.emptyList();
            }

            int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
            int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
            int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

            List<Location> blocks = new ArrayList<>(
                    (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)
            );

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        blocks.add(new Location(pos1.getWorld(), x, y, z));
                    }
                }
            }

            return blocks;
        });
    }

    @Override
    public Location getCenter() {
        if (pos1 == null || pos2 == null || !pos1.getWorld().equals(pos2.getWorld())) {
            return null;
        }
        return new Location(
                pos1.getWorld(),
                (pos1.getX() + pos2.getX()) / 2,
                (pos1.getY() + pos2.getY()) / 2,
                (pos1.getZ() + pos2.getZ()) / 2
        );
    }

    public List<Location> getVertices() {
        if (pos1 == null || pos2 == null || !pos1.getWorld().equals(pos2.getWorld())) {
            return Collections.emptyList();
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        return Arrays.asList(
                new Location(pos1.getWorld(), minX, minY, minZ),
                new Location(pos1.getWorld(), minX, minY, maxZ),
                new Location(pos1.getWorld(), minX, maxY, minZ),
                new Location(pos1.getWorld(), minX, maxY, maxZ),
                new Location(pos1.getWorld(), maxX, minY, minZ),
                new Location(pos1.getWorld(), maxX, minY, maxZ),
                new Location(pos1.getWorld(), maxX, maxY, minZ),
                new Location(pos1.getWorld(), maxX, maxY, maxZ)
        );
    }
}
