package dev.ocean.arc.api;

import dev.ocean.api.ArcToolBelt;
import dev.ocean.api.world.pattern.BlockPattern;
import dev.ocean.arc.format.ArcFormat;
import dev.ocean.arc.utils.world.ArcWorldEditor;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Toolbelt implements ArcToolBelt {
    public CompletableFuture<Void> setBlocks(Map<Location, BlockData> placements) {
        return ArcWorldEditor.get().setBlocks(placements);
    }

    @Override
    public CompletableFuture<Map<Location, BlockData>> getBlocks(Location pos1, Location pos2) {
        return ArcWorldEditor.get().getBlocks(pos1, pos2);
    }

    @Override
    public CompletableFuture<Void> fill(Location pos1, Location pos2, BlockPattern pattern) {
        return ArcWorldEditor.get().fill(pos1, pos2, pattern);
    }

    @Override
    public CompletableFuture<Void> fill(Location pos1, Location pos2, BlockData blockData) {
        return ArcWorldEditor.get().fill(pos1, pos2, blockData);
    }

    @Override
    public CompletableFuture<Void> replace(Location pos1, Location pos2, BlockData previousBlock, BlockData replacedBlock) {
        return ArcWorldEditor.get().replace(pos1, pos2, previousBlock, replacedBlock);
    }

    @Override
    public void saveLocationToFile(Location pos1, Location pos2, File file, Location anchorLocation) throws IOException {
        ArcFormat.saveLocationToFile(pos1, pos2, file, anchorLocation);
    }
}
