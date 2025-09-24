package dev.ocean.axis.utils;

import dev.ocean.axis.region.SelectionService;
import dev.ocean.axis.region.impl.CubeSelection;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Optional;

@UtilityClass
public class PlayerUtils {
    private static final double DEFAULT_MAX_DISTANCE = 100.0;
    private static final double EPSILON = 1e-6;

    private static final Duration DEFAULT_FADE_IN = Duration.ofMillis(500);
    private static final Duration DEFAULT_STAY = Duration.ofMillis(3500);
    private static final Duration DEFAULT_FADE_OUT = Duration.ofMillis(1000);

    public void sendError(Player player, String message) {
        player.sendMessage(ComponentUtils.convertLegacy("&c&lERROR &r" + message));
    }

    public void sendWarning(Player player, String message) {
        player.sendMessage(ComponentUtils.convertLegacy("&e&lWARNING &r" + message));
    }

    public void sendInfo(Player player, String message) {
        player.sendMessage(ComponentUtils.convertLegacy("&9&lINFO &r" + message));
    }

    public void sendMessage(Player player, Component message) {
        player.sendMessage(message);
    }

    public void sendMessage(Player player, String message) {
        player.sendMessage(ComponentUtils.convertLegacy(message));
    }

    @Deprecated
    public Component convertLegacy(String input) {
        return ComponentUtils.convertLegacy(input);
    }

    public boolean isLookingAtBoundingBox(Player player, BoundingBox boundingBox) {
        return isLookingAtBoundingBox(player, boundingBox, DEFAULT_MAX_DISTANCE);
    }

    public void playSound(Player player, String sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    public void playSound(Player player, String sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public void playSound(Player player, Location location, String sound, float volume, float pitch) {
        player.playSound(location, sound, volume, pitch);
    }

    public void playSoundSuccess(Player player) {
        playSound(player, "entity.experience_orb.pickup", 1.0f, 1.2f);
    }

    public void playSoundError(Player player) {
        playSound(player, "entity.villager.no", 1.0f, 0.8f);
    }

    public void playSoundWarning(Player player) {
        playSound(player, "block.note_block.pling", 1.0f, 0.5f);
    }

    public void playSoundInfo(Player player) {
        playSound(player, "entity.experience_orb.pickup", 0.7f, 1.0f);
    }

    public void playSoundClick(Player player) {
        playSound(player, "ui.button.click", 0.5f, 1.0f);
    }

    public void playSoundTeleport(Player player) {
        playSound(player, "entity.enderman.teleport", 1.0f, 1.0f);
    }

    public void playSoundLevelUp(Player player) {
        playSound(player, "entity.player.levelup", 1.0f, 1.0f);
    }

    public void playSoundBell(Player player) {
        playSound(player, "block.bell.use", 1.0f, 1.0f);
    }

    public void playSoundPop(Player player) {
        playSound(player, "entity.chicken.egg", 1.0f, 1.5f);
    }

    public void playSoundBreak(Player player) {
        playSound(player, "entity.item.break", 1.0f, 1.0f);
    }

    public void stopAllSounds(Player player) {
        player.stopAllSounds();
    }

    public void stopSound(Player player, String sound) {
        player.stopSound(sound);
    }

    public void playGlobalSound(String sound, float volume, float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            playSound(p, sound, volume, pitch);
        }
    }

    public void playSoundToNearbyPlayers(Location location, String sound, float volume, float pitch, double radius) {
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius) {
                playSound(player, location, sound, volume, pitch);
            }
        }
    }

    public void sendActionBar(Player player, String message) {
        player.sendActionBar(ComponentUtils.convertLegacy(message));
    }

    public void sendActionBar(Player player, Component message) {
        player.sendActionBar(message);
    }

    public void sendActionBarError(Player player, String message) {
        sendActionBar(player, "&c" + message);
    }

    public void sendActionBarWarning(Player player, String message) {
        sendActionBar(player, "&e" + message);
    }

    public void sendActionBarInfo(Player player, String message) {
        sendActionBar(player, "&9" + message);
    }

    public void sendActionBarSuccess(Player player, String message) {
        sendActionBar(player, "&a" + message);
    }

    public void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT);
    }

    public void sendTitle(Player player, String title, String subtitle, Duration fadeIn, Duration stay, Duration fadeOut) {
        Component titleComponent = title != null ? ComponentUtils.convertLegacy(title) : Component.empty();
        Component subtitleComponent = subtitle != null ? ComponentUtils.convertLegacy(subtitle) : Component.empty();

        Title titleObj = Title.title(titleComponent, subtitleComponent, Title.Times.times(fadeIn, stay, fadeOut));
        player.showTitle(titleObj);
    }

    public void sendTitle(Player player, Component title, Component subtitle) {
        sendTitle(player, title, subtitle, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT);
    }

    public void sendTitle(Player player, Component title, Component subtitle, Duration fadeIn, Duration stay, Duration fadeOut) {
        Component titleComponent = title != null ? title.decoration(TextDecoration.ITALIC, false) : Component.empty();
        Component subtitleComponent = subtitle != null ? subtitle.decoration(TextDecoration.ITALIC, false) : Component.empty();

        Title titleObj = Title.title(titleComponent, subtitleComponent, Title.Times.times(fadeIn, stay, fadeOut));
        player.showTitle(titleObj);
    }

    public void sendTitleOnly(Player player, String title) {
        sendTitle(player, title, null);
    }

    public void sendTitleOnly(Player player, Component title) {
        sendTitle(player, title, null);
    }

    public void sendSubtitleOnly(Player player, String subtitle) {
        sendTitle(player, null, subtitle);
    }

    public void sendSubtitleOnly(Player player, Component subtitle) {
        sendTitle(player, null, subtitle);
    }

    public void clearTitle(Player player) {
        player.clearTitle();
    }

    public void resetTitleTimes(Player player) {
        player.resetTitle();
    }

    public void sendQuickTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle,
                Duration.ofMillis(250),
                Duration.ofMillis(1000),
                Duration.ofMillis(250));
    }

    public void sendLongTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle,
                Duration.ofMillis(1000),
                Duration.ofMillis(5000),
                Duration.ofMillis(1000));
    }

    public boolean isLookingAtBoundingBox(Player player, BoundingBox boundingBox, double maxDistance) {
        Location eyeLocation = player.getEyeLocation();
        Vector rayOrigin = eyeLocation.toVector();
        Vector rayDirection = eyeLocation.getDirection().normalize();

        return rayIntersectsBoundingBox(rayOrigin, rayDirection, boundingBox, maxDistance);
    }

    public boolean isLookingAtSelection(Player player) {
        return isLookingAtBoundingBox(player, BoundingBox.of(
                ((CubeSelection) SelectionService.get().getSelection(player.getUniqueId())).getPos1(),
                ((CubeSelection) SelectionService.get().getSelection(player.getUniqueId())).getPos2()
        ));
    }

    public boolean isLookingAtSelection(Player player, Location pos1, Location pos2) {
        return isLookingAtBoundingBox(player, BoundingBox.of(pos1, pos2));
    }

    public boolean isLookingAtSelection(Player player, Location pos1, Location pos2, double maxDistance) {
        return isLookingAtBoundingBox(player, BoundingBox.of(pos1, pos2), maxDistance);
    }

    public Optional<Vector> getRaycastHit(Player player, BoundingBox boundingBox) {
        return getRaycastHit(player, boundingBox, DEFAULT_MAX_DISTANCE);
    }

    public Optional<Vector> getRaycastHit(Player player, BoundingBox boundingBox, double maxDistance) {
        Location eyeLocation = player.getEyeLocation();
        Vector rayOrigin = eyeLocation.toVector();
        Vector rayDirection = eyeLocation.getDirection().normalize();

        return rayIntersectsBoundingBoxWithHit(rayOrigin, rayDirection, boundingBox, maxDistance);
    }

    public double getDistanceToBoundingBox(Player player, BoundingBox boundingBox) {
        Vector eyePos = player.getEyeLocation().toVector();
        Vector closestPoint = getClosestPointOnBoundingBox(eyePos, boundingBox);
        return eyePos.distance(closestPoint);
    }

    private Vector getClosestPointOnBoundingBox(Vector point, BoundingBox boundingBox) {
        double x = Math.max(boundingBox.getMinX(), Math.min(point.getX(), boundingBox.getMaxX()));
        double y = Math.max(boundingBox.getMinY(), Math.min(point.getY(), boundingBox.getMaxY()));
        double z = Math.max(boundingBox.getMinZ(), Math.min(point.getZ(), boundingBox.getMaxZ()));
        return new Vector(x, y, z);
    }

    private boolean rayIntersectsBoundingBox(Vector rayOrigin, Vector rayDirection, BoundingBox box, double maxDistance) {
        double tMin = 0.0;
        double tMax = maxDistance;

        double[] xValues = getSlabIntersection(rayOrigin.getX(), rayDirection.getX(), box.getMinX(), box.getMaxX());
        if (!updateSlabBounds(xValues, tMin, tMax)) return false;
        tMin = Math.max(tMin, Math.min(xValues[0], xValues[1]));
        tMax = Math.min(tMax, Math.max(xValues[0], xValues[1]));

        double[] yValues = getSlabIntersection(rayOrigin.getY(), rayDirection.getY(), box.getMinY(), box.getMaxY());
        if (!updateSlabBounds(yValues, tMin, tMax)) return false;
        tMin = Math.max(tMin, Math.min(yValues[0], yValues[1]));
        tMax = Math.min(tMax, Math.max(yValues[0], yValues[1]));

        double[] zValues = getSlabIntersection(rayOrigin.getZ(), rayDirection.getZ(), box.getMinZ(), box.getMaxZ());
        if (!updateSlabBounds(zValues, tMin, tMax)) return false;
        tMin = Math.max(tMin, Math.min(zValues[0], zValues[1]));
        tMax = Math.min(tMax, Math.max(zValues[0], zValues[1]));

        return tMin <= tMax && tMax >= 0;
    }

    private Optional<Vector> rayIntersectsBoundingBoxWithHit(Vector rayOrigin, Vector rayDirection, BoundingBox box, double maxDistance) {
        double tMin = 0.0;
        double tMax = maxDistance;

        double[] xValues = getSlabIntersection(rayOrigin.getX(), rayDirection.getX(), box.getMinX(), box.getMaxX());
        if (!updateSlabBounds(xValues, tMin, tMax)) return Optional.empty();
        tMin = Math.max(tMin, Math.min(xValues[0], xValues[1]));
        tMax = Math.min(tMax, Math.max(xValues[0], xValues[1]));

        double[] yValues = getSlabIntersection(rayOrigin.getY(), rayDirection.getY(), box.getMinY(), box.getMaxY());
        if (!updateSlabBounds(yValues, tMin, tMax)) return Optional.empty();
        tMin = Math.max(tMin, Math.min(yValues[0], yValues[1]));
        tMax = Math.min(tMax, Math.max(yValues[0], yValues[1]));

        double[] zValues = getSlabIntersection(rayOrigin.getZ(), rayDirection.getZ(), box.getMinZ(), box.getMaxZ());
        if (!updateSlabBounds(zValues, tMin, tMax)) return Optional.empty();
        tMin = Math.max(tMin, Math.min(zValues[0], zValues[1]));
        tMax = Math.min(tMax, Math.max(zValues[0], zValues[1]));

        if (tMin > tMax || tMax < 0) return Optional.empty();

        double t = tMin >= 0 ? tMin : tMax;
        if (t > maxDistance) return Optional.empty();

        Vector hitPoint = rayOrigin.clone().add(rayDirection.clone().multiply(t));
        return Optional.of(hitPoint);
    }

    private double[] getSlabIntersection(double rayPos, double rayDir, double slabMin, double slabMax) {
        if (Math.abs(rayDir) < EPSILON) {
            return new double[]{Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
        }
        double t1 = (slabMin - rayPos) / rayDir;
        double t2 = (slabMax - rayPos) / rayDir;
        return new double[]{t1, t2};
    }

    private boolean updateSlabBounds(double[] values, double tMin, double tMax) {
        if (values[0] == Double.NEGATIVE_INFINITY && values[1] == Double.POSITIVE_INFINITY) {
            return true;
        }
        return Math.min(values[0], values[1]) <= Math.max(values[0], values[1]);
    }
}