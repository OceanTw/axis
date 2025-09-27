package dev.ocean.arc.api;

import dev.ocean.api.ArcApi;
import dev.ocean.api.tools.ArcTool;
import dev.ocean.api.world.pattern.BlockPattern;
import dev.ocean.arc.format.ArcFormat;
import dev.ocean.arc.tools.ToolService;
import dev.ocean.arc.utils.world.ArcWorldEditor;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ArcApiImpl implements ArcApi {

    @Override
    public void registerTool(ArcTool tool) {
        ToolService.get().registerCustomTool(tool);
    }

    @Override
    public CompletableFuture<Void> setBlocks(Map<Location, BlockData> placements, Player executor) {
        return ArcWorldEditor.get().setBlocks(placements, executor);
    }

    @Override
    public CompletableFuture<Map<Location, BlockData>> getBlocks(Location pos1, Location pos2) {
        return ArcWorldEditor.get().getBlocks(pos1, pos2);
    }

    @Override
    public CompletableFuture<Integer> fill(Location pos1, Location pos2, BlockPattern pattern, Player executor) {
        return ArcWorldEditor.get().fill(pos1, pos2, pattern, executor);
    }

    @Override
    public CompletableFuture<Integer> fill(Location pos1, Location pos2, BlockData blockData, Player executor) {
        return ArcWorldEditor.get().fill(pos1, pos2, blockData, executor);
    }

    @Override
    public CompletableFuture<Integer> replace(Location pos1, Location pos2, BlockData previousBlock, BlockData replacedBlock, Player executor) {
        return ArcWorldEditor.get().replace(pos1, pos2, previousBlock, replacedBlock, executor);
    }

    @Override
    public void saveLocationToFile(Location pos1, Location pos2, File file, Location anchorLocation) throws IOException {
        ArcFormat.saveLocationToFile(pos1, pos2, file, anchorLocation);
    }
}
