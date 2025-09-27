package dev.ocean.axis.utils.world.task;

import dev.ocean.axis.utils.world.chunk.AxisChunkCoord;
import dev.ocean.axis.utils.world.chunk.AxisConcurrentChunk;
import dev.ocean.axis.utils.world.chunk.AxisSectionEditor;
import dev.ocean.axis.utils.world.pattern.AxisBlockPattern;
import dev.ocean.axis.utils.world.region.AxisRegion;
import net.minecraft.core.Vec3i;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AxisBatchProcessor {
    private final Map<AxisChunkCoord, AxisConcurrentChunk> chunks = new ConcurrentHashMap<>();
    private final World world;

    public AxisBatchProcessor(World world) {
        this.world = world;
    }

    public CompletableFuture<Void> setBlocks(AxisRegion region, AxisBlockPattern pattern) {
        CompletableFuture<?>[] futures = new CompletableFuture[region.chunkCount()];
        int index = 0;

        for (int chunkX = region.minChunkX(); chunkX <= region.maxChunkX(); chunkX++) {
            for (int chunkZ = region.minChunkZ(); chunkZ <= region.maxChunkZ(); chunkZ++) {
                final AxisChunkCoord coord = new AxisChunkCoord(chunkX, chunkZ);
                futures[index++] = AxisTaskScheduler.submitAsync(() -> {
                    try {
                        processChunk(coord, region, pattern);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                });
            }
        }

        return CompletableFuture.allOf(futures);
    }
    
    public CompletableFuture<Void> setBlocks(Map<Vec3i, Character> placements) {
        if (placements == null || placements.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Map<AxisChunkCoord, List<Map.Entry<Vec3i, Character>>> grouped = new HashMap<>();
        for (Map.Entry<Vec3i, Character> entry : placements.entrySet()) {
            Vec3i p = entry.getKey();
            AxisChunkCoord coord = new AxisChunkCoord(p.getX() >> 4, p.getZ() >> 4);
            grouped.computeIfAbsent(coord, k -> new ArrayList<>()).add(entry);
        }

        CompletableFuture<?>[] futures = grouped.entrySet().stream()
                .map(entry -> AxisTaskScheduler.submitAsync(() -> {
                    processChunkMapPlacements(entry.getKey(), entry.getValue());
                }))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    private void processChunk(AxisChunkCoord coord, AxisRegion region, AxisBlockPattern pattern) {
        AxisConcurrentChunk chunk = chunks.computeIfAbsent(coord, AxisConcurrentChunk::new);

        int chunkMinX = coord.x() << 4;
        int chunkMinZ = coord.z() << 4;

        int startX = Math.max(region.minX, chunkMinX);
        int endX = Math.min(region.maxX, chunkMinX + 15);
        int startZ = Math.max(region.minZ, chunkMinZ);
        int endZ = Math.min(region.maxZ, chunkMinZ + 15);

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                for (int y = region.minY; y <= region.maxY; y++) {
                    char blockId = pattern.getBlockId(x, y, z);
                    chunk.setBlock(x, y, z, blockId);
                }
            }
        }
    }

    private void processChunkMapPlacements(AxisChunkCoord coord, List<Map.Entry<Vec3i, Character>> placements) {
        AxisConcurrentChunk chunk = chunks.computeIfAbsent(coord, AxisConcurrentChunk::new);

        for (Map.Entry<Vec3i, Character> e : placements) {
            Vec3i p = e.getKey();
            char id = e.getValue();
            chunk.setBlock(p.getX(), p.getY(), p.getZ(), id);
        }
    }

    public CompletableFuture<Void> saveAll() {
        Set<AxisChunkCoord> modifiedChunks = new HashSet<>(chunks.keySet());

        CompletableFuture<?>[] futures = chunks.entrySet().stream()
                .map(entry -> AxisTaskScheduler.submitAsync(() -> {
                    AxisSectionEditor editor = new AxisSectionEditor(world, entry.getKey());
                    editor.applyChanges(entry.getValue());
                }))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures).thenRun(() -> {
            if (!modifiedChunks.isEmpty()) {
                AxisSectionEditor editor = new AxisSectionEditor(world, modifiedChunks.iterator().next());
                editor.refreshChunksForPlayers(modifiedChunks);
            }
            chunks.clear();
        });
    }
}