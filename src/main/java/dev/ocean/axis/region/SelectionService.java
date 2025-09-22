package dev.ocean.axis.region;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionService {
    private final ConcurrentHashMap<UUID, Selection> playerSelections = new ConcurrentHashMap<>();

    public void setSelection(UUID playerUUID, Selection selection) {
        playerSelections.put(playerUUID, selection);
    }

    public Selection getSelection(UUID playerUUID) {
        return playerSelections.get(playerUUID);
    }
}
