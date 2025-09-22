package dev.ocean.axis;

import dev.lrxh.blockChanger.BlockChanger;
import dev.ocean.axis.commands.ToolCommand;
import dev.ocean.axis.tools.ToolListener;
import dev.ocean.axis.tools.ToolService;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
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

        getServer().getPluginManager().registerEvents(new ToolListener(), this);

        this.liteCommands = LiteBukkitFactory.builder("fallback-prefix", this)
                .commands(
                        new ToolCommand()
                )
                .build();
    }

    @Override
    public void onDisable() {

    }
}
