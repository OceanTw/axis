package dev.ocean.axis.commands;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import dev.ocean.axis.tools.ToolService;
import dev.ocean.axis.utils.PlayerUtils;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.display.BlockDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

@Command(name = "axistest")
@Permission("axis.toolbox")
public class TestCommand {
    @Execute(name = "blockdisplay")
    public void getItem(@Context Player sender) {
        WrapperEntity wrapperEntity = new WrapperEntity(new Random().nextInt(), EntityTypes.BLOCK_DISPLAY);
        ((BlockDisplayMeta) wrapperEntity.getEntityMeta()).setBlockId(1);
        wrapperEntity.spawn(SpigotConversionUtil.fromBukkitLocation(sender.getLocation()));
        wrapperEntity.addViewer(sender.getUniqueId());
        PacketEvents.getAPI().getPlayerManager().sendPacket(sender, wrapperEntity.getEntityMeta().createPacket());
    }
}
