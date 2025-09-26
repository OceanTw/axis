package dev.ocean.axis.commands;

import dev.ocean.axis.AxisPlugin;
import dev.ocean.axis.format.AxisFormat;
import dev.ocean.axis.region.SelectionService;
import dev.ocean.axis.utils.PlayerUtils;
import dev.ocean.axis.utils.WorldUtils;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;

@Command(name = "axis")
@Permission("axis.use")
public class AxisCommand {

    SelectionService selection = SelectionService.get();

    @Execute(name = "getblock")
    public void getBlock(@Context Player sender, @Arg int x, @Arg int y, @Arg int z) {
        long start = System.currentTimeMillis();
        WorldUtils.getBlockAsync(new Location(sender.getWorld(), x, y, z)).thenAccept(blockData -> {
            PlayerUtils.sendInfo(sender, blockData.getMaterial().name());
            long end = System.currentTimeMillis();
            PlayerUtils.sendInfo(sender, "Took " + (end - start) + "ms");
        });
    }

    @Execute(name = "getblock_bukkit")
    public void getBlockBukkit(@Context Player sender, @Arg int x, @Arg int y, @Arg int z) {
        long start = System.currentTimeMillis();
        Location location = new Location(sender.getWorld(), x, y, z);
        PlayerUtils.sendInfo(sender, location.getBlock().getType().name());
        PlayerUtils.sendInfo(sender, sender.getWorld().getBlockAt(location).getType().name());
        long end = System.currentTimeMillis();
        PlayerUtils.sendInfo(sender, "Took " + (end - start) + "ms");
    }

    @Execute(name = "save")
    public void save(@Context Player sender, @Arg String name) {
        if (selection.getPos1(sender.getUniqueId()) == null || selection.getPos2(sender.getUniqueId()) == null) {
            PlayerUtils.sendError(sender, "No selection found! Make a selection first.");
            return;
        }
        Location pos1 = selection.getPos1(sender.getUniqueId());
        Location pos2 = selection.getPos2(sender.getUniqueId());

        File schematicsDir = new File(AxisPlugin.getInstance().getDataFolder(), "schematics");
        if (!schematicsDir.exists() && !schematicsDir.mkdirs()) {
            PlayerUtils.sendError(sender, "Failed to create schematics folder");
            return;
        }

        File outFile = new File(schematicsDir, name + ".axis");
        try {
            AxisFormat.save(pos1, pos2, outFile, sender.getLocation());
            PlayerUtils.sendInfo(sender, "Saved selection to " + name + ".axis");
        } catch (Exception e) {
            e.printStackTrace();
            PlayerUtils.sendError(sender, "Failed to save schematic! Check console for more information.");
        }
    }

    @Execute(name = "load")
    public void load(@Context Player sender, @Arg String name) {
        File schematicsDir = new File(AxisPlugin.getInstance().getDataFolder(), "schematics");
        File file = new File(schematicsDir, name + ".axis");
        if (!file.exists()) {
            PlayerUtils.sendError(sender, "File not found!");
            return;
        }
        try {
            AxisFormat.load(file, sender.getLocation());
            PlayerUtils.sendInfo(sender, "Loaded " + name + ".axis");
        } catch (Exception e) {
            e.printStackTrace();
            PlayerUtils.sendError(sender, "Failed to load schematic! Check console for more information.");
        }
    }
}