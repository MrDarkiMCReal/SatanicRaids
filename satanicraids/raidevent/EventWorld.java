package org.mrdarkimc.satanicraids.raidevent;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventWorld {
    public CompletableFuture<Void> loadWorld();
    public void unloadWorld();
    public void teleportIntoEvent(Player player);
    public World getRaidWorld();
    public List<Location> getChestLocations();
}
