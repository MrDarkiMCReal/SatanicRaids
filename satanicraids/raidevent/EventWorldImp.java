package org.mrdarkimc.satanicraids.raidevent;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.enhancedtextdisplays.EnhancedTextDisplays;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.enhancedtextdisplays.displays.interfaces.DisplayHandler;
import org.mrdarkimc.enhancedtextdisplays.tasks.UpdateHoloTask;
import org.mrdarkimc.satanicraids.SatanicRaids;
import org.mrdarkimc.satanicraids.holograms.HologramHandler;
import org.mrdarkimc.satanicraids.tasks.ChestUpdateTask;
import org.mrdarkimc.satanicraids.tasks.HologramUpdateTask;
import org.mrdarkimc.satanicraids.weapi.WorldPaster;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EventWorldImp implements EventWorld {
    private final World raidWorld;
    private WorldPaster eventWorldSchemPaster;
    private final Location pasteLocation;
    private final List<Location> playerSpawnPoints;
    private final List<Location> chestLocations;
    //private final List<Player> involvedPlayers = new ArrayList<>();
    private final List<Location> hologramLocations = new ArrayList<>(); //todo
    private ConfigurationSection config;
    //private LocationFinder eventSpawnPointFinder;
    private HologramHandler chestHologramHandler;
    private HologramHandler portalHoloHandler;
    private BukkitTask chestHologramUpdaterTask;
    private BukkitTask chestFillerTask;

    public EventWorldImp(World world, WorldPaster paster, List<Location> playerSpawnPoints, List<Location> chestLocations, HologramHandler portalHoloHandler) {
        this.raidWorld = world;
        this.playerSpawnPoints = playerSpawnPoints;
        this.chestLocations = chestLocations;
        this.eventWorldSchemPaster = paster;
        this.pasteLocation = new Location(raidWorld, 0, 0, 0);
        //this.eventSpawnPointFinder = new PresetAndSafeFinder(this, playerSpawnPoints);
        this.config = SatanicRaids.getInstance().getConfig();
        this.chestHologramHandler = new HologramHandler(60); //обновляем голограммы каждые 60 сек
        this.portalHoloHandler = portalHoloHandler;

    }

    @Override
    public CompletableFuture<Void> loadWorld() {
        CompletableFuture<Void> future = eventWorldSchemPaster.pasteAsync(pasteLocation).thenRun(() -> {
                    Bukkit.getScheduler().runTask(SatanicRaids.getInstance(), () -> {
                        prepareSpawnLocations();
                        spawnChests();
                        createOutPortal();
                        this.chestHologramUpdaterTask = new HologramUpdateTask(chestHologramHandler).runTaskTimer(SatanicRaids.getInstance(), 0, 20);
                        this.chestFillerTask = new ChestUpdateTask(chestLocations).runTaskTimer(SatanicRaids.getInstance(), 0, 60 * 20L);
                    });
                }).exceptionally(ex -> {
                    Bukkit.getLogger().severe("[RaidEvent] Ошибка при загрузке мира рейда: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
        return future;
    }

    @Override
    public void unloadWorld() {
        cleanUp();
        //очищаем все затраченные ресурсы.
        //Event не должен иметь состояния на момент выключения
    }

    @Override
    public void teleportIntoEvent(Player player) {
        if (raidWorld == null) {
            player.sendMessage("§cМир рейда еще не загружен!");
            return;
        }

        Location targetLocation = findSafePlayerSpawnLocation();
        if (targetLocation != null) {
            //if (!involvedPlayers.contains(player)) {
            //    involvedPlayers.add(player);

            player.teleport(targetLocation.add(0, 2, 0));
            player.sendMessage("§aВы были телепортированы в рейдовый мир!");
            //} else {
            //    Bukkit.getLogger().warning(String.format("[%s] Невозможно добавить игрока в рейдовый мир. Игрок уже там."));
            //}


        } else {
            player.sendMessage("§cНе удалось найти безопасное место для телепортации!");
        }
    }

    private void createOutPortal() {
        FileConfiguration configuration = SatanicRaids.getInstance().getHologramConfig().get();
        ConfigurationSection section = configuration.getConfigurationSection("textdisplays.portalOut");
        MiniTextDisplay miniTextDisplay = MiniTextDisplay.fromConfig(section);
        Location loc = new Location(raidWorld, 0, 0, 0);
        create3by3Zone(loc, Material.END_PORTAL);
        miniTextDisplay.spawn(loc.clone().add(0, 2, 0));
        portalHoloHandler.add(miniTextDisplay);
    }

    private Location findSafePlayerSpawnLocation() {
//        LocationFinder finder = eventSpawnPointFinder;
//        if (finder != null && raidWorld != null) {
//            // Устанавливаем мир в finder
////            if (respawnFinder instanceof PresetAndSafeFinder) { //todo почему так
////                ((PresetAndSafeFinder) respawnFinder).setWorld(raidWorld);
////            }
//            Location found = finder.find();
//            if (found != null) {
//                found.setWorld(raidWorld);
//                return found;
//            }
//        }
//        // Если не нашли, возвращаем спавн мира
//        if (raidWorld != null) {
//            Bukkit.getLogger().warning("[SatanicRaids] Неудалось найти локацию. Беру первую попавшуюся");
        int size = playerSpawnPoints.size();
        Random random = new Random();
        int i = random.nextInt(0, size);
        return playerSpawnPoints.get(i);
        //}
        //return null;
    }


    @Override
    public World getRaidWorld() {
        return raidWorld;
    }

    @Override
    public List<Location> getChestLocations() {
        return chestLocations;
    }


    //@Override
    //public List<Player> getInvolvedPlayers() {
    //    return new ArrayList<>(involvedPlayers);
    //}

    //@Override
    //public void removePlayer(Player player) {
    //    involvedPlayers.remove(player);
    //}

    public void spawnChests() {
        chestLocations.forEach(loc -> {
                    loc.getBlock().setType(Material.CHEST);
                    create3by3Zone(loc.clone().subtract(0, 1, 0), Material.BEDROCK);
                    spawnChestHologram(loc);
                }
        ); //todo зарегать слушатель, что бы их нельзя было взорвать и ломать
    }

    private void create3by3Zone(Location loc, Material blocktype) {
        int centerX = loc.getBlockX();
        int centerY = loc.getBlockY();
        int centerZ = loc.getBlockZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = raidWorld.getBlockAt(centerX + x, centerY, centerZ + z);
                block.setType(blocktype);
            }
        }
    }

    public void prepareSpawnLocations() {
        for (Location spawnLocation : playerSpawnPoints) {
            raidWorld.getBlockAt(spawnLocation).setType(Material.BEDROCK);
            create3by3Zone(spawnLocation, Material.BEDROCK);
        }
    }

    public void spawnChestHologram(final Location location) {
        FileConfiguration hologramConfig = SatanicRaids.getInstance().getHologramConfig().get();
        ConfigurationSection section = hologramConfig.getConfigurationSection("textdisplays.templateChest");
        MiniTextDisplay textDisplay = MiniTextDisplay.fromConfig(section);
        Location adjustedLocation = location.clone().add(0.5, 1, 0.5);

        DisplayHandler handler = EnhancedTextDisplays.getInstance().getDisplayHandler();
      
    }

    public void cleanUp() {
        // Отменяем задачи если они еще не отменены
        if (chestHologramUpdaterTask != null && !chestHologramUpdaterTask.isCancelled()) {
            chestHologramUpdaterTask.cancel();
        }
        if (chestFillerTask != null && !chestFillerTask.isCancelled()) {
            chestFillerTask.cancel();
        }
        
        // Очищаем списки
        chestLocations.clear();
        hologramLocations.clear();
        
        // Удаляем все сущности кроме игроков
        raidWorld.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);

        //involvedPlayers.clear();
    }

    //public List<Location> getPlayerSpawnPoints() {
    //    return playerSpawnPoints;
    //}


}
