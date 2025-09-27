package dev.ocean.axis.tools.impl;

import dev.ocean.axis.history.HistoryService;
import dev.ocean.axis.region.SelectionService;
import dev.ocean.axis.tools.Tool;
import dev.ocean.axis.tools.ToolSettings;
import dev.ocean.axis.tools.patterns.MaterialPercentagePattern;
import dev.ocean.axis.utils.PlayerUtils;
import dev.ocean.axis.utils.world.AxisWorldEditor;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FillTool extends Tool {
    public FillTool() {
        super("fill", "Fill Tool", "Fills your selection with blocks based on your settings!", Material.POWDER_SNOW_BUCKET);
    }

    private final SelectionService selection = SelectionService.get();
    private final AxisWorldEditor worldEditor = AxisWorldEditor.get();

    @Override
    public boolean onLeftClick(@NonNull Player player, Location location, ToolSettings settings) {
        if (HistoryService.get().getHistory(player).isEmpty()) {
            PlayerUtils.sendActionBar(player, "No actions to undo!");
            PlayerUtils.playSoundError(player);
            return true;
        }

        HistoryService.get().undo(player).restore(true);
        PlayerUtils.sendActionBar(player, "Undid 1 action");
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

        Location pos1 = selection.getPos1(player.getUniqueId());
        Location pos2 = selection.getPos2(player.getUniqueId());

        MaterialPercentagePattern pattern = new MaterialPercentagePattern(blockSettings, replaceAirOnly, player.getWorld());

        long startTime = System.currentTimeMillis();
        PlayerUtils.sendInfo(player, "Filling selection with material percentages...");

        worldEditor.fill(pos1, pos2, pattern).thenAccept(blocksChanged -> {
            long duration = System.currentTimeMillis() - startTime;
            PlayerUtils.sendMessage(player,
                    Component.text("§a§lSUCCESS! §rFilled §d" + blocksChanged + "§r blocks in §e" + duration + "ms"));
            PlayerUtils.playSoundSuccess(player);

            worldEditor.save(player.getWorld()).thenRun(() -> {
                PlayerUtils.sendActionBar(player, "Changes saved to world!");
            });
        });

        return true;
    }

    @Override
    public boolean canUse(@NonNull Player player) {
        return player.hasPermission("axis.tools.fill") || player.isOp();
    }

    @Override
    public ToolSettings createDefaultSettings() {
        ToolSettings settings = new ToolSettings();
        Map<Material, Double> defaultBlocks = new HashMap<>();
        defaultBlocks.put(Material.STONE, 100.0);
        settings.setMaterialPercentages("blocks", defaultBlocks);
        settings.set("replace_air_only", false);
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