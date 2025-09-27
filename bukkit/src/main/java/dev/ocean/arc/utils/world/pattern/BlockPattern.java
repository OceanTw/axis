package dev.ocean.arc.utils.world.pattern;

public interface BlockPattern {
    char getBlockId(int x, int y, int z);
    default boolean isAir() { return false; }
}