package org.mrdarkimc.satanicraids.portals;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Portal {
    private Location portalLocation;
    private PasteOffset offset;
    private final int radius;

    public Portal(Location portalLocation, PasteOffset offset, int radius) {
        this.offset = offset;
        this.radius = radius;
        System.out.println("CurrentLocation: " + portalLocation.toString());
        setPortalLocation(portalLocation);
        System.out.println("After manupulations: "+ this.portalLocation.toString());
    }

    public void updateLocation(Location loc) {
        setPortalLocation(loc);
    }

    public Location getPortalLocation() {
        return portalLocation.clone();
    }

    public boolean isNearEnoughForTeleport(Player player) {
        System.out.println("Player Loc : " + player.getLocation().toString());
        System.out.println("Portal location: " + portalLocation.toString());
        System.out.println("Distance: " + player.getLocation().distance(portalLocation));
        System.out.println("Offset: " + offset.toString());
        double distance = player.getLocation().distance(portalLocation)-1; //-1 т.к игрок стоит на блоке
        return distance <= radius;
    }

    public void destroyEndPortalBlocks() {
        World world = portalLocation.getWorld();
        int centerX = portalLocation.getBlockX();
        int centerY = portalLocation.getBlockY();
        int centerZ = portalLocation.getBlockZ();

        // Уничтожаем все блоки в квадрате 3x3
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                Location blockLocation = new Location(world, x, centerY, z);
                blockLocation.getBlock().setType(Material.AIR);
            }
        }
    }

    private void setPortalLocation(Location loc) {
        if (offset != null && offset.hasOffset()) {
            this.portalLocation = loc.clone().add(offset.toVector());
        } else {
            this.portalLocation = loc;
        }
    }
}
