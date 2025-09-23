package dev.ocean.axis.visuals;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;
import dev.ocean.axis.region.Selection;
import dev.ocean.axis.region.SelectionService;
import dev.ocean.axis.region.impl.CubeSelection;
import lombok.experimental.UtilityClass;
import me.tofaa.entitylib.meta.display.BlockDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class SelectionRenderer {
    private final SelectionService selection = SelectionService.get();
    private final Map<UUID, List<Integer>> activeSelections = new HashMap<>();
    private final AtomicInteger entityIdCounter = new AtomicInteger(100000); // Always unique for session

    public void startRender(Player player) {
        Selection sel = selection.getSelection(player.getUniqueId());
        if (sel == null) {
            Bukkit.getLogger().info("[SelectionRenderer] No selection found for player " + player.getName());
            return;
        }

        if (sel instanceof CubeSelection cube) {
            List<Integer> spawnedEntities = new ArrayList<>();
            List<Location> vertices = cube.getVertices();

            int[][] edges = {
                    {0, 1}, {0, 2}, {0, 4},
                    {1, 3}, {1, 5},
                    {2, 3}, {2, 6},
                    {3, 7},
                    {4, 5}, {4, 6},
                    {5, 7},
                    {6, 7}
            };

            Bukkit.getLogger().info("[SelectionRenderer] Rendering selection for player " + player.getName() + ". Vertices: " + vertices.size());

            for (int i = 0; i < edges.length; i++) {
                int[] edge = edges[i];
                Location a = vertices.get(edge[0]);
                Location b = vertices.get(edge[1]);

                Vector dir = b.toVector().subtract(a.toVector());
                double dirLen = dir.length();

                // Guard: skip degenerate edges
                if (dirLen < 0.01) {
                    Bukkit.getLogger().info("[SelectionRenderer] Skipping edge " + Arrays.toString(edge) + " due to zero length.");
                    continue;
                }

                Vector midVec = a.toVector().add(dir.clone().multiply(0.5));

                int entityId = entityIdCounter.getAndIncrement();

                Bukkit.getLogger().info("[SelectionRenderer] Edge " + i + ": " + Arrays.toString(edge) +
                        " a=" + a + ", b=" + b +
                        ", mid=" + midVec +
                        ", dir=" + dir + ", len=" + dirLen +
                        ", entityId=" + entityId);

                WrapperEntity entity = new WrapperEntity(entityId, EntityTypes.BLOCK_DISPLAY);
                ((BlockDisplayMeta) entity.getEntityMeta()).setBlockId(1);

                // Midpoint in world space
                Vector3f mid = new Vector3f(
                        (float) midVec.getX(),
                        (float) midVec.getY(),
                        (float) midVec.getZ()
                );

                // Length along edge
                float length = (float) dir.length();

                Bukkit.getLogger().info("[SelectionRenderer] Edge " + i +
                        " | mid=" + mid +
                        " | length=" + length);

                ((BlockDisplayMeta) entity.getEntityMeta()).setTranslation(mid);

                // Try hardcoded scale on first edge
                Vector3f scale = (i == 0) ? new Vector3f(1f, 0.1f, 0.1f) : new Vector3f(length, 0.1f, 0.1f);
                Bukkit.getLogger().info("[SelectionRenderer] Edge " + i +
                        " | scale=" + scale);
                ((BlockDisplayMeta) entity.getEntityMeta()).setScale(scale);

                // Rotation: align X axis to edge direction
                Vector from = new Vector(1f, 0f, 0f);
                Vector to = dir.clone().normalize();

                Quaternion4f rotation = null;
                try {
                    rotation = dev.ocean.axis.utils.MathUtils.fromToRotation(from, to);
                    if (rotation != null) {
                        Bukkit.getLogger().info("[SelectionRenderer] Edge " + i +
                                " | Quaternion: x=" + rotation.getX() + " y=" + rotation.getY() + " z=" + rotation.getZ() + " w=" + rotation.getW());
                        ((BlockDisplayMeta) entity.getEntityMeta()).setLeftRotation(rotation);
                        ((BlockDisplayMeta) entity.getEntityMeta()).setRightRotation(rotation);
                    } else {
                        Bukkit.getLogger().info("[SelectionRenderer] Edge " + i + " | Quaternion is null; skipping rotation");
                    }
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("[SelectionRenderer] Edge " + i +
                            " | Quaternion calculation failed: " + ex.getMessage());
                }

                entity.addViewer(player.getUniqueId());

                // Spawn at midpoint
                com.github.retrooper.packetevents.protocol.world.Location loc =
                        new com.github.retrooper.packetevents.protocol.world.Location(
                                midVec.getX(), midVec.getY(), midVec.getZ(), 0f, 0f
                        );

                Bukkit.getLogger().info("[SelectionRenderer] Edge " + i + " | Spawning at " + loc);

                entity.spawn(loc);

                // Always send metadata after spawn
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, entity.getEntityMeta().createPacket());

                spawnedEntities.add(entityId);
            }

            activeSelections.put(player.getUniqueId(), spawnedEntities);
            Bukkit.getLogger().info("[SelectionRenderer] Spawned entities for player " + player.getName() + ": " + spawnedEntities);
        }
    }

    public void stopRender(Player player) {
        List<Integer> entities = activeSelections.remove(player.getUniqueId());
        if (entities == null || entities.isEmpty()) {
            Bukkit.getLogger().info("[SelectionRenderer] No entities to destroy for player " + player.getName());
            return;
        }

        int[] ids = entities.stream().mapToInt(Integer::intValue).toArray();
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(ids);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);

        Bukkit.getLogger().info("[SelectionRenderer] Destroyed entities for player " + player.getName() + ": " + Arrays.toString(ids));
    }
}