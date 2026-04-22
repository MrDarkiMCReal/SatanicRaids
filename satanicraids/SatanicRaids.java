package org.mrdarkimc.satanicraids;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.SatanicLib;
import org.mrdarkimc.SatanicLib.Utils;
import org.mrdarkimc.SatanicLib.configsetups.Configs;
import org.mrdarkimc.enhancedtextdisplays.EnhancedTextDisplays;
import org.mrdarkimc.enhancedtextdisplays.displays.interfaces.DisplayHandler;
import org.mrdarkimc.satanicraids.holograms.HologramHandler;
import org.mrdarkimc.satanicraids.hooks.WGSchemLoader;
import org.mrdarkimc.satanicraids.listeners.LootEditorListener;
import org.mrdarkimc.satanicraids.portals.PortalLocationFinder;
import org.mrdarkimc.satanicraids.raidevent.*;
import org.mrdarkimc.satanicraids.raidevent.RaidEvent;
import org.mrdarkimc.satanicraids.utils.Disableable;
import org.mrdarkimc.satanicraids.weapi.SimpleSchemPaster;
import org.mrdarkimc.satanicraids.weapi.WETimedSchemPaster;
import org.mrdarkimc.satanicraids.weapi.WorldPaster;
import org.mrdarkimc.satanicraids.raidevent.EventWorld;
import org.mrdarkimc.satanicraids.raidevent.EventWorldImp;
import org.mrdarkimc.satanicraids.commands.RaidsCommand;
import org.mrdarkimc.satanicraids.finders.LocationFinder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SatanicRaids extends JavaPlugin {
//    private static SatanicRaids instance;
//
//    public static SatanicRaids getInstance() {
//        return instance;
//    }
//    private Configs config;
//    private Configs holograms;
//    private EventScheduler scheduler;
//    private EventWorld eventWorld; //todo нам нельзя напрямую давать доступ к этому классу.
//    private EventRunner eventRunner;
//    private World raidWorld;
//    private int eventLifeTime = 3 * 60; //in seconds 3 мин
//
//    public Configs getHologramConfig() {
//        return holograms;
//    }
//
//
//    @Override
//    public void onEnable() {
//        instance = this;
//        SatanicLib.setupLib(this);
//        Utils.startUp("SatanicRaids");
//        this.config = Configs.Defaults.setupConfig();
//        this.holograms = new Configs("holograms");
//        //WGSchemLoader schemLoader = new WGSchemLoader();
//        //schemLoader.loadSchematicsToCache();
//
//        initializeComponents();
//        registerListeners();
//
//        registerCommands();
//        //startScheduling();
//
//        getLogger().info("Плагин SatanicRaids успешно загружен!");
//    }
//
//    @Override
//    public void onDisable() {
//        // Останавливаем расписание
//        if (scheduler instanceof RaidScheduler) {
//            ((RaidScheduler) scheduler).stop();
//        }
//
//        if (eventWorld != null) {
//            eventWorld.unloadWorld();
//        }
//        for (Disableable disableable : Disableable.disableables) {
//            disableable.disable();
//        }
//
//    }
//    private List<Location> chestLocations;
//
//    @Override
//    public FileConfiguration getConfig() {
//        return config.get();
//    }
//
//    private void initializeComponents() {
//        raidWorld = Bukkit.getWorld("RaidWorld");
//        WGSchemLoader wgSchemLoader = new WGSchemLoader();
//        HologramHandler portalHologramHandler = new HologramHandler(eventLifeTime);
//        wgSchemLoader.loadSchematicsToCache();
//        List<Location> spawnLocations = loadSpawnLocations();
//        chestLocations = loadChestLocations();
//        String worldSchemFileName = "worldraid.schem";
//        WorldPaster worldPaster = new SimpleSchemPaster(wgSchemLoader.clipboardMap.get(worldSchemFileName), eventLifeTime);
//
//        eventWorld = new EventWorldImp(raidWorld, worldPaster ,spawnLocations, chestLocations, portalHologramHandler);
//
//
//        //portalHandler = new PortalHandler(eventWorld);
//
//        ConfigurationSection eventConfig = config.get().getConfigurationSection("event");
//        if (eventConfig == null) {
//            getLogger().warning("Секция 'event' не найдена в конфиге! Используются значения по умолчанию.");
//            return;
//        }
//
//        long interval = eventConfig.getLong("interval", 72000L); // 1 час по умолчанию
//        interval = 5*20*20; // 1 час по умолчанию
//        long duration = eventConfig.getLong("duration", 72000L); // 1 час по умолчанию
//        duration = eventLifeTime * 20L; // 1 час по умолчанию
//        int timeToDeath = eventConfig.getInt("time-to-death", 30);
//
//        //int portalRadius = eventConfig.getInt("portal-activation-radius", 3);//todo refactor to offset
//
//
//        //ConfigurationSection exitPortalConfig = eventConfig.getConfigurationSection("exit-portal");
//        //Location exitPortalLocation = null;
//        //if (exitPortalConfig != null) {
//        //    int exitX = exitPortalConfig.getInt("x", 0);
//        //    int exitY = exitPortalConfig.getInt("y", 64);
//        //    int exitZ = exitPortalConfig.getInt("z", 0);
//        //    exitPortalLocation = new Location(null, exitX, exitY, exitZ);
//        //}
//
//
//        World worldOut = Bukkit.getWorld("world");
//        if (worldOut == null) {
//            getLogger().severe("Мир 'world' не найден! Плагин не может работать.");
//            return;
//        }
//
//        LocationFinder overWorldPortalLocationFinder = new PortalLocationFinder(worldOut);
//
//        String portalSchemName = "raidportal.schem";
//        WorldPaster portalSchemPaster = new SimpleSchemPaster(wgSchemLoader.clipboardMap.get(portalSchemName), eventLifeTime);
//
//
//        RaidEvent eventRunner = new RaidEvent(
//                eventWorld,
//                overWorldPortalLocationFinder,
//                portalSchemPaster,
//                timeToDeath,
//                eventLifeTime,
//                portalHologramHandler
//        );
//        this.eventRunner = eventRunner;
//
//
//        scheduler = new RaidScheduler(interval, duration, eventRunner);
//
//        getLogger().info("Компоненты успешно инициализированы!");
//        getLogger().info("Интервал между событиями: " + (interval / 20 / 60) + " минут");
//        getLogger().info("Длительность события: " + (duration / 20 / 60) + " минут");
//    }
//
//    private List<Location> loadSpawnLocations() {
//        List<Map<?, ?>> spawnsConfig = config.get().getMapList("playerRespawns");
//        return mapsToListOfLocation(spawnsConfig);
//    }
//
//    private List<Location> loadChestLocations() {
//        List<Map<?, ?>> chestsConfig = config.get().getMapList("chests");
//        return mapsToListOfLocation(chestsConfig);
//    }
//
//    private List<Location> mapsToListOfLocation(List<Map<?, ?>> maps) {
//        return maps.stream()
//                .map(chestMap -> {
//                    int x = (int) chestMap.get("x");
//                    int y = (int) chestMap.get("y");
//                    int z = (int) chestMap.get("z");
//                    return new Location(raidWorld, x, y, z);
//                }).collect(Collectors.toList());
//    }
//
//
//    private void registerListeners() {
//        getServer().getPluginManager().registerEvents(new LootEditorListener(), this);
//    }
//    private void registerCommands() {
//        if (getCommand("raids") != null) {
//            getCommand("raids").setExecutor(new RaidsCommand());
//        }
//    }
//
//    private void startScheduling() {
//        if (scheduler != null) {
//            scheduler.startSchedule();
//        }
//    }
//
//    //public EventWorld getEventWorld() {
//    //    return eventWorld;
//    //}
//
//    //public PortalHandler getPortalHandler() {
//    //    return portalHandler;
//    //}
//
//    //public EventContainerImpl getEventContainer() {
//    //    return eventContainer;
//    //}
//
//    //public WGSchemLoader getSchemLoader() {
//    //    return schemLoader;
//    //}
//
//    public EventRunner getEventRunner() {
//        return eventRunner;
//    }
}
