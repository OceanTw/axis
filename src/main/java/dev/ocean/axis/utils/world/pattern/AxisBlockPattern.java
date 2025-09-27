package dev.ocean.axis.utils.world.pattern;

public interface AxisBlockPattern {
    char getBlockId(int x, int y, int z);
    default boolean isAir() { return false; }
}