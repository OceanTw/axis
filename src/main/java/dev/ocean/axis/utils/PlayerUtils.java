package dev.ocean.axis.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Optional;

@UtilityClass
public class PlayerUtils {

    private static final double DEFAULT_MAX_DISTANCE = 100.0;
    private static final double EPSILON = 1e-6;

    public void sendError(Player player, String message) {
        player.sendMessage(
                Component.text("ERROR ").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                        .append(Component.text(message).color(NamedTextColor.WHITE))
        );
    }

    public void sendWarning(Player player, String message) {
        player.sendMessage(
                Component.text("WARNING ").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                        .append(Component.text(message).color(NamedTextColor.WHITE))
        );
    }

    public void sendInfo(Player player, String message) {
        player.sendMessage(
                Component.text("INFO ").color(NamedTextColor.BLUE)
                        .append(Component.text(message).color(NamedTextColor.WHITE))
        );
    }

    public boolean isLookingAtBoundingBox(Player player, BoundingBox boundingBox) {
        return isLookingAtBoundingBox(player, boundingBox, DEFAULT_MAX_DISTANCE);
    }

    public boolean isLookingAtBoundingBox(Player player, BoundingBox boundingBox, double maxDistance) {
        Location eyeLocation = player.getEyeLocation();
        Vector rayOrigin = eyeLocation.toVector();
        Vector rayDirection = eyeLocation.getDirection().normalize();

        return rayIntersectsBoundingBox(rayOrigin, rayDirection, boundingBox, maxDistance);
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