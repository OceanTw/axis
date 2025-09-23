package dev.ocean.axis.region;

import org.bukkit.Location;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class Selection {
    public abstract CompletableFuture<List<Location>> getBlocksAsync();

    public abstract Location getCenter();
}
