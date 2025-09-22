package dev.ocean.axis.history;

import dev.lrxh.blockChanger.snapshot.CuboidSnapshot;

import java.util.ArrayDeque;
import java.util.Deque;

public class HistoryService {
    private static HistoryService instance;

    public static HistoryService get() {
        if (instance == null) {
            instance = new HistoryService();
        }
        return instance;
    }


    private Deque<CuboidSnapshot> history = new ArrayDeque<>();

    public void add(CuboidSnapshot action) {
        history.push(action);
    }

    public CuboidSnapshot undo() {
        return history.isEmpty() ? null : history.pop();
    }
}
