package dev.ocean.axis.utils.world.pattern;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AxisReplacePattern implements AxisBlockPattern {
    private final char fromId;
    private final char toId;

    public AxisReplacePattern(BlockState from, BlockState to) {
        this.fromId = (char) Block.getId(from);
        this.toId = (char) Block.getId(to);
    }

    @Override
    public char getBlockId(int x, int y, int z) {
        return toId;
    }

    public boolean shouldReplace(char currentId) {
        return currentId == fromId;
    }
}