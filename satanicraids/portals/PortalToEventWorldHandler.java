package org.mrdarkimc.satanicraids.portals;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mrdarkimc.satanicraids.raidevent.EventWorld;
import org.mrdarkimc.satanicraids.utils.WorthCalculator;

import java.util.Random;

public class PortalToEventWorldHandler extends SimplePortalHandler<EventWorld>{
    private Random random = new Random();
    private WorthCalculator calculator;
    public PortalToEventWorldHandler(EventWorld eventWorld, Portal portal, WorthCalculator calculator) {
        super(eventWorld, portal);
        this.calculator = calculator;
    }

    @Override
    public void enter(Player player) {
        world.teleportIntoEvent(player);
        calculator.calculateJoin(player);
    }

    @Override
    public void exit(Player player) {
        int x = random.nextInt(-20, 20);
        int y = random.nextInt(-20, 20);

        Location closeRandomCoords = portal.getPortalLocation().add(x, 0, y);
        Block highestBlockAt = closeRandomCoords.getWorld().getHighestBlockAt(closeRandomCoords);
        player.teleport(highestBlockAt.getLocation());
        calculator.calculateExit(player);
    }
}
