package org.mrdarkimc.satanicraids.portals;

import org.bukkit.util.Vector;

public record PasteOffset(int x, int y, int z) {

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
