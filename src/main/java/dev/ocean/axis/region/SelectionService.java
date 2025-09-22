package dev.ocean.axis.region;

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

    public void setPos1(UUID playerUUID, Location location) {
        if (getSelection(playerUUID) == null) {
            playerSelections.put(playerUUID, new Selection());
        }
        getSelection(playerUUID).setPos1(location);
    }

    public void setPos2(UUID playerUUID, Location location) {
        if (getSelection(playerUUID) == null) {
            playerSelections.put(playerUUID, new Selection());
        }
        getSelection(playerUUID).setPos2(location);
    }

    public Location getPos1(UUID playerUUID) {
        return getSelection(playerUUID).getPos1();
    }

    public Location getPos2(UUID playerUUID) {
        return getSelection(playerUUID).getPos2();
    }

    public void clear(UUID playerUUID) {
        playerSelections.remove(playerUUID);
    }


    public Selection getSelection(UUID playerUUID) {
        return playerSelections.get(playerUUID);
    }
}
