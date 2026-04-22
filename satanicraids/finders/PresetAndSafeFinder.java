package org.mrdarkimc.satanicraids.finders;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mrdarkimc.satanicraids.raidevent.EventWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresetAndSafeFinder extends AbstracLocationFinder {
    private int lastSpawnIndex = -1;
    private List<Location> spawnLocations;


    public PresetAndSafeFinder(EventWorld world, List<Location> spawnLocations) {
        super(world.getRaidWorld());
        this.spawnLocations = spawnLocations != null ? spawnLocations : new ArrayList<>();
    }

    public World getWorld() {
        return world;
    }

    @Override
    public Location find() {
        Location roundRobinLocation = findRoundRobinSafeLocation();
        if (roundRobinLocation != null) {
            return roundRobinLocation;
        }

        Location anyListLocation = findAnySafeListLocation();
        if (anyListLocation != null) {
            return anyListLocation;
        }
        return findRandomSafeLocationInWorld();
    }

    private Location findRoundRobinSafeLocation() {
        for (int i = 0; i < spawnLocations.size(); i++) {
            lastSpawnIndex = (lastSpawnIndex + 1) % spawnLocations.size();
            Location location = spawnLocations.get(lastSpawnIndex);

            if (isLocationSafe(location)) {
                return location;
            }
        }
        return null;
    }

    private Location findAnySafeListLocation() {
        List<Location> shuffledLocations = new ArrayList<>(spawnLocations);
        Collections.shuffle(shuffledLocations);

        for (Location location : shuffledLocations) {
            if (isLocationSafe(location)) {
                return location;
            }
        }
        return null;
    }

    private boolean isLocationSafe(Location location) {
        // Проверяем, что в радиусе 20 блоков нет других игроков
        for (Player nearbyPlayer : world.getPlayers()) {
            if (nearbyPlayer.getLocation().distance(location) <= 20) {
                return false;
            }
        }

        // Дополнительные проверки безопасности локации
        return isLocationPhysicallySafe(location);
    }

    private boolean isLocationPhysicallySafe(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();


        Material ground = world.getBlockAt(x, y - 1, z).getType();
        if (!isSafeBlockMaterial(ground)) {
            return false;
        }


        Material current = world.getBlockAt(x, y, z).getType();
        if (current.isSolid()) {
            return false;
        }

        Material above = world.getBlockAt(x, y + 1, z).getType();
        if (above.isSolid()) {
            return false;
        }

        return true;
    }
}
