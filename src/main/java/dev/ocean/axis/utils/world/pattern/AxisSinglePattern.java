package dev.ocean.axis.utils.world.pattern;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AxisSinglePattern implements AxisBlockPattern {
    private final char blockId;

    public AxisSinglePattern(BlockState state) {
        this.blockId = (char) Block.getId(state);
    }

    @Override
    public char getBlockId(int x, int y, int z) {
        return blockId;
    }
}