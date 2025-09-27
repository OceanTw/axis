package dev.ocean.arc.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class RaycastResult {
    @Getter
    private final Block block;
    @Getter
    private final BlockFace hitFace;
    private final Location hitLocation;

    public RaycastResult(Block block, BlockFace hitFace, Location hitLocation) {
        this.block = block;
        this.hitFace = hitFace;
        this.hitLocation = hitLocation;
    }

    public Location getLocation() {
        return hitLocation;
    }
}