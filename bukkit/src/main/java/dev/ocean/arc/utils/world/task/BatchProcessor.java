package dev.ocean.arc.utils.world.task;

import dev.ocean.api.world.pattern.BlockPattern;
import dev.ocean.arc.utils.world.chunk.ChunkCoord;
import dev.ocean.arc.utils.world.chunk.ConcurrentChunk;
import dev.ocean.arc.utils.world.chunk.SectionEditor;
import dev.ocean.arc.utils.world.region.ArcRegion;
import net.minecraft.core.Vec3i;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BatchProcessor {
    private final Map<ChunkCoord, ConcurrentChunk> chunks = new ConcurrentHashMap<>();
    private final World world;

    public BatchProcessor(World world) {
        this.world = world;
    }

    public CompletableFuture<Void> setBlocks(ArcRegion region, BlockPattern pattern) {
        CompletableFuture<?>[] futures = new CompletableFuture[region.chunkCount()];
        int index = 0;

        for (int chunkX = region.minChunkX(); chunkX <= region.maxChunkX(); chunkX++) {
            for (int chunkZ = region.minChunkZ(); chunkZ <= region.maxChunkZ(); chunkZ++) {
                final ChunkCoord coord = new ChunkCoord(chunkX, chunkZ);
                futures[index++] = ArcTaskScheduler.submitAsync(() -> {
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

        Map<ChunkCoord, List<Map.Entry<Vec3i, Character>>> grouped = new HashMap<>();
        for (Map.Entry<Vec3i, Character> entry : placements.entrySet()) {
            Vec3i p = entry.getKey();
            ChunkCoord coord = new ChunkCoord(p.getX() >> 4, p.getZ() >> 4);
            grouped.computeIfAbsent(coord, k -> new ArrayList<>()).add(entry);
        }

        CompletableFuture<?>[] futures = grouped.entrySet().stream()
                .map(entry -> ArcTaskScheduler.submitAsync(() -> {
                    processChunkMapPlacements(entry.getKey(), entry.getValue());
                }))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    private void processChunk(ChunkCoord coord, ArcRegion region, BlockPattern pattern) {
        ConcurrentChunk chunk = chunks.computeIfAbsent(coord, ConcurrentChunk::new);

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

    private void processChunkMapPlacements(ChunkCoord coord, List<Map.Entry<Vec3i, Character>> placements) {
        ConcurrentChunk chunk = chunks.computeIfAbsent(coord, ConcurrentChunk::new);

        for (Map.Entry<Vec3i, Character> e : placements) {
            Vec3i p = e.getKey();
            char id = e.getValue();
            chunk.setBlock(p.getX(), p.getY(), p.getZ(), id);
        }
    }

    public CompletableFuture<Void> saveAll() {
        Set<ChunkCoord> modifiedChunks = new HashSet<>(chunks.keySet());

        CompletableFuture<?>[] futures = chunks.entrySet().stream()
                .map(entry -> ArcTaskScheduler.submitAsync(() -> {
                    SectionEditor editor = new SectionEditor(world, entry.getKey());
                    editor.applyChanges(entry.getValue());
                }))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures).thenRun(() -> {
            if (!modifiedChunks.isEmpty()) {
                SectionEditor editor = new SectionEditor(world, modifiedChunks.iterator().next());
                editor.refreshChunksForPlayers(modifiedChunks);
            }
            chunks.clear();
        });
    }
}