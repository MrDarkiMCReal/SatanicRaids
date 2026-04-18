package org.mrdarkimc.raidsrecode.portals;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface Portal {
    public void spawn(Location loc);
    public void enter(Player player);
    public boolean isNearToPoral(Player player);
    public void setDestinationPoints(List<Location> locations);
    public void undo();
}