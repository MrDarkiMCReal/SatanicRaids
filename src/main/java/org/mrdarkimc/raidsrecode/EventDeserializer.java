package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.ConfigAPI.Config;
import org.mrdarkimc.SatanicLib.worldedit.WeSchemLoader;
import org.mrdarkimc.SatanicLib.worldedit.pasters.PartitionWePaster;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePaster;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePasterImpl;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.raidsrecode.api.EventSupplier;
import org.mrdarkimc.raidsrecode.events.raidevent.RaidWorldPaster;
import org.mrdarkimc.raidsrecode.finders.AsyncLocationFinder;
import org.mrdarkimc.raidsrecode.finders.LocationFinder;
import org.mrdarkimc.raidsrecode.finders.PreparedLocationFinder;
import org.mrdarkimc.raidsrecode.portals.Offset;
import org.mrdarkimc.raidsrecode.portals.Portal;
import org.mrdarkimc.raidsrecode.portals.RaidPortal;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EventDeserializer {
    private static final Map<String, EventSupplier> registry = new HashMap<>();
    private final JavaPlugin plugin;
    private final Config mainConfig;
    private final Config hologramConfig;
    private final WeSchemLoader schemLoader;
    private final Map<String, BiFunction<ConfigurationSection, String, WePaster>> pasteStrategies = new HashMap<>();
    private final Map<String, Function<ConfigurationSection, LocationFinder>> locationStrategies = new HashMap<>();

    public EventDeserializer(JavaPlugin plugin, WeSchemLoader schemLoader, Config mainConfig, Config hologramConfig) {
        this.plugin = plugin;
        this.mainConfig = mainConfig;
        this.hologramConfig = hologramConfig;
        this.schemLoader = schemLoader;

        pasteStrategies.put("DEFAULT", this::createNormalPaster);
        pasteStrategies.put("PARTITION", this::createPartitionPaster);
        pasteStrategies.put("RAIDWORLD", this::createRaidWorldPaster);

        locationStrategies.put("ASYNC", this::createAsyncFinder);
        locationStrategies.put("PREPARED", this::createPreparedFinder);
    }

    public static void register(String type, EventSupplier supplier) {
        registry.put(type.toLowerCase(Locale.ROOT), supplier);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }


    /**
     * Читает все файлы из папки {@code plugins/<name>/events/}
     */
    public List<EventSupplier> allEvents() {
        List<EventSupplier> result = new ArrayList<>();
        String subfolder = "events";

        Path eventsDir = plugin.getDataFolder().toPath().resolve(subfolder);
        File dir = eventsDir.toFile();

        if (!dir.exists() || !dir.isDirectory()) {
            plugin.getLogger().warning("[EventDeserializer] Папка events/ не найдена: " + dir.getAbsolutePath());
            return result;
        }

        File[] files = dir.listFiles(f -> f.getName().endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("[EventDeserializer] Папка events/ пуста — нет эвентов для загрузки.");
            return result;
        }

        for (File file : files) {
            Config config = new Config(plugin, file);
            FileConfiguration cfg = config.get();

            ConfigurationSection eventSection = cfg.getConfigurationSection("event");
            if (eventSection == null) {
                plugin.getLogger().warning("[EventDeserializer] Файл " + file.getName() + " не содержит секцию 'event'. Пропускаю.");
                continue;
            }

            String type = eventSection.getString("type");
            if (type == null || type.isBlank()) {
                plugin.getLogger().warning("[EventDeserializer] Файл " + file.getName() + " не содержит 'event.type'. Пропускаю.");
                continue;
            }

            EventSupplier prototype = registry.get(type.toLowerCase(Locale.ROOT));
            if (prototype == null) {
                plugin.getLogger().warning("[EventDeserializer] Неизвестный тип эвента '" + type + "' в файле " + file.getName() + ". Зарегистрированные типы: " + registry.keySet());
                continue;
            }

            try {
                EventSupplier supplier = prototype.fromConfig(cfg, this);
                result.add(supplier);
                plugin.getLogger().info("[EventDeserializer] Загружен эвент '" + supplier.getDisplayName() + "' (тип: " + type + ") из " + file.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("[EventDeserializer] Ошибка при загрузке эвента из " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return result;
    }

    public Portal getPortal(String path) {
        ConfigurationSection sec = mainConfig.get().getConfigurationSection("portals." + path);
        if (sec == null) throw new RuntimeException("Portal config " + path + " not found!");

        PasteData pasters = getPasteData(sec);
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

    public PasteData getPasteData(ConfigurationSection parentSec) {
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

    public List<Location> loadSafeLocations(World world) {
        List<Map<?, ?>> spawns = mainConfig.get().getMapList("playerRespawns");
        return spawns.stream()
                .map(m -> new Location(world, (int) m.get("x"), (int) m.get("y"), (int) m.get("z")))
                .collect(Collectors.toList());
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
        return new WePasterImpl(schemLoader.getClipboard(schemName), (s) -> s.ignoreAirBlocks(true));
    }

    private WePaster createPartitionPaster(ConfigurationSection sec, String schemName) {
        ConfigurationSection partSec = sec.getConfigurationSection("partition");
        return PartitionWePaster.newBuilder()
                .usingPlugin(plugin)
                .withName(schemName)
                .setSchemLoader(schemLoader)
                .chunks(partSec != null ? partSec.getInt("chunks") : 4)
                .interval(partSec != null ? partSec.getLong("delay") : 120L)
                .build();
    }

    private WePaster createRaidWorldPaster(ConfigurationSection sec, String schemName) {
        ConfigurationSection partSec = sec.getConfigurationSection("partition");
        return new RaidWorldPaster.RaidWorldPasterBuilder()
                .usingPlugin(plugin)
                .withName(schemName)
                .setSchemLoader(schemLoader)
                .chunks(partSec != null ? partSec.getInt("chunks") : 4)
                .interval(partSec != null ? partSec.getLong("delay") : 120L)
                .build();
    }

    private LocationFinder createPreparedFinder(ConfigurationSection sec) {
        World world = Bukkit.getWorld(sec.getString("world", "world"));
        double x = sec.getDouble("x", 0);
        double y = sec.getDouble("y", 0);
        double z = sec.getDouble("z", 0);
        if (x == 0 && y == 0 && z == 0) {
            Bukkit.getLogger().warning("[EventDeserializer] Координаты 0 0 0 для PreparedFinder. Это нормально?");
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
                .onFound(loc -> plugin.getLogger().warning("[AsyncLocFinder] Найдена локация: " + loc + ". Переопредели onFound через метод whenFound!"))
                .build();
    }

    private Offset getOffset(ConfigurationSection sec) {
        if (sec == null) return new Offset(0, 0, 0);
        return new Offset(sec.getInt("x"), sec.getInt("y"), sec.getInt("z"));
    }


    public record PasteData(WePaster paster, LocationFinder finder) {
    }
}
