package dev.ocean.arc.format;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import dev.ocean.arc.utils.world.ArcWorldEditor;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@UtilityClass
// TODO: API
public class ArcFormat {
    public void saveLocationToFile(Location pos1, Location pos2, File file, Location anchorLocation) throws IOException {
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

        Map<String, Integer> paletteMap = new LinkedHashMap<>();
        List<String> paletteList = new ArrayList<>();

        Map<Location, BlockData> regionBlocks = ArcWorldEditor.get().getBlocks(
                new Location(world, minX, minY, minZ),
                new Location(world, maxX, maxY, maxZ)
        ).join();

        List<BlockEntry> rawBlocks = new ArrayList<>();
        for (Map.Entry<Location, BlockData> entry : regionBlocks.entrySet()) {
            Location loc = entry.getKey();
            BlockData data = entry.getValue();
            String dataStr = compressBlockData(data.getAsString());
            int paletteIdx = paletteMap.computeIfAbsent(dataStr, s -> {
                paletteList.add(s);
                return paletteList.size() - 1;
            });
            int relX = loc.getBlockX() - minX;
            int relY = loc.getBlockY() - minY;
            int relZ = loc.getBlockZ() - minZ;
            rawBlocks.add(new BlockEntry((short) relX, (short) relY, (short) relZ, paletteIdx));
        }

        rawBlocks.sort(Comparator.comparingInt((BlockEntry b) -> b.relZ)
                .thenComparingInt(b -> b.relY)
                .thenComparingInt(b -> b.relX));

        List<RleBlockEntry> rleBlocks = applyRLE(rawBlocks);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new ZstdOutputStream(Files.newOutputStream(file.toPath()), 6)))) {
            out.writeUTF(world.getName());
            writeVarInt(out, minX);
            writeVarInt(out, minY);
            writeVarInt(out, minZ);
            writeVarInt(out, anchorRelX);
            writeVarInt(out, anchorRelY);
            writeVarInt(out, anchorRelZ);
            writeVarInt(out, paletteList.size());
            for (String s : paletteList) out.writeUTF(s);
            writeVarInt(out, rleBlocks.size());
            boolean useByte = paletteList.size() <= 256;
            for (RleBlockEntry entry : rleBlocks) {
                writeVarInt(out, entry.relX);
                writeVarInt(out, entry.relY);
                writeVarInt(out, entry.relZ);
                if (useByte) {
                    out.writeByte(entry.paletteIdx);
                } else {
                    writeVarInt(out, entry.paletteIdx);
                }
                writeVarInt(out, entry.count);
            }
        }
    }

    public void load(File file, Location anchorLocation) throws IOException {
        Objects.requireNonNull(file, "File cannot be null");
        Objects.requireNonNull(anchorLocation, "Anchor location cannot be null");

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new ZstdInputStream(Files.newInputStream(file.toPath()))))) {
            String worldName = in.readUTF();
            World world = Bukkit.getWorld(worldName);
            if (world == null) throw new IllegalStateException("World " + worldName + " not found!");
            int minX = readVarInt(in);
            int minY = readVarInt(in);
            int minZ = readVarInt(in);
            int anchorRelX = readVarInt(in);
            int anchorRelY = readVarInt(in);
            int anchorRelZ = readVarInt(in);
            int absOriginX = anchorLocation.getBlockX() - anchorRelX;
            int absOriginY = anchorLocation.getBlockY() - anchorRelY;
            int absOriginZ = anchorLocation.getBlockZ() - anchorRelZ;

            int paletteSize = readVarInt(in);
            if (paletteSize <= 0 || paletteSize > 100000) {
                throw new IOException("Invalid palette size: " + paletteSize);
            }

            String[] palette = new String[paletteSize];
            for (int i = 0; i < paletteSize; i++) {
                palette[i] = decompressBlockData(in.readUTF());
            }

            int blockCount = readVarInt(in);
            if (blockCount < 0) {
                throw new IOException("Invalid block count: " + blockCount);
            }

            boolean useByte = paletteSize <= 256;
            Map<Location, BlockData> blocks = new HashMap<>();

            for (int i = 0; i < blockCount; i++) {
                int relX = readVarInt(in);
                int relY = readVarInt(in);
                int relZ = readVarInt(in);
                int paletteIdx = useByte ? in.readUnsignedByte() : readVarInt(in);
                int count = readVarInt(in);

                if (paletteIdx < 0 || paletteIdx >= paletteSize) {
                    throw new IOException("Palette index out of bounds: " + paletteIdx + " (palette size: " + paletteSize + ")");
                }

                String dataStr = palette[paletteIdx];
                BlockData data = createBlockDataSafe(dataStr);

                for (int j = 0; j < count; j++) {
                    Location loc = new Location(world, absOriginX + relX + j, absOriginY + relY, absOriginZ + relZ);
                    blocks.put(loc, data);
                }
            }

            if (blocks.isEmpty()) return;
            dev.lrxh.blockChanger.BlockChanger.setBlocks(blocks, true).thenRun(() -> {});
        }
    }

    private List<RleBlockEntry> applyRLE(List<BlockEntry> blocks) {
        List<RleBlockEntry> rleBlocks = new ArrayList<>();
        if (blocks.isEmpty()) return rleBlocks;

        BlockEntry prev = blocks.get(0);
        int count = 1;

        for (int i = 1; i < blocks.size(); i++) {
            BlockEntry current = blocks.get(i);
            if (prev.relX + count == current.relX &&
                    prev.relY == current.relY &&
                    prev.relZ == current.relZ &&
                    prev.paletteIdx == current.paletteIdx) {
                count++;
            } else {
                rleBlocks.add(new RleBlockEntry(prev.relX, prev.relY, prev.relZ, prev.paletteIdx, count));
                prev = current;
                count = 1;
            }
        }
        rleBlocks.add(new RleBlockEntry(prev.relX, prev.relY, prev.relZ, prev.paletteIdx, count));
        return rleBlocks;
    }

    private void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            b = in.readByte();
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return value;
    }

    private String compressBlockData(String dataStr) {
        if (dataStr.startsWith("minecraft:")) {
            dataStr = dataStr.substring(10);
        }
        return dataStr.replace("[facing=north]", "")
                .replace("[level=0]", "")
                .replace("[waterlogged=false]", "")
                .replace("[lit=false]", "")
                .replace("[open=false]", "")
                .replace("[powered=false]", "");
    }

    private String decompressBlockData(String compressed) {
        if (!compressed.contains("[")) {
            return "minecraft:" + compressed;
        }
        return "minecraft:" + compressed;
    }

    private BlockData createBlockDataSafe(String dataStr) {
        try {
            return Bukkit.createBlockData(dataStr);
        } catch (IllegalArgumentException ex) {
            String fallback = dataStr.split("\\[", 2)[0];
            try {
                return Bukkit.createBlockData(fallback);
            } catch (IllegalArgumentException ex2) {
                return Bukkit.createBlockData("minecraft:air");
            }
        }
    }

    private record BlockEntry(short relX, short relY, short relZ, int paletteIdx) {}
    private record RleBlockEntry(short relX, short relY, short relZ, int paletteIdx, int count) {}
}