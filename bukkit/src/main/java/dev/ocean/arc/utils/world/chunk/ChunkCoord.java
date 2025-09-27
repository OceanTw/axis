package dev.ocean.arc.utils.world.chunk;

import net.minecraft.world.level.ChunkPos;

public record ChunkCoord(int x, int z) {
    public static ChunkCoord fromBlock(int blockX, int blockZ) {
        return new ChunkCoord(blockX >> 4, blockZ >> 4);
    }

    public static ChunkCoord fromChunkPos(ChunkPos pos) {
        return new ChunkCoord(pos.x, pos.z);
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