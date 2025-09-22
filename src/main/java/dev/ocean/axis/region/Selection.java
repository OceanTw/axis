package dev.ocean.axis.region;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Accessors
public class Selection {
    private Location pos1;
    private Location pos2;

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

            List<Location> blocks = new ArrayList<>((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1));

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
}
