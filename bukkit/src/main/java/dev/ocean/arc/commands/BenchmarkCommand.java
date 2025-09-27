package dev.ocean.arc.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import dev.lrxh.blockChanger.BlockChanger;
import dev.ocean.arc.utils.PlayerUtils;
import dev.ocean.arc.utils.world.ArcWorldEditor;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Command(name = "benchmark")
@Permission("arc.benchmark")
public class BenchmarkCommand {

    @Execute(name = "blockchanger")
    public void blockchanger(@Context Player sender) {
        org.bukkit.World bukkitWorld = sender.getWorld();

        int centerX = sender.getLocation().getBlockX();
        int centerY = sender.getLocation().getBlockY();
        int centerZ = sender.getLocation().getBlockZ();
        int size = 100;

        PlayerUtils.sendInfo(sender, "Starting BlockChanger block placement benchmark...");

        Location pos1 = new Location(bukkitWorld, centerX, centerY, centerZ);
        Location pos2 = new Location(bukkitWorld, centerX + size, centerY + size, centerZ + size);
        BlockData stoneData = Material.STONE.createBlockData();

        Map<Location, BlockData> blockMap = new HashMap<>();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blockMap.put(new Location(bukkitWorld, x, y, z), stoneData);
                }
            }
        }

        long start = System.nanoTime();

        BlockChanger.setBlocks(blockMap, false).thenAccept(unused -> {
            long end = System.nanoTime();
            PlayerUtils.sendInfo(sender, "BlockChanger Results: " + size * size * size + " blocks in " + (end - start) / 1_000_000 + "ms");
        });
    }

    @Execute(name = "")
    public void arc(@Context Player sender) {
        org.bukkit.World bukkitWorld = sender.getWorld();

        int centerX = sender.getLocation().getBlockX();
        int centerY = sender.getLocation().getBlockY();
        int centerZ = sender.getLocation().getBlockZ();
        int size = 100;

        World world = BukkitAdapter.adapt(bukkitWorld);
        BlockVector3 min = BlockVector3.at(centerX, centerY, centerZ);
        BlockVector3 max = BlockVector3.at(centerX + size, centerY + size, centerZ + size);

        Region region = new CuboidRegion(world, min, max);

        PlayerUtils.sendInfo(sender, "Starting ArcWorldEditor block placement benchmark...");

        Location pos1 = new Location(bukkitWorld, centerX, centerY, centerZ);
        Location pos2 = new Location(bukkitWorld, centerX + size, centerY + size, centerZ + size);
        BlockData stoneData = Material.STONE.createBlockData();

        long start = System.nanoTime();

        ArcWorldEditor editor = ArcWorldEditor.get();
        editor.fill(pos1, pos2, stoneData)
                .thenCompose(unused -> {
                    long fillTime = System.nanoTime();
                    PlayerUtils.sendInfo(sender, "ArcWorldEditor Fill: " + size * size * size + " blocks in " + (fillTime - start) / 1_000_000 + "ms");

                    return CompletableFuture.completedFuture(null);
                })
                .thenRun(() -> {
                    long saveTime = System.nanoTime();
                    PlayerUtils.sendInfo(sender, "Changes saved to world! Total time: " + (saveTime - start) / 1_000_000 + "ms");
                })
                .exceptionally(throwable -> {
                    PlayerUtils.sendError(sender, "Error during benchmark: " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                });
    }

    @Execute(name = "fawe")
    public void fawe(@Context Player sender) {
        org.bukkit.World bukkitWorld = sender.getWorld();

        int centerX = sender.getLocation().getBlockX();
        int centerY = sender.getLocation().getBlockY();
        int centerZ = sender.getLocation().getBlockZ();
        int size = 100;

        World world = BukkitAdapter.adapt(bukkitWorld);
        BlockVector3 min = BlockVector3.at(centerX, centerY, centerZ);
        BlockVector3 max = BlockVector3.at(centerX + size, centerY + size, centerZ + size);

        Region region = new CuboidRegion(world, min, max);

        PlayerUtils.sendInfo(sender, "Starting FAWE block placement benchmark...");

        long start = System.nanoTime();
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            editSession.setBlocks(region, BlockTypes.STONE);
            editSession.flushQueue();
            long end = System.nanoTime();
            PlayerUtils.sendInfo(sender, "FAWE Results: " + size * size * size + " blocks in " + (end - start) / 1000000 + "ms");
        }
    }
}