package org.mrdarkimc.satanicraids.hooks;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.satanicraids.SatanicRaids;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Regions {
    private static String regionName = "SatanicRegion-";

    public static ProtectedRegion createTimedProtectedRegion(Location center, int removeTime, int radius) {
        World world = BukkitAdapter.adapt(center.getWorld());
        double x = center.getX();
        double y = center.getY();
        double z = center.getZ();

        double minX = x - radius;
        double minY = y - radius;
        double minZ = z - radius;


        double maxX = x + radius;
        double maxY = y + radius;
        double maxZ = z + radius;
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        String localRgName = regionName + UUID.randomUUID().toString().substring(0, 8);
        ProtectedRegion region = new ProtectedCuboidRegion(localRgName, BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));
        Set<String> allowedCommands = new HashSet<>(); //blocked all commands except //todo
        region.setFlag(Flags.ALLOWED_CMDS, allowedCommands);
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);
        //region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        region.setFlag(Flags.PVP, StateFlag.State.ALLOW);
        region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
        region.setPriority(10);
        manager.addRegion(region);
        String loc = center.getWorld().getName() +
                " " +
                (int) x +
                " " +
                ((int) y) +
                " " +
                ((int) z);

        Bukkit.getLogger().warning("[Regions] Делаю Регион: " + localRgName);
        Bukkit.getLogger().warning("[Regions] Локация: " + loc);
        new BukkitRunnable() {

            @Override
            public void run() {
                manager.removeRegion(localRgName);
                Bukkit.getLogger().warning("[Regions] Регион удален: " + localRgName);
            }
        }.runTaskLater(SatanicRaids.getInstance(), removeTime * 20L);
        return region;
    }
}