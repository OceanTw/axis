package dev.ocean.arc.utils.world.snapshot;

import dev.ocean.arc.utils.world.chunk.ChunkCoord;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.Arrays;

public class ChunkSnapshot {
    private final World world;
    private final ChunkCoord coord;
    private final char[] blockData;
    private final int chunkMinX, chunkMinZ;

    public ChunkSnapshot(World world, ChunkCoord coord) {
        this.world = world;
        this.coord = coord;
        this.chunkMinX = coord.x() << 4;
        this.chunkMinZ = coord.z() << 4;
        this.blockData = new char[16 * 384 * 16];
    }

    public void capture() {
        net.minecraft.world.level.chunk.ChunkAccess chunk =
                ((CraftWorld) world).getHandle().getChunk(coord.x(), coord.z());

        LevelChunkSection[] sections = chunk.getSections();

        for (int sectionY = 0; sectionY < sections.length; sectionY++) {
            LevelChunkSection section = sections[sectionY];
            if (section == null) continue;

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int worldY = (sectionY << 4) + y;
                        int index = getIndex(x, worldY, z);
                        blockData[index] = (char) Block.getId(section.getBlockState(x, y, z));
                    }
                }
            }
        }
    }

    public void restore() {
        net.minecraft.world.level.chunk.ChunkAccess chunk =
                ((CraftWorld) world).getHandle().getChunk(coord.x(), coord.z());

        LevelChunkSection[] sections = chunk.getSections();

        for (int sectionY = 0; sectionY < sections.length; sectionY++) {
            LevelChunkSection section = sections[sectionY];
            if (section == null) continue;

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int worldY = (sectionY << 4) + y;
                        int index = getIndex(x, worldY, z);
                        int blockId = blockData[index];

                        if (blockId != 0) {
                            section.setBlockState(x, y, z,
                                    net.minecraft.world.level.block.Block.stateById(blockId), false);
                        }
                    }
                }
            }
        }
        chunk.markUnsaved();

        refreshChunk();
    }

    private int getIndex(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }

    private void refreshChunk() {
        ChunkPos chunkPos = new ChunkPos(coord.x(), coord.z());
        ((CraftWorld) world).getHandle().getChunkSource().chunkMap.getPlayers(chunkPos, false)
                .forEach(player -> {
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(
                            ((CraftWorld) world).getHandle().getChunk(coord.x(), coord.z()),
                            ((CraftWorld) world).getHandle().getChunkSource().getLightEngine(),
                            null, null, true
                    ));
                });
    }
}