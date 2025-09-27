package dev.ocean.api;

public final class ArcAPI {
    private ArcAPI() {}
    private static IArcAPI api;

    public static void _internalSetApi(IArcAPI impl) { api = impl; }

    public static IArcAPI getAPI() { return api; }
}
