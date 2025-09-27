package dev.ocean.arc.history;

import dev.lrxh.blockChanger.snapshot.CuboidSnapshot;
import org.bukkit.entity.Player;

import java.util.*;

// TODO: API
public class HistoryService {
    private static HistoryService instance;

    public static HistoryService get() {
        if (instance == null) {
            instance = new HistoryService();
        }
        return instance;
    }

    private final Map<UUID, Deque<CuboidSnapshot>> histories = new HashMap<>();

    private Deque<CuboidSnapshot> getHistory(UUID playerId) {
        return histories.computeIfAbsent(playerId, id -> new ArrayDeque<>());
    }

    public void add(Player player, CuboidSnapshot action) {
        getHistory(player.getUniqueId()).push(action.clone());
    }

    public List<CuboidSnapshot> getHistory(Player player) {
        return new ArrayList<>(getHistory(player.getUniqueId()));
    }

    public CuboidSnapshot undo(Player player) {
        Deque<CuboidSnapshot> history = getHistory(player.getUniqueId());
        return history.isEmpty() ? null : history.pop();
    }

    public void clear(Player player) {
        histories.remove(player.getUniqueId());
    }
}
