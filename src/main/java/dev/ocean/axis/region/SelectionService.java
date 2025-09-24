package dev.ocean.axis.region;

import dev.ocean.axis.region.impl.CubeSelection;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionService {

    private static SelectionService instance;

    public static SelectionService get() {
        if (instance == null) {
            instance = new SelectionService();
        }
        return instance;
    }

    private final ConcurrentHashMap<UUID, Selection> playerSelections = new ConcurrentHashMap<>();

    public void setSelection(UUID playerUUID, Selection selection) {
        playerSelections.put(playerUUID, selection);
    }

    public Selection getSelection(UUID playerUUID) {
        return playerSelections.get(playerUUID);
    }

    public void clear(UUID playerUUID) {
        playerSelections.remove(playerUUID);
    }

    // Cube-specific helpers
    public void setPos1(UUID playerUUID, Location location) {
        Selection sel = playerSelections.computeIfAbsent(playerUUID, id -> new CubeSelection());
        if (sel instanceof CubeSelection cube) {
            cube.setPos1(location);
            if (cube.getPos1() != null && cube.getPos2() != null) {
                // TODO: Selection Preview
            }
        }
    }

    public void setPos2(UUID playerUUID, Location location) {
        Selection sel = playerSelections.computeIfAbsent(playerUUID, id -> new CubeSelection());
        if (sel instanceof CubeSelection cube) {
            cube.setPos2(location);
        }
    }

    public Location getPos1(UUID playerUUID) {
        Selection sel = playerSelections.get(playerUUID);
        return sel instanceof CubeSelection cube ? cube.getPos1() : null;
    }

    public Location getPos2(UUID playerUUID) {
        Selection sel = playerSelections.get(playerUUID);
        return sel instanceof CubeSelection cube ? cube.getPos2() : null;
    }
}
