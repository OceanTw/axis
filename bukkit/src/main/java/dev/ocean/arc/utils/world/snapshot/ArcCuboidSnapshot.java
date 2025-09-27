package dev.ocean.arc.utils.world.snapshot;

import dev.ocean.arc.utils.world.chunk.ChunkCoord;
import dev.ocean.arc.utils.world.region.ArcRegion;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.HashMap;
import java.util.Map;

public class ArcCuboidSnapshot {
    @Getter
    private final World world;
    @Getter
    private final ArcRegion region;
    private final Map<ChunkCoord, char[]> chunkData; // chunk coord -> block data array
    @Getter
    private final long timestamp;

    public ArcCuboidSnapshot(World world, ArcRegion region) {
        this.world = world;
        this.region = region;
        this.chunkData = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public void capture() {
        for (int chunkX = region.minChunkX(); chunkX <= region.maxChunkX(); chunkX++) {
            for (int chunkZ = region.minChunkZ(); chunkZ <= region.maxChunkZ(); chunkZ++) {
                ChunkCoord coord = new ChunkCoord(chunkX, chunkZ);
                char[] blocks = captureChunk(coord);
                chunkData.put(coord, blocks);
            }
        }
    }

    public void restore() {
        chunkData.forEach((coord, blocks) -> restoreChunk(coord, blocks));
    }

    private char[] captureChunk(ChunkCoord coord) {
        net.minecraft.world.level.chunk.ChunkAccess chunk =
                ((CraftWorld) world).getHandle().getChunk(coord.x(), coord.z());

        char[] blocks = new char[16 * 384 * 16]; // Entire chunk
        LevelChunkSection[] sections = chunk.getSections();

        for (int sectionY = 0; sectionY < sections.length; sectionY++) {
            LevelChunkSection section = sections[sectionY];
            if (section == null) continue;

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int worldY = (sectionY << 4) + y;
                        int index = (worldY << 8) | (z << 4) | x;
                        blocks[index] = (char) Block.getId(section.getBlockState(x, y, z));
                    }
                }
            }
        }
        return blocks;
    }

    private void restoreChunk(ChunkCoord coord, char[] blocks) {
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
                        int index = (worldY << 8) | (z << 4) | x;
                        char blockId = blocks[index];

                        if (blockId != 0) {
                            section.setBlockState(x, y, z,
                                    net.minecraft.world.level.block.Block.stateById(blockId), false);
                        }
                    }
                }
            }
        }
        chunk.markUnsaved();
        refreshChunk(coord);
    }

    private void refreshChunk(ChunkCoord coord) {
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