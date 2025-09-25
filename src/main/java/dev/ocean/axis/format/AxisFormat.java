package dev.ocean.axis.format;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import dev.ocean.axis.utils.WorldUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@UtilityClass
public class AxisFormat {

    public void save(Location pos1, Location pos2, File file, Location anchorLocation) throws IOException {
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
        List<BlockEntry> blocks = new ArrayList<>();

        Map<Location, BlockData> regionBlocks = WorldUtils.getBlocksAsync(
                new Location(world, minX, minY, minZ),
                new Location(world, maxX, maxY, maxZ)
        ).join();

        for (Map.Entry<Location, BlockData> entry : regionBlocks.entrySet()) {
            Location loc = entry.getKey();
            BlockData data = entry.getValue();
            String dataStr = data.getAsString();
            int paletteIdx = paletteMap.computeIfAbsent(dataStr, s -> {
                paletteList.add(s);
                return paletteList.size() - 1;
            });
            int relX = loc.getBlockX() - minX;
            int relY = loc.getBlockY() - minY;
            int relZ = loc.getBlockZ() - minZ;
            blocks.add(new BlockEntry((short) relX, (short) relY, (short) relZ, paletteIdx));
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

    public void load(File file, Location anchorLocation) throws IOException {
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

    private record BlockEntry(short relX, short relY, short relZ, int paletteIdx) {
    }
}