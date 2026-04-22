package org.mrdarkimc.satanicraids.worlds;

import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EventWorldImp implements EventWorld {
    private final String worldName = "RaidWorld";
    private World raidWorld;
    private List<Location> spawnLocations;
    private LocationFinder respawnFinder;

    public EventWorldImp(List<Location> spawnLocations) {
        this.spawnLocations = spawnLocations;
        if (spawnLocations != null && !spawnLocations.isEmpty()) {
            this.respawnFinder = new PresetAndSafeFinder(null, spawnLocations);
        }
    }

    @Override
    public void loadWorld() {
        // Создаем мир асинхронно

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                WorldCreator creator = new WorldCreator(worldName);
                creator.type(WorldType.FLAT);
                world = Bukkit.createWorld(creator);
            }
            if (world != null) {
                world.setAutoSave(false);
                world.setPVP(true);
                world.setDifficulty(Difficulty.HARD);
                System.out.println("RaidWorld is not null");
            }

                this.raidWorld = world;
                // Пересоздаем respawnFinder с правильным миром
                if (world != null && spawnLocations != null && !spawnLocations.isEmpty()) {
                    this.respawnFinder = new PresetAndSafeFinder(world, spawnLocations);
                }
    }

    @Override
    public void unloadWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            // Телепортируем всех игроков обратно
            //List<Player> players = new ArrayList<>(world.getPlayers());
//            World defaultWorld = Bukkit.getWorld("world");
//            if (defaultWorld != null) {
//                Location spawn = defaultWorld.getSpawnLocation();
//                players.forEach(p -> p.teleport(spawn));
//            }
            Bukkit.unloadWorld(world, false);
        }
        this.raidWorld = null;
    }

    @Override
    public void addPlayer(Player player) {
        if (raidWorld == null) {
            player.sendMessage("§cМир рейда еще не загружен!");
            return;
        }

        Location targetLocation = findSafeSpawnLocation();
        if (targetLocation != null) {
            targetLocation.setWorld(raidWorld);
            player.teleport(targetLocation);
            player.sendMessage("§aВы были телепортированы в рейдовый мир!");
        } else {
            player.sendMessage("§cНе удалось найти безопасное место для телепортации!");
        }
    }

    private Location findSafeSpawnLocation() {
        if (respawnFinder != null && raidWorld != null) {
            // Устанавливаем мир в finder
//            if (respawnFinder instanceof PresetAndSafeFinder) { //todo почему так
//                ((PresetAndSafeFinder) respawnFinder).setWorld(raidWorld);
//            }
            Location found = respawnFinder.find();
            if (found != null) {
                found.setWorld(raidWorld);
                return found;
            }
        }
        // Если не нашли, возвращаем спавн мира
        if (raidWorld != null) {
            return raidWorld.getSpawnLocation();
        }
        return null;
    }

    @Override
    public List<Player> getPlayers() {
        if (raidWorld == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(raidWorld.getPlayers());
    }

    @Override
    public World getWorld() {
        return raidWorld;
    }

    public void setSpawnLocations(List<Location> spawnLocations) {
        this.spawnLocations = spawnLocations;
        if (spawnLocations != null && !spawnLocations.isEmpty() && raidWorld != null) {
            this.respawnFinder = new PresetAndSafeFinder(raidWorld, spawnLocations);
        }
    }
}
