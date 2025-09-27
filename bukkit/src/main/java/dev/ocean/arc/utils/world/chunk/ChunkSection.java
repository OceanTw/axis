package dev.ocean.arc.utils.world.chunk;

import lombok.Getter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.Arrays;

public class ChunkSection {
    private static final int SIZE = 16 * 16 * 16;
    protected static final char AIR = 0;

    @Getter
    private char[] blocks;
    private final int sectionY;
    @Getter
    private boolean modified = false;

    public ChunkSection(int sectionY) {
        this.sectionY = sectionY;
    }

    public void setBlock(int x, int y, int z, char blockId) {
        if (blocks == null) {
            if (blockId == AIR) return;
            blocks = new char[SIZE];
            Arrays.fill(blocks, AIR);
        }
        int index = (y << 8) | (z << 4) | x;
        blocks[index] = blockId;
        modified = true;
    }

    public char getBlockId(int x, int y, int z) {
        if (blocks == null) return AIR;
        int index = (y << 8) | (z << 4) | x;
        return blocks[index];
    }

    public void fillArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, char blockId) {
        if (blocks == null && blockId == AIR) return;

        if (blocks == null) {
            blocks = new char[SIZE];
            Arrays.fill(blocks, AIR);
        }

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    int index = (y << 8) | (z << 4) | x;
                    blocks[index] = blockId;
                }
            }
        }
        modified = true;
    }

    public boolean isAllocated() {
        return blocks != null;
    }

    public LevelChunkSection toNmsSection() {
        if (blocks == null) return null;

        LevelChunkSection section = new LevelChunkSection(null, null);
        for (int i = 0; i < SIZE; i++) {
            if (blocks[i] != AIR) {
                int x = i & 15;
                int y = (i >> 8) & 15;
                int z = (i >> 4) & 15;
                BlockState state = Block.stateById(blocks[i]);
                section.setBlockState(x, y, z, state, false);
            }
        }
        return section;
    }
}