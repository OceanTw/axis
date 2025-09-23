package dev.ocean.axis.utils;

import com.github.retrooper.packetevents.util.Quaternion4f;
import lombok.experimental.UtilityClass;
import org.bukkit.util.Vector;

@UtilityClass
public class MathUtils {

    public Quaternion4f fromToRotation(Vector from, Vector to) {
        Vector f = from.clone().normalize();
        Vector t = to.clone().normalize();

        double dot = f.dot(t);
        Vector cross = f.clone().crossProduct(t);
        double s = Math.sqrt((1 + dot) * 2);
        double invS = 1 / s;

        float x = (float) (cross.getX() * invS);
        float y = (float) (cross.getY() * invS);
        float z = (float) (cross.getZ() * invS);
        float w = (float) (s * 0.5);

        return new Quaternion4f(x, y, z, w);
    }
}
