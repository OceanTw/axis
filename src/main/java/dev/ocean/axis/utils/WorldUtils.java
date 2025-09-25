package dev.ocean.axis.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class WorldUtils {
    public CompletableFuture<Map<Location, BlockData>> getBlocksAsync(Location pos1, Location pos2) {
        return CompletableFuture.supplyAsync(() -> {
            World world = pos1.getWorld();
            int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
            int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
            int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
            ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
            Map<Location, BlockData> blocks = new HashMap<>();
            int chunkMinX = minX >> 4;
            int chunkMaxX = maxX >> 4;
            int chunkMinZ = minZ >> 4;
            int chunkMaxZ = maxZ >> 4;
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                    Chunk bukkitChunk = world.getChunkAt(chunkX, chunkZ);
                    ChunkAccess chunk = ((CraftChunk) bukkitChunk).getHandle(ChunkStatus.FULL);
                    LevelChunkSection[] sections = chunk.getSections();
                    int sectionCount = sections.length;
                    for (int sectionIdx = 0; sectionIdx < sectionCount; sectionIdx++) {
                        LevelChunkSection section = sections[sectionIdx];
                        if (section == null) continue;
                        int sectionBaseY = nmsWorld.getSectionYFromSectionIndex(sectionIdx) << 4;
                        for (int lx = 0; lx < 16; lx++) {
                            int x = (chunkX << 4) + lx;
                            if (x < minX || x > maxX) continue;
                            for (int lz = 0; lz < 16; lz++) {
                                int z = (chunkZ << 4) + lz;
                                if (z < minZ || z > maxZ) continue;
                                for (int ly = 0; ly < 16; ly++) {
                                    int y = sectionBaseY + ly;
                                    if (y < minY || y > maxY) continue;
                                    BlockState nmsState = section.getBlockState(lx, ly, lz);
                                    if (nmsState == null || nmsState.isAir()) continue;
                                    BlockData data;
                                    try {
                                        data = CraftBlockData.fromData(nmsState);
                                    } catch (Exception ex) {
                                        continue;
                                    }
                                    Location loc = new Location(world, x, y, z);
                                    blocks.put(loc, data);
                                }
                            }
                        }
                    }
                }
            }
            return blocks;
        });
    }

    public CompletableFuture<BlockData> getBlockAsync(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            World world = location.getWorld();
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            Chunk bukkitChunk = world.getChunkAt(chunkX, chunkZ);
            ChunkAccess chunk = ((CraftChunk) bukkitChunk).getHandle(ChunkStatus.FULL);
            LevelChunkSection[] sections = chunk.getSections();
            int sectionIdx = nmsWorld.getSectionIndex(y);
            if (sectionIdx < 0 || sectionIdx >= sections.length) return null;
            LevelChunkSection section = sections[sectionIdx];
            if (section == null) return null;
            int lx = x & 15;
            int ly = y & 15;
            int lz = z & 15;
            BlockState nmsState = section.getBlockState(lx, ly, lz);
            if (nmsState == null || nmsState.isAir()) return null;
            try {
                return CraftBlockData.fromData(nmsState);
            } catch (Exception ex) {
                return null;
            }
        });
    }
}