package dev.ocean.axis.commands;

import dev.ocean.axis.tools.ToolService;
import dev.ocean.axis.tools.menus.ToolboxMenu;
import dev.ocean.axis.utils.PlayerUtils;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command(name = "tools", aliases = {"toolbox"})
@Permission("axis.toolbox")
public class ToolCommand {
    @Execute(name = "")
    public void toolbox(@Context Player sender, @OptionalArg String item) {
        if (item != null) {
            ToolService.get().getToolByName(item).ifPresentOrElse(tool -> {
                ItemStack is = tool.createItemStack();
                sender.getInventory().addItem(is);
                PlayerUtils.sendInfo(sender, "Item added to inventory: " + item);
            }, () -> PlayerUtils.sendError(sender, "Item not found"));
            return;
        }
        new ToolboxMenu().open(sender);
    }
}
