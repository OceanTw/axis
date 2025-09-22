package dev.ocean.axis.region;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;

@Getter
@Setter
@Accessors
public class Selection {
    private Location pos1;
    private Location pos2;
}
