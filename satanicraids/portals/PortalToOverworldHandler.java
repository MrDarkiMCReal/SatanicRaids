package org.mrdarkimc.satanicraids.portals;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Кажется от этого класса нужна только локация
 * Такое надо отрефачить. вери бед архитектура
 */
public class PortalToOverworldHandler extends SimplePortalHandler<World>{


    public PortalToOverworldHandler(World world, Portal portal) {
        super(world, portal);
    }

    @Override
    public void enter(Player player) {
        //Location portalLocation = portal.getPortalLocation();
    }

    @Override
    public void exit(Player player) {

    }
}
