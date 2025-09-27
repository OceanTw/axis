package dev.ocean.axis.utils.world.chunk;

import net.minecraft.world.level.ChunkPos;

public record AxisChunkCoord(int x, int z) {
    public static AxisChunkCoord fromBlock(int blockX, int blockZ) {
        return new AxisChunkCoord(blockX >> 4, blockZ >> 4);
    }

    public static AxisChunkCoord fromChunkPos(ChunkPos pos) {
        return new AxisChunkCoord(pos.x, pos.z);
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(x, z);
    }

    public int getBlockX(int localX) {
        return (x << 4) + localX;
    }

    public int getBlockZ(int localZ) {
        return (z << 4) + localZ;
    }
}