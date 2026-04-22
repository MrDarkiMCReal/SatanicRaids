package org.mrdarkimc.raidsrecode.finders;

import org.bukkit.Location;
//todo все файндеры в либу
public class PreparedLocationFinder implements LocationFinder {
    private final Location location;

    public PreparedLocationFinder(Location location) {
        this.location = location;
    }

    @Override
    public Location find() {
        return location;
    }
}
