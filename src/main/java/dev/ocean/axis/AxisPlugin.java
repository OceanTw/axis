package dev.ocean.axis;

import com.github.retrooper.packetevents.PacketEvents;
import dev.lrxh.blockChanger.BlockChanger;
import dev.ocean.axis.commands.AxisCommand;
import dev.ocean.axis.commands.TestCommand;
import dev.ocean.axis.commands.ToolCommand;
import dev.ocean.axis.tools.ToolListener;
import dev.ocean.axis.utils.menu.MenuListener;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AxisPlugin extends JavaPlugin {

    @Getter
    private static AxisPlugin instance;
    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        instance = this;
        BlockChanger.initialize(this);
        PacketEvents.getAPI().init();
        APIConfig settings;
        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);

        settings = new APIConfig(PacketEvents.getAPI())
                .debugMode()
                .tickTickables()
                .usePlatformLogger();

        EntityLib.init(platform, settings);
        getServer().getPluginManager().registerEvents(new ToolListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        this.liteCommands = LiteBukkitFactory.builder("fallback-prefix", this)
                .commands(
                        new ToolCommand(),
                        new TestCommand(),
                        new AxisCommand()
                )
                .build();
    }

    @Override
    public void onDisable() {

    }
}
