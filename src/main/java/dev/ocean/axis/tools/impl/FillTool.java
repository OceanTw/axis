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
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FillTool extends Tool {
    public FillTool() {
        super("fill", "Fill Tool", "Fills your selection with blocks based on your settings!", Material.POWDER_SNOW_BUCKET);
    }

    SelectionService selection = SelectionService.get();

    @Override
    public boolean onLeftClick(@NonNull Player player, Location location, ToolSettings settings) {
        HistoryService.get().undo(player);
        PlayerUtils.sendInfo(player, "Undid 1 action");
        PlayerUtils.playSoundInfo(player);
        return true;
    }

    @Override
    public boolean onRightClick(@NonNull Player player, Location location, ToolSettings settings) {
        var selectionOptional = selection.getSelection(player.getUniqueId());
        if (selectionOptional == null) {
            PlayerUtils.sendError(player, "No selection found! Make a selection first.");
            PlayerUtils.playSoundError(player);
            return false;
        }

        Map<Material, Double> blockSettings = settings.getMaterialPercentages("blocks");
        if (blockSettings.isEmpty()) {
            PlayerUtils.sendError(player, "No fill materials configured!");
            PlayerUtils.playSoundError(player);
            return false;
        }

        boolean replaceAirOnly = settings.get("replace_air_only", false);

        CuboidSnapshot.create(
                selection.getPos1(player.getUniqueId()),
                selection.getPos2(player.getUniqueId())
        ).thenAccept(snapshot -> HistoryService.get().add(player, snapshot));

        selectionOptional.getBlocksAsync().thenAccept(locations -> {
            Map<Location, BlockData> blocks = new HashMap<>();
            int totalBlocks = locations.size();
            int processedBlocks = 0;

            for (Location loc : locations) {
                Block block = loc.getBlock();

                if (replaceAirOnly && !block.getType().isAir()) {
                    continue;
                }

                Material materialToUse = selectMaterialByPercentage(blockSettings);
                blocks.put(loc, materialToUse.createBlockData());
                processedBlocks++;
            }

            if (blocks.isEmpty()) {
                PlayerUtils.sendWarning(player, "No blocks were changed. Check your settings!");
                PlayerUtils.playSoundWarning(player);
                return;
            }

            long startTime = System.currentTimeMillis();
            PlayerUtils.sendInfo(player, "Filling " + blocks.size() + " blocks...");

            BlockChanger.setBlocks(blocks, true).thenAccept(success -> {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                PlayerUtils.sendMessage(player,
                        Component.text("§a§lSUCCESS! §rFilled §d" + blocks.size() + "§r blocks in §e" + duration + "ms"));
                PlayerUtils.playSoundSuccess(player);
            });
        });

        return true;
    }

    private Material selectMaterialByPercentage(Map<Material, Double> materialPercentages) {
        double random = ThreadLocalRandom.current().nextDouble() * 100.0;
        double cumulative = 0.0;

        for (Map.Entry<Material, Double> entry : materialPercentages.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                return entry.getKey();
            }
        }

        // Fallback to first material
        return materialPercentages.keySet().iterator().next();
    }

    @Override
    public boolean canUse(@NonNull Player player) {
        return player.hasPermission("axis.tools.fill") || player.isOp();
    }

    @Override
    public ToolSettings createDefaultSettings() {
        ToolSettings settings = new ToolSettings();
        settings.set("replace_air_only", false);

        Map<Material, Double> defaultBlocks = new HashMap<>();
        defaultBlocks.put(Material.STONE, 100.0);
        settings.setMaterialPercentages("blocks", defaultBlocks);

        return settings;
    }

    @Override
    public Set<String> getConfigurableSettings() {
        return Set.of("blocks", "replace_air_only");
    }

    @Override
    protected String getLeftClickDescription() {
        return "Undo last action";
    }

    @Override
    protected String getRightClickDescription() {
        return "Fill selection with configured blocks";
    }
}