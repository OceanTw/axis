package dev.ocean.api;

public final class Arc {
    private Arc() {}
    private static ArcApi api;

    public static void _internalSetApi(ArcApi impl) { api = impl; }

    public static ArcApi getAPI() { return api; }
}
