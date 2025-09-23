package dev.ocean.axis.visuals;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;
import dev.ocean.axis.region.Selection;
import dev.ocean.axis.region.SelectionService;
import dev.ocean.axis.region.impl.CubeSelection;
import lombok.experimental.UtilityClass;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.display.BlockDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;

import java.util.*;

@UtilityClass
public class SelectionRenderer {
    private final SelectionService selection = SelectionService.get();
    private final Map<UUID, List<UUID>> activeSelections = new HashMap<>();

    public void startRender(Player player) {
        Selection sel = selection.getSelection(player.getUniqueId());
        if (sel == null) return;

        if (sel instanceof CubeSelection cube) {
            List<UUID> spawnedEntities = new ArrayList<>();
            List<Location> vertices = cube.getVertices();

            int[][] edges = {
                    {0,1}, {0,2}, {0,4},
                    {1,3}, {1,5},
                    {2,3}, {2,6},
                    {3,7},
                    {4,5}, {4,6},
                    {5,7},
                    {6,7}
            };

            for (int[] edge : edges) {
                Location a = vertices.get(edge[0]);
                Location b = vertices.get(edge[1]);

                Vector dir = b.toVector().subtract(a.toVector());
                Vector midVec = a.toVector().add(dir.clone().multiply(0.5));

                int entityId = new Random().nextInt();
                UUID entityUuid = UUID.randomUUID();

                WrapperEntity entity = new WrapperEntity(entityId, entityUuid, com.github.retrooper.packetevents.protocol.entity.type.EntityTypes.BLOCK_DISPLAY);
                EntityMeta meta = entity.getEntityMeta();
                BlockDisplayMeta blockDisplayMeta = (BlockDisplayMeta) meta;
                blockDisplayMeta.setBlockId(Material.BLACK_CONCRETE.getId());

                Vector3f start = new Vector3f((float)a.getX(), (float)a.getY(), (float)a.getZ());
                Vector3f end   = new Vector3f((float)b.getX(), (float)b.getY(), (float)b.getZ());
                Vector3f dir3f = new Vector3f(end.getX() - start.getX(), end.getY() - start.getY(), end.getZ() - start.getZ());

                Vector3f mid = new Vector3f(
                        (start.getX() + end.getX()) / 2f,
                        (start.getY() + end.getY()) / 2f,
                        (start.getZ() + end.getZ()) / 2f
                );
                float length = (float) Math.sqrt(dir3f.getX() * dir3f.getX()
                        + dir3f.getY() * dir3f.getY()
                        + dir3f.getZ() * dir3f.getZ());


                blockDisplayMeta.setTranslation(mid);
                blockDisplayMeta.setScale(new Vector3f(length, 0.1f, 0.1f));

                Vector from = new Vector(1f, 0f, 0f);
                Vector to   = dir.clone().normalize();
                Quaternion4f rotation = dev.ocean.axis.utils.MathUtils.fromToRotation(from, to);

                blockDisplayMeta.setLeftRotation(rotation);
                blockDisplayMeta.setRightRotation(rotation);

                entity.addViewer(player.getUniqueId());
                entity.spawn(new com.github.retrooper.packetevents.protocol.world.Location(midVec.getX(), midVec.getY(), midVec.getZ(), 0f, 0f));

                WrapperPlayServerEntityMetadata metaPacket = meta.createPacket();
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, metaPacket);

                spawnedEntities.add(entityUuid);
            }

            activeSelections.put(player.getUniqueId(), spawnedEntities);
        }
    }

    public void stopRender(Player player) {
        List<UUID> entities = activeSelections.remove(player.getUniqueId());
        if (entities == null || entities.isEmpty()) return;

        int[] ids = entities.stream().mapToInt(UUID::hashCode).toArray();
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(ids);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);
    }
}
