package dev.ocean.axis.utils.world.chunk;

import java.util.concurrent.locks.ReentrantLock;

public class AxisConcurrentChunk {
    private final AxisChunkCoord coord;
    private final AxisChunkSection[] sections;
    private final ReentrantLock[] sectionLocks;

    public AxisConcurrentChunk(AxisChunkCoord coord) {
        this.coord = coord;
        this.sections = new AxisChunkSection[24];
        this.sectionLocks = new ReentrantLock[24];
        for (int i = 0; i < sectionLocks.length; i++) {
            sectionLocks[i] = new ReentrantLock();
        }
    }

    public void setBlock(int x, int y, int z, char blockId) {
        int sectionIndex = (y >> 4) + 4;
        if (sectionIndex < 0 || sectionIndex >= sections.length) return;

        sectionLocks[sectionIndex].lock();
        try {
            AxisChunkSection section = sections[sectionIndex];
            if (section == null) {
                section = new AxisChunkSection(sectionIndex);
                sections[sectionIndex] = section;
            }
            section.setBlock(x & 15, y & 15, z & 15, blockId);
        } finally {
            sectionLocks[sectionIndex].unlock();
        }
    }

    public void fillArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, char blockId) {
        int minSection = (minY >> 4) + 4;
        int maxSection = (maxY >> 4) + 4;

        for (int sectionIndex = minSection; sectionIndex <= maxSection; sectionIndex++) {
            if (sectionIndex < 0 || sectionIndex >= sections.length) continue;

            sectionLocks[sectionIndex].lock();
            try {
                AxisChunkSection section = sections[sectionIndex];
                if (section == null && blockId == AxisChunkSection.AIR) continue;

                if (section == null) {
                    section = new AxisChunkSection(sectionIndex);
                    sections[sectionIndex] = section;
                }

                int sectionMinY = Math.max(minY, sectionIndex << 4) & 15;
                int sectionMaxY = Math.min(maxY, (sectionIndex << 4) + 15) & 15;
                int sectionMinX = minX & 15;
                int sectionMaxX = maxX & 15;
                int sectionMinZ = minZ & 15;
                int sectionMaxZ = maxZ & 15;

                section.fillArea(sectionMinX, sectionMinY, sectionMinZ,
                        sectionMaxX, sectionMaxY, sectionMaxZ, blockId);
            } finally {
                sectionLocks[sectionIndex].unlock();
            }
        }
    }

    public AxisChunkSection[] getSections() {
        return sections;
    }

    public AxisChunkCoord getCoord() {
        return coord;
    }
}