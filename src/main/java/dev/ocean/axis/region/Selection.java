package dev.ocean.axis.region;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;

@Getter
@Accessors
public class Selection {
    private Location pos1;
    private Location pos2;
}
