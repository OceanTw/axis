package dev.ocean.arc.utils.world.chunk;

import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.*;

public class SectionEditor {
    private final ServerLevel nmsWorld;
    @Getter
    private final ChunkAccess chunk;

    public SectionEditor(World world, ChunkCoord coord) {
        if (world == null) throw new IllegalArgumentException("World cannot be null");

        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.chunk = nmsWorld.getChunkSource().getChunk(coord.x(), coord.z(), true);

        if (this.chunk == null) {
            throw new IllegalStateException("Could not load chunk at " + coord.x() + "," + coord.z());
        }
    }

    public void applyChanges(ConcurrentChunk arcChunk) {
        if (arcChunk == null) throw new IllegalArgumentException("ArcChunk cannot be null");

        ChunkSection[] sections = arcChunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            ChunkSection section = sections[i];
            if (section != null && section.isModified()) {
                applySectionChanges(i, section);
            }
        }
        chunk.markUnsaved();
    }

    private void applySectionChanges(int sectionIndex, ChunkSection arcSection) {
        LevelChunkSection nmsSection = chunk.getSections()[sectionIndex];
        if (nmsSection == null) {
            Registry<Biome> biomeRegistry = nmsWorld.registryAccess().lookupOrThrow(Registries.BIOME);
            ChunkPos chunkPos = new ChunkPos(chunk.getPos().x, chunk.getPos().z);
            int sectionY = sectionIndex - 4;

            nmsSection = new LevelChunkSection(
                    biomeRegistry,
                    nmsWorld,
                    chunkPos,
                    sectionY
            );
            chunk.getSections()[sectionIndex] = nmsSection;
        }
        char[] blocks = arcSection.getBlocks();
        if (blocks != null) {
            for (int i = 0; i < blocks.length; i++) {
                if (blocks[i] != ChunkSection.AIR) {
                    int x = i & 15;
                    int y = (i >> 8) & 15;
                    int z = (i >> 4) & 15;

                    try {
                        BlockState state =
                                net.minecraft.world.level.block.Block.stateById(blocks[i]);
                        if (state != null) {
                            nmsSection.setBlockState(x, y, z, state, false);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void refreshChunksForPlayers(Collection<ChunkCoord> coords) {
        Map<ServerPlayer, List<ChunkPos>> playerChunks = new HashMap<>();

        for (ChunkCoord coord : coords) {
            ChunkPos chunkPos = new ChunkPos(coord.x(), coord.z());
            nmsWorld.getChunkSource().chunkMap.getPlayers(chunkPos, false).forEach(player -> {
                playerChunks.computeIfAbsent(player, k -> new ArrayList<>()).add(chunkPos);
            });
        }

        playerChunks.forEach((player, chunkList) -> {
            for (ChunkPos chunkPos : chunkList) {
                ChunkAccess chunk = nmsWorld.getChunk(chunkPos.x, chunkPos.z);
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(
                        nmsWorld.getChunk(chunkPos.x, chunkPos.z), nmsWorld.getChunkSource().getLightEngine(), null, null, true
                ));
            }
        });
    }
}