package org.mrdarkimc.raidsrecode.portals;


import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public record Offset(int x, int y, int z) {

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public boolean hasOffset() {
        if (x == 0 && y == 0 && z == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Offset{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
