package dev.ocean.axis.tools.impl;

import dev.lrxh.blockChanger.BlockChanger;
import dev.lrxh.blockChanger.snapshot.CuboidSnapshot;
import dev.ocean.axis.history.HistoryService;
import dev.ocean.axis.region.SelectionService;
import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.utils.PlayerUtils;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FillTool extends Tool {
    public FillTool() {
        super("fill", "Fill Tool", "Fills your selection with a specific block!", Material.POWDER_SNOW_BUCKET);
    }

    SelectionService selection = SelectionService.get();

    @Override
    public boolean onLeftClick(@NonNull Player player, @NonNull Location location, ToolSettings settings) {
        // TODO: open settings menu
        return false;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, @NonNull Location location, ToolSettings settings) {
        if (!PlayerUtils.isLookingAtSelection(player)) return false;

        var selectionOptional = selection.getSelection(player.getUniqueId());
        if (selectionOptional == null) return false;

        CuboidSnapshot.create(
                selection.getPos1(player.getUniqueId()),
                selection.getPos2(player.getUniqueId())
        ).thenAccept(snapshot -> HistoryService.get().add(snapshot));

        selectionOptional.getBlocksAsync().thenAccept(locations -> {
            Map<Location, BlockData> blocks = new HashMap<>();
            for (Location loc : locations) {
                blocks.put(loc, Material.STONE.createBlockData());
            }

            long startTime = System.currentTimeMillis();

            BlockChanger.setBlocks(blocks, true).thenAccept(success -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                PlayerUtils.sendMessage(player,
                        Component.text("§a§lSUCCESS! §rTook §e" + duration + "ms §rto fill §d" + blocks.size() + "§r blocks!"));
            });
        });

        return true;
    }


    @Override
    public boolean canUse(@NonNull Player player) {
        return true;
    }

    @Override
    public ToolSettings createDefaultSettings() {
        return null;
    }
}
