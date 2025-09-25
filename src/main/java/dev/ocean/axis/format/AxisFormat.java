package dev.ocean.axis.format;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.block.state.BlockState;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import com.github.luben.zstd.ZstdOutputStream;
import com.github.luben.zstd.ZstdInputStream;

public final class AxisFormat {

    private AxisFormat() {}

    public static void save(Location pos1, Location pos2, File file, Location anchorLocation) throws IOException {
        Objects.requireNonNull(pos1, "Position 1 cannot be null");
        Objects.requireNonNull(pos2, "Position 2 cannot be null");
        Objects.requireNonNull(anchorLocation, "Anchor location cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        World world = pos1.getWorld();
        if (world == null || pos2.getWorld() == null || anchorLocation.getWorld() == null) {
            throw new IllegalArgumentException("All locations must have a world");
        }
        if (!world.getUID().equals(pos2.getWorld().getUID()) || !world.getUID().equals(anchorLocation.getWorld().getUID())) {
            throw new IllegalArgumentException("All locations must be in the same world");
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int anchorRelX = anchorLocation.getBlockX() - minX;
        int anchorRelY = anchorLocation.getBlockY() - minY;
        int anchorRelZ = anchorLocation.getBlockZ() - minZ;

        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();

        Map<String, Integer> paletteMap = new LinkedHashMap<>();
        List<String> paletteList = new ArrayList<>();
        List<BlockEntry> blocks = new ArrayList<>();

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

                                String dataStr;
                                try {
                                    CraftBlockData cbd = CraftBlockData.fromData(nmsState);
                                    dataStr = cbd.getAsString();
                                } catch (Exception ex) {
                                    dataStr = nmsState.toString();
                                }

                                int paletteIdx = paletteMap.computeIfAbsent(dataStr, s -> {
                                    paletteList.add(s);
                                    return paletteList.size() - 1;
                                });

                                int relX = x - minX;
                                int relY = y - minY;
                                int relZ = z - minZ;

                                blocks.add(new BlockEntry((short) relX, (short) relY, (short) relZ, paletteIdx));
                            }
                        }
                    }
                }
            }
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new ZstdOutputStream(Files.newOutputStream(file.toPath()))))) {
            out.writeUTF(world.getName());
            out.writeInt(minX);
            out.writeInt(minY);
            out.writeInt(minZ);

            out.writeInt(anchorRelX);
            out.writeInt(anchorRelY);
            out.writeInt(anchorRelZ);

            out.writeInt(paletteList.size());
            for (String s : paletteList) out.writeUTF(s);

            out.writeInt(blocks.size());
            boolean useByte = paletteList.size() <= 256;
            for (BlockEntry entry : blocks) {
                out.writeShort(entry.relX);
                out.writeShort(entry.relY);
                out.writeShort(entry.relZ);
                if (useByte) {
                    out.writeByte(entry.paletteIdx);
                } else {
                    out.writeShort(entry.paletteIdx);
                }
            }
        }
    }

    public static void load(File file, Location anchorLocation) throws IOException {
        Objects.requireNonNull(file, "File cannot be null");
        Objects.requireNonNull(anchorLocation, "Anchor location cannot be null");

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new ZstdInputStream(Files.newInputStream(file.toPath()))))) {
            String worldName = in.readUTF();
            World world = Bukkit.getWorld(worldName);
            if (world == null) throw new IllegalStateException("World " + worldName + " not found!");

            int minX = in.readInt();
            int minY = in.readInt();
            int minZ = in.readInt();

            int anchorRelX = in.readInt();
            int anchorRelY = in.readInt();
            int anchorRelZ = in.readInt();

            int absOriginX = anchorLocation.getBlockX() - anchorRelX;
            int absOriginY = anchorLocation.getBlockY() - anchorRelY;
            int absOriginZ = anchorLocation.getBlockZ() - anchorRelZ;

            int paletteSize = in.readInt();
            String[] palette = new String[paletteSize];
            for (int i = 0; i < paletteSize; i++) palette[i] = in.readUTF();

            boolean useByte = paletteSize <= 256;

            int blockCount = in.readInt();
            Map<Location, BlockData> blocks = new HashMap<>(Math.max(4, blockCount));

            for (int i = 0; i < blockCount; i++) {
                short relX = in.readShort();
                short relY = in.readShort();
                short relZ = in.readShort();
                int paletteIdx = useByte ? in.readUnsignedByte() : in.readUnsignedShort();

                String dataStr = palette[paletteIdx];
                BlockData data;
                try {
                    data = Bukkit.createBlockData(dataStr);
                } catch (IllegalArgumentException ex) {
                    String fallback = dataStr.split("\\[", 2)[0];
                    try {
                        data = Bukkit.createBlockData(fallback);
                    } catch (IllegalArgumentException ex2) {
                        data = Bukkit.createBlockData("minecraft:air");
                    }
                }

                Location loc = new Location(world, absOriginX + relX, absOriginY + relY, absOriginZ + relZ);
                blocks.put(loc, data);
            }

            if (blocks.isEmpty()) {
                return;
            }

            dev.lrxh.blockChanger.BlockChanger.setBlocks(blocks, true).thenRun(() -> {});
        }
    }

    private static class BlockEntry {
        final short relX, relY, relZ;
        final int paletteIdx;

        BlockEntry(short relX, short relY, short relZ, int paletteIdx) {
            this.relX = relX;
            this.relY = relY;
            this.relZ = relZ;
            this.paletteIdx = paletteIdx;
        }
    }
}