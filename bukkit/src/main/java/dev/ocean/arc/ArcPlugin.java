package dev.ocean.arc;

import dev.lrxh.blockChanger.BlockChanger;
import dev.ocean.api.Arc;
import dev.ocean.arc.api.ArcApiImpl;
import dev.ocean.arc.commands.ArcCommand;
import dev.ocean.arc.commands.BenchmarkCommand;
import dev.ocean.arc.commands.ToolCommand;
import dev.ocean.arc.tools.ToolListener;
import dev.ocean.arc.utils.menu.MenuListener;
import dev.ocean.arc.utils.world.ArcWorldEditor;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ArcPlugin extends JavaPlugin {

    @Getter
    private static ArcPlugin instance;
    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        instance = this;
        BlockChanger.initialize(this);
//        PacketEvents.getAPI().init();
//        APIConfig settings;
//        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);

//        settings = new APIConfig(PacketEvents.getAPI())
//                .debugMode()
//                .tickTickables()
//                .usePlatformLogger();

//        EntityLib.init(platform, settings);
        getServer().getPluginManager().registerEvents(new ToolListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        this.liteCommands = LiteBukkitFactory.builder("fallback-prefix", this)
                .commands(
                        new ToolCommand(),
                        new ArcCommand(),
                        new BenchmarkCommand()
                )
                .build();

        Arc._internalSetApi(new ArcApiImpl());
    }

    @Override
    public void onDisable() {
        this.liteCommands.unregister();
        ArcWorldEditor.get().shutdown();
    }
}
