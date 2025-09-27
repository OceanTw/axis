package dev.ocean.arc.utils.world.pattern;

import dev.ocean.api.world.pattern.BlockPattern;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SinglePattern implements BlockPattern {
    private final char blockId;

    public SinglePattern(BlockState state) {
        this.blockId = (char) Block.getId(state);
    }

    @Override
    public char getBlockId(int x, int y, int z) {
        return blockId;
    }
}