package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.mrdarkimc.SatanicLib.configsetups.Configs;
import org.mrdarkimc.SatanicLib.worldedit.WeSchemLoader;
import org.mrdarkimc.SatanicLib.worldedit.pasters.PartitionWePaster;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePaster;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePasterImpl;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.raidsrecode.events.RaidEvent;
import org.mrdarkimc.raidsrecode.events.RunnableEvent;
import org.mrdarkimc.raidsrecode.finders.AsyncLocationFinder;
import org.mrdarkimc.raidsrecode.finders.LocationFinder;
import org.mrdarkimc.raidsrecode.finders.PreparedLocationFinder;
import org.mrdarkimc.raidsrecode.portals.Offset;
import org.mrdarkimc.raidsrecode.portals.Portal;
import org.mrdarkimc.raidsrecode.portals.RaidPortal;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EventDeserializer {
    private final JavaPlugin plugin;
    private final Configs mainConfig;
    private final Configs hologramConfig;

    private final Map<String, BiFunction<ConfigurationSection, String, WePaster>> pasteStrategies = new HashMap<>();
    private final Map<String, Function<ConfigurationSection, LocationFinder>> locationStrategies = new HashMap<>();

    public EventDeserializer(JavaPlugin plugin, Configs mainConfig, Configs hologramConfig) {
        this.plugin = plugin;
        this.mainConfig = mainConfig;
        this.hologramConfig = hologramConfig;

        pasteStrategies.put("DEFAULT", this::createNormalPaster);
        pasteStrategies.put("PARTITION", this::createPartitionPaster);
        pasteStrategies.put("RAIDWORLD", this::createRaidWorldPaster);//todo надо добавить поддержку добавления таких методов в общий класс-десерализатор
        locationStrategies.put("ASYNC", this::createAsyncFinder);
        locationStrategies.put("PREPARED", this::createPreparedFinder);
    }

    private LocationFinder createPreparedFinder(ConfigurationSection sec) {
        World world = Bukkit.getWorld(sec.getString("world", "world"));
        double x = sec.getDouble("x", 0);
        double y = sec.getDouble("y", 0);
        double z = sec.getDouble("z", 0);
        if (x == 0 && x == y && x == z) {
            Bukkit.getLogger().warning("Warning. Coordinates is 0 0 0 for prepared portal. Is it okay?");
            //return createAsyncFinder(sec);
        }
        return new PreparedLocationFinder(new Location(world, x, y, z));
    }

    private LocationFinder createAsyncFinder(ConfigurationSection sec) {
        World world = Bukkit.getWorld(sec.getString("world", "world"));
        Location center = new Location(world, sec.getDouble("x", 0), sec.getDouble("y", 0), sec.getDouble("z", 0));

        return AsyncLocationFinder.newBuilder()
                .center(center)
                .radius(sec.getInt("radius", 1000))
                .maxAttempts(sec.getInt("maxAttempts", 50))
                //.requireSafeSurface() // вынести в конфиг как список требований
                //.require3x3WithAirAbove()
                .onFound(loc -> {
                    plugin.getLogger().warning("[AsyncLocFinder] Location found: " + loc + " WARNING: this is default consumer! Override it with whenFound method");
                })
                .build();
    }

    public List<Supplier<RunnableEvent>> allEvents() {
        List<Supplier<RunnableEvent>> allEvents = new ArrayList<>();
        Set<String> eventSec = mainConfig.get().getConfigurationSection("events").getKeys(false);
        eventSec.forEach(key -> allEvents.add(() -> getEvent(key)));
        return allEvents;
    }

    public RunnableEvent getEvent(String eventKey) {
        ConfigurationSection eventSec = mainConfig.get().getConfigurationSection("events." + eventKey);
        ConfigurationSection globalSec = mainConfig.get().getConfigurationSection("event");

        if (eventSec == null) throw new RuntimeException("Event config for " + eventKey + " not found!");

        PasteData eventPasters = getPasteData(eventSec);

        Portal raidIn = getPortal("raidInPortal");
        Portal raidOut = getPortal("raidOutPortal");

        World world = Bukkit.getWorld(eventSec.getString("paste.pasteLocation.world", "raidWorld"));
        List<Location> spawnPoints = loadSafeLocations(world);

        // Длительность конкретного события — events.<key>.duration (сек).
        // Раньше читали event.duration, которого в конфиге нет (есть event.interval — интервал планировщика).
        int durationSeconds = eventSec.getInt("duration",
                globalSec != null ? globalSec.getInt("duration", 3600) : 3600);
        Bukkit.getLogger().info(String.format("Creating event: %s with duration %s sec.", eventKey, durationSeconds));
        return new RaidEvent(plugin, eventPasters, spawnPoints, raidIn, raidOut, durationSeconds);
    }

    public Portal getPortal(String path) {
        ConfigurationSection sec = mainConfig.get().getConfigurationSection("portals." + path);
        if (sec == null) throw new RuntimeException("Portal config " + path + " not found!");

        PasteData pasters = getPasteData(sec); //todo избавиться от этого, сразу ищем paster, на следующем спринте взяться
        WePaster paster = pasters.paster();

        String holoKey = sec.getString("holo");
        ConfigurationSection holoConf = hologramConfig.get().getConfigurationSection("textdisplays." + holoKey);
        if (holoConf == null) throw new RuntimeException("Hologram " + holoKey + " not found!");

        MiniTextDisplay template = MiniTextDisplay.fromConfig(holoConf);

        return new RaidPortal(
                plugin,
                paster,
                template,
                getOffset(sec.getConfigurationSection("holoOffset")),
                getOffset(sec.getConfigurationSection("portalEntranceOffset"))
        );
    }

    public record PasteData(WePaster paster, LocationFinder finder) {
    }

    private PasteData getPasteData(ConfigurationSection parentSec) {
//        List<Map<?, ?>> pastesMaps = parentSec.getMapList("paste");
//        if (pastesMaps.isEmpty()) {
//            throw new RuntimeException("Секция 'paste' пуста или отсутствует в " + parentSec.getName());
//        }
//
//        ConfigurationSection pasteSec = createMemorySection(pastesMaps.get(0));
//
//        WePaster paster = getWePaster(pasteSec);
//
//        ConfigurationSection locSec = pasteSec.getConfigurationSection("pasteLocation");
//        String locStrategyName = (locSec != null) ? locSec.getString("strategy", "PREPARED").toUpperCase() : "PREPARED";
//
//        LocationFinder finder = locationStrategies.getOrDefault(locStrategyName, this::createPreparedFinder)
//                .apply(locSec);
        ConfigurationSection pasteSec = parentSec.getConfigurationSection("paste");

        if (pasteSec == null) {
            throw new RuntimeException("Секция 'paste' не найдена в " + parentSec.getName());
        }

        WePaster paster = getWePaster(pasteSec);
        ConfigurationSection locSec = pasteSec.getConfigurationSection("pasteLocation");
        String locStrategyName = (locSec != null)
                ? locSec.getString("strategy", "PREPARED").toUpperCase()
                : "PREPARED";

        LocationFinder finder = locationStrategies.getOrDefault(locStrategyName, this::createPreparedFinder)
                .apply(locSec);
        return new PasteData(paster, finder);
    }

    private WePaster getWePaster(ConfigurationSection pasteSec) {
        String strategyName = pasteSec.getString("pasteStrategy", "DEFAULT").toUpperCase();
        String schemName = pasteSec.getString("schem");

        if (schemName == null) {
            throw new RuntimeException("В конфиге не указано имя схемы (schem)!");
        }

        BiFunction<ConfigurationSection, String, WePaster> pasterFunc =
                pasteStrategies.getOrDefault(strategyName, this::createNormalPaster);

        return pasterFunc.apply(pasteSec, schemName);
    }

    private WePaster createNormalPaster(ConfigurationSection sec, String schemName) {
        return new WePasterImpl(WeSchemLoader.getClipboard(schemName), (s) -> s.ignoreAirBlocks(true)); //todo лютый хардкод и хуйня и мне надо надавать по башке. Это надо выносить в конфиг
    }

    private WePaster createPartitionPaster(ConfigurationSection sec, String schemName) {
        ConfigurationSection partSec = sec.getConfigurationSection("partition");
        return PartitionWePaster.newBuilder()
                .usingPlugin(plugin)
                .withName(schemName)
                .chunks(partSec != null ? partSec.getInt("chunks") : 4)
                .interval(partSec != null ? partSec.getLong("delay") : 120L)
                .build();
    }
    private WePaster createRaidWorldPaster(ConfigurationSection sec, String schemName) {
        ConfigurationSection partSec = sec.getConfigurationSection("partition");
        return new RaidWorldPaster.RaidWorldPasterBuilder()
                .usingPlugin(plugin)
                .withName(schemName)
                .chunks(partSec != null ? partSec.getInt("chunks") : 4)
                .interval(partSec != null ? partSec.getLong("delay") : 120L)
                .build();
    }

    private Offset getOffset(ConfigurationSection sec) {
        if (sec == null) return new Offset(0, 0, 0);
        return new Offset(sec.getInt("x"), sec.getInt("y"), sec.getInt("z"));
    }


    private List<Location> loadSafeLocations(World world) {
        List<Map<?, ?>> spawns = mainConfig.get().getMapList("playerRespawns");
        return spawns.stream()
                .map(m -> new Location(world, (int) m.get("x"), (int) m.get("y"), (int) m.get("z")))
                .collect(Collectors.toList());
    }
}


