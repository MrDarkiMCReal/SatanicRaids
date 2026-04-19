package org.mrdarkimc.raidsrecode.manager;

import org.bukkit.Location;

@FunctionalInterface
public interface Spawnable {
    public abstract void spawn(Location location);
}
