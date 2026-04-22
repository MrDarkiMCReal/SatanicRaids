package org.mrdarkimc.satanicraids.worlds;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public interface EventWorld {
    public void loadWorld();
    public void unloadWorld();
    public void addPlayer(Player player);
    public List<Player> getPlayers();
    public World getWorld();
}
