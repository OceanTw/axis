package dev.ocean.arc.utils.world.region;

public class ArcRegion {
    public final int minX, minY, minZ, maxX, maxY, maxZ;

    public ArcRegion(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public int minChunkX() { return minX >> 4; }
    public int maxChunkX() { return maxX >> 4; }
    public int minChunkZ() { return minZ >> 4; }
    public int maxChunkZ() { return maxZ >> 4; }
    public int chunkCount() {
        return (maxChunkX() - minChunkX() + 1) * (maxChunkZ() - minChunkZ() + 1);
    }
}