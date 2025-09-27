package dev.ocean.arc.commands;

import dev.ocean.arc.tools.ToolService;
import dev.ocean.arc.tools.menus.ToolboxMenu;
import dev.ocean.arc.utils.PlayerUtils;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command(name = "tools", aliases = {"toolbox"})
@Permission("arc.toolbox")
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
