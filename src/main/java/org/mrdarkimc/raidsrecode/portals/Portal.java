package org.mrdarkimc.raidsrecode.portals;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Portal {
    public void spawn(Location loc);
    public boolean enter(Player player);
    public boolean isEnteringPortal(Player player);
    public boolean isAllowedToEnterPortal(Player player);
    public void setDestinationPoints(List<Location> locations);
    public void setTeleportRequirements(Predicate<Player> requirements);
    public void undo();
    public void afterTeleportation(Consumer<Player> onEnter);
}