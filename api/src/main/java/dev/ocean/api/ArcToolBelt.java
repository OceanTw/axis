package dev.ocean.api;

import dev.ocean.api.world.pattern.BlockPattern;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ArcToolBelt {

    // Setters
    CompletableFuture<Void> setBlocks(Map<Location, BlockData> placements);

    // getters
    CompletableFuture<Map<Location, BlockData>> getBlocks(Location pos1, Location pos2);

    // fill
    CompletableFuture<Void> fill(Location pos1, Location pos2, BlockPattern pattern);
    CompletableFuture<Void> fill(Location pos1, Location pos2, BlockData blockData);

    // Replace Functions
    CompletableFuture<Void> replace(Location pos1, Location pos2, BlockData previousBlock, BlockData replacedBlock);

    void saveLocationToFile(Location pos1, Location pos2, File file, Location anchorLocation) throws IOException;
}
