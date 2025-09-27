package dev.ocean.arc.utils.world;

import dev.ocean.api.ArcToolBelt;
import dev.ocean.api.world.pattern.BlockPattern;
import dev.ocean.arc.utils.world.pattern.ReplacePattern;
import dev.ocean.arc.utils.world.pattern.SinglePattern;
import dev.ocean.arc.utils.world.region.ArcRegion;
import dev.ocean.arc.utils.world.task.BatchProcessor;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ArcWorldEditor implements ArcToolBelt {
    private final Map<World, BatchProcessor> processors = new ConcurrentHashMap<>();

    private final Map<Character, BlockState> idToState = new ConcurrentHashMap<>();
    private final Map<BlockState, Character> stateToId = new ConcurrentHashMap<>();
    private final AtomicInteger nextBlockId = new AtomicInteger(1);

    private static ArcWorldEditor instance;

    public static ArcWorldEditor get() {
        if (instance == null) {
            instance = new ArcWorldEditor();
        }
        return instance;
    }

    private BatchProcessor getProcessor(World world) {
        return processors.computeIfAbsent(world, BatchProcessor::new);
    }

    @Override
    public CompletableFuture<Void> fill(Location pos1, Location pos2, BlockData blockData) {
        return fill(pos1, pos2, new SinglePattern(((CraftBlockData) blockData).getState()));
    }

    @Override
    public CompletableFuture<Void> fill(Location pos1, Location pos2, BlockPattern pattern) {
        World world = pos1.getWorld();
        World world2 = pos2.getWorld();

        if (world == null || world2 == null) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Both locations must have a non-null world"));
            return failed;
        }

        if (!world.equals(world2)) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Both locations must be in the same world"));
            return failed;
        }

        ArcRegion region = new ArcRegion(
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()
        );

        return getProcessor(world).setBlocks(region, pattern);
    }

    @Override
    public CompletableFuture<Void> replace(Location pos1, Location pos2, BlockData from, BlockData to) {
        World world = pos1.getWorld();
        ArcRegion region = new ArcRegion(
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()
        );

        BlockState fromState = ((CraftBlockData) from).getState();
        BlockState toState = ((CraftBlockData) to).getState();

        BlockPattern pattern = new ReplacePattern(fromState, toState);
        return getProcessor(world).setBlocks(region, pattern);
    }

    public CompletableFuture<Void> save(World world) {
        BatchProcessor processor = processors.get(world);
        if (processor != null) {
            return processor.saveAll();
        }
        return CompletableFuture.completedFuture(null);
    }

    public void shutdown() {
        processors.values().forEach(processor -> processor.saveAll());
    }

     @Override
    public CompletableFuture<Map<Location, BlockData>> getBlocks(Location min, Location max) {
        if (min == null || max == null) {
            CompletableFuture<Map<Location, BlockData>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("min and max locations must be non-null"));
            return failed;
        }

        World world = min.getWorld();
        if (world == null || !world.equals(max.getWorld())) {
            CompletableFuture<Map<Location, BlockData>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Both locations must be in the same non-null world"));
            return failed;
        }

        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

        return CompletableFuture.supplyAsync(() -> {
            Map<Location, BlockData> result = new HashMap<>();
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        org.bukkit.block.Block block = world.getBlockAt(x, y, z);
                        BlockData data = block.getBlockData();
                        result.put(new Location(world, x, y, z), data);
                    }
                }
            }
            return result;
        });
    }

    public CompletableFuture<Void> setBlocks(Map<Location, BlockData> placements) {
        if (placements == null || placements.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Map<World, Map<Vec3i, Character>> perWorld = new HashMap<>();

        for (Map.Entry<Location, BlockData> e : placements.entrySet()) {
            Location loc = e.getKey();
            if (loc == null) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new IllegalArgumentException("Location keys must not be null"));
                return failed;
            }

            World w = loc.getWorld();
            if (w == null) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new IllegalArgumentException("All locations must have a non-null world"));
                return failed;
            }

            BlockData data = e.getValue();
            if (data == null) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new IllegalArgumentException("BlockData values must not be null"));
                return failed;
            }

            BlockState state = ((CraftBlockData) data).getState();
            char id = getOrCreateBlockId(state);

            Vec3i vec = new Vec3i(
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ()
            );

            perWorld.computeIfAbsent(w, k -> new HashMap<>()).put(vec, id);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>(perWorld.size());
        for (Map.Entry<World, Map<Vec3i, Character>> entry : perWorld.entrySet()) {
            World world = entry.getKey();
            Map<Vec3i, Character> mapForWorld = entry.getValue();
            futures.add(getProcessor(world).setBlocks(mapForWorld));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private char getOrCreateBlockId(BlockState state) {
        Character existing = stateToId.get(state);
        if (existing != null) return existing;

        synchronized (this) {
            existing = stateToId.get(state);
            if (existing != null) return existing;

            int next = nextBlockId.getAndIncrement();
            if (next > Character.MAX_VALUE) {
                throw new IllegalStateException("Exceeded maximum number of distinct block ids");
            }
            char id = (char) next;
            idToState.put(id, state);
            stateToId.put(state, id);
            return id;
        }
    }
}