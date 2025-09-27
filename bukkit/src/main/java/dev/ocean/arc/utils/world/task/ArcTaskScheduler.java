package dev.ocean.arc.utils.world.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ArcTaskScheduler {
    private static final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static <T> CompletableFuture<T> submitAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executor);
    }

    public static CompletableFuture<Void> submitAsync(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}