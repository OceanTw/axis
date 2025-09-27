package dev.ocean.arc.utils.world;

import dev.ocean.api.world.pattern.BlockPattern;
import dev.ocean.arc.utils.world.pattern.ReplacePattern;
import dev.ocean.arc.utils.world.pattern.SinglePattern;
import dev.ocean.arc.utils.world.region.ArcRegion;
import dev.ocean.arc.utils.world.snapshot.ArcCuboidSnapshot;
import dev.ocean.arc.utils.world.task.BatchProcessor;
import lombok.experimental.UtilityClass;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ArcWorldEditor {
    private final Map<World, BatchProcessor> processors = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<HistoryEntry>> playerHistories = new ConcurrentHashMap<>();

    private final Map<Character, BlockState> idToState = new ConcurrentHashMap<>();
    private final Map<BlockState, Character> stateToId = new ConcurrentHashMap<>();
    private final AtomicInteger nextBlockId = new AtomicInteger(1);

    private static final int MAX_HISTORY_SIZE = 20;

    private static ArcWorldEditor instance;

    public static ArcWorldEditor get() {
        if (instance == null) {
            instance = new ArcWorldEditor();
        }
        return instance;
    }

    private record HistoryEntry(ArcCuboidSnapshot snapshot) {

        public CompletableFuture<Void> undo() {
                return CompletableFuture.runAsync(snapshot::restore);
            }
        }

    private BatchProcessor getProcessor(World world) {
        return processors.computeIfAbsent(world, BatchProcessor::new);
    }

    private Deque<HistoryEntry> getPlayerHistory(UUID playerId) {
        Deque<HistoryEntry> history = playerHistories.computeIfAbsent(playerId, id -> new ArrayDeque<>());
        while (history.size() >= MAX_HISTORY_SIZE) {
            history.removeLast();
        }
        return history;
    }

    public CompletableFuture<Integer> fill(Location pos1, Location pos2, BlockData blockData, Player player) {
        return fill(pos1, pos2, new SinglePattern(((CraftBlockData) blockData).getState()), player);
    }

    public CompletableFuture<Integer> fill(Location pos1, Location pos2, BlockPattern pattern, Player player) {
        World world = pos1.getWorld();
        if (world == null || !world.equals(pos2.getWorld())) {
            return CompletableFuture.completedFuture(0);
        }

        ArcRegion region = new ArcRegion(
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()
        );

        ArcCuboidSnapshot snapshot = new ArcCuboidSnapshot(world, region);
        snapshot.capture();

        CompletableFuture<Void> operation = getProcessor(world).setBlocks(region, pattern)
                .thenCompose(unused -> save(world));

        if (player != null) {
            operation = operation.thenRun(() -> {
                HistoryEntry entry = new HistoryEntry(snapshot);
                getPlayerHistory(player.getUniqueId()).push(entry);
            });
        }

        return operation.thenApply(unused -> region.blockCount());
    }

    public CompletableFuture<Integer> replace(Location pos1, Location pos2, BlockData from, BlockData to, Player player) {
        World world = pos1.getWorld();
        ArcRegion region = new ArcRegion(
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()
        );

        BlockState fromState = ((CraftBlockData) from).getState();
        BlockState toState = ((CraftBlockData) to).getState();

        BlockPattern pattern = new ReplacePattern(fromState, toState);

        ArcCuboidSnapshot snapshot = new ArcCuboidSnapshot(world, region);
        snapshot.capture();

        CompletableFuture<Void> operation = getProcessor(world).setBlocks(region, pattern)
                .thenCompose(unused -> save(world));

        if (player != null) {
            operation = operation.thenRun(() -> {
                HistoryEntry entry = new HistoryEntry(snapshot);
                getPlayerHistory(player.getUniqueId()).push(entry);
            });
        }

        return operation.thenApply(unused -> region.blockCount());
    }

    public CompletableFuture<Void> setBlocks(Map<Location, BlockData> placements, Player player) {
        if (placements == null || placements.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Location firstLoc = placements.keySet().iterator().next();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (Location loc : placements.keySet()) {
            minX = Math.min(minX, loc.getBlockX());
            minY = Math.min(minY, loc.getBlockY());
            minZ = Math.min(minZ, loc.getBlockZ());
            maxX = Math.max(maxX, loc.getBlockX());
            maxY = Math.max(maxY, loc.getBlockY());
            maxZ = Math.max(maxZ, loc.getBlockZ());
        }

        ArcRegion region = new ArcRegion(minX, minY, minZ, maxX, maxY, maxZ);
        ArcCuboidSnapshot snapshot = new ArcCuboidSnapshot(firstLoc.getWorld(), region);
        snapshot.capture();

        Map<World, Map<Vec3i, Character>> perWorld = new HashMap<>();
        for (Map.Entry<Location, BlockData> e : placements.entrySet()) {
            Location loc = e.getKey();
            World w = loc.getWorld();
            BlockState state = ((CraftBlockData) e.getValue()).getState();
            char id = getOrCreateBlockId(state);
            Vec3i vec = new Vec3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            perWorld.computeIfAbsent(w, k -> new HashMap<>()).put(vec, id);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<World, Map<Vec3i, Character>> entry : perWorld.entrySet()) {
            World world = entry.getKey();
            CompletableFuture<Void> future = getProcessor(world).setBlocks(entry.getValue())
                    .thenCompose(unused -> save(world));

            if (player != null) {
                future = future.thenRun(() -> {
                    HistoryEntry historyEntry = new HistoryEntry(snapshot);
                    getPlayerHistory(player.getUniqueId()).push(historyEntry);
                });
            }
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public CompletableFuture<Void> undo(Player player) {
        Deque<HistoryEntry> history = getPlayerHistory(player.getUniqueId());
        if (history.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        HistoryEntry entry = history.pop();
        return entry.undo();
    }

    public void clearHistory(Player player) {
        playerHistories.remove(player.getUniqueId());
    }

    public int getHistorySize(Player player) {
        return getPlayerHistory(player.getUniqueId()).size();
    }

    public CompletableFuture<Integer> fill(Location pos1, Location pos2, BlockData blockData) {
        return fill(pos1, pos2, blockData, null);
    }

    public CompletableFuture<Integer> fill(Location pos1, Location pos2, BlockPattern pattern) {
        return fill(pos1, pos2, pattern, null);
    }

    public CompletableFuture<Integer> replace(Location pos1, Location pos2, BlockData from, BlockData to) {
        return replace(pos1, pos2, from, to, null);
    }

    private CompletableFuture<Void> save(World world) {
        BatchProcessor processor = processors.get(world);
        return processor != null ? processor.saveAll() : CompletableFuture.completedFuture(null);
    }

    public void shutdown() {
        processors.values().forEach(BatchProcessor::saveAll);
    }

    // TODO: API
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

    // TODO: API
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
            futures.add(getProcessor(world).setBlocks(mapForWorld).thenAccept(unused -> save(world)));
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