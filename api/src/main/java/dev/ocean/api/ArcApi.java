package dev.ocean.api;

import dev.ocean.api.tools.ArcTool;
import dev.ocean.api.world.pattern.BlockPattern;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ArcApi {

    void registerTool(ArcTool tool);

    // Setters
    CompletableFuture<Void> setBlocks(Map<Location, BlockData> placements, Player executor);

    // getters
    CompletableFuture<Map<Location, BlockData>> getBlocks(Location pos1, Location pos2);

    // fill
    CompletableFuture<Integer> fill(Location pos1, Location pos2, BlockPattern pattern, Player executor);
    CompletableFuture<Integer> fill(Location pos1, Location pos2, BlockData blockData, Player executor);

    // Replace Functions
    CompletableFuture<Integer> replace(Location pos1, Location pos2, BlockData previousBlock, BlockData replacedBlock, Player executor);

    void saveLocationToFile(Location pos1, Location pos2, File file, Location anchorLocation) throws IOException;
}
