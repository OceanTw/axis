package dev.ocean.api;

public final class ArcAPI {
    private ArcAPI() {};
    private final static String version = "0.3.2";
    private static volatile ArcToolBelt toolbelt;

    public static void _internalSetToolbelt(ArcToolBelt impl) { toolbelt = impl; }

    public static ArcToolBelt getToolBelt() {
        if (toolbelt == null) throw new IllegalStateException("Core isn't loaded");
        return toolbelt;
    }

}