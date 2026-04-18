package org.mrdarkimc.raidsrecode;

import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.SatanicLib;
import org.mrdarkimc.SatanicLib.Utils;
import org.mrdarkimc.SatanicLib.configsetups.Configs;
import org.mrdarkimc.SatanicLib.worldedit.WeSchemLoader;
import org.mrdarkimc.raidsrecode.commands.RaidsCommand;
import org.mrdarkimc.raidsrecode.eventrunner.EventRunner;
import org.mrdarkimc.raidsrecode.events.RaidScheduler;
import org.mrdarkimc.raidsrecode.events.RunnableEvent;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

public class SatanicRaids extends JavaPlugin {
    private static SatanicRaids instance;
    private Configs mainConfig;

    public static SatanicRaids getInstance() {
        return instance;
    }

    private Configs holograms;

    public Configs getHologramConfig() {
        return holograms;
    }

    private EventRunner runner;

    @Override
    public void onEnable() {
        instance = this;
        SatanicLib.setupLib(this);
        Utils.startUp("SatanicRaids");
        WeSchemLoader.loadSchematicsToCache(new File(getDataFolder() + "/schems/"));
        this.mainConfig = Configs.Defaults.setupConfig();
        this.holograms = new Configs("holograms");

        EventDeserializer eventDeserializer = new EventDeserializer(this, mainConfig, holograms);
        //RunnableEvent raidEvent = eventDeserializer.getEvent("raidworld");
        List<Supplier<RunnableEvent>> events = eventDeserializer.allEvents();
        RaidScheduler scheduler = new RaidScheduler(events, 3600); //пока хардкод
     //   scheduler.startSchedule();

        getCommand("raids").setExecutor(new RaidsCommand(scheduler));
    }

//    public RunnableEvent createEvent(String name) {
//        Portal raidInPortal = createPortal("raidInPortal");
//        Portal raidOutPortal = createPortal("raidOutPortal");
//        List<Location> spawnPoints = getPlayerSafeTeleportationLocations(Bukkit.getWorld("raidWorld"));
//        ConfigurationSection eventConfig = mainConfig.get().getConfigurationSection("event");
//        WePaster raidWorldPaster = createPartitionPaster("raidWorldPaster");
//        if (eventConfig == null) {
//            getLogger().warning("Секция 'event' не найдена в конфиге! Используются значения по умолчанию.");
//            return null;
//        }
//        int interval = eventConfig.getInt("interval", 72000); // 1 час по умолчанию
//        return new RaidEvent(this, raidWorldPaster, spawnPoints, raidInPortal, raidOutPortal, interval);
//    }
//
//    public Portal createPortal(String path) {
//        ConfigurationSection portalSection = getMainConfig().get().getConfigurationSection("portals." + path);
//        if (portalSection == null) {
//            throw new RuntimeException("Не возможно создать портал. Такой портал не найден.");
//        }
//
//        String schemName = portalSection.getString("schem");
//        Clipboard clipboard = WeSchemLoader.getClipboard(schemName);
//        WePaster paster = new WePasterImpl(clipboard, null);
//        String holoKey = portalSection.getString("holo");
//
//        FileConfiguration holoConfig = holograms.get();
//
//        ConfigurationSection holoConf = holoConfig.getConfigurationSection("textdisplays." + holoKey);
//        if (holoConfig == null) {
//            throw new RuntimeException("Не возможно создать портал. Голограмма не найдена: " + holoKey);
//        }
//        MiniTextDisplay portalTemplate = MiniTextDisplay.fromConfig(holoConf);
//
//        Offset holoOffset = getOffset(portalSection.getConfigurationSection("holoOffset"));
//        Offset entranceOffset = getOffset(portalSection.getConfigurationSection("portalEntranceOffset"));
//        return new RaidPortal(
//                this,
//                paster,
//                portalTemplate,
//                holoOffset,
//                entranceOffset
//        );
//    }
//
//    //todo отрефачить что бы можно было выбирать тип пастера
//    public WePaster createPartitionPaster(String name) {
//        FileConfiguration config = SatanicRaids.getInstance().getMainConfig().get();
//
//        ConfigurationSection section = config.getConfigurationSection("pasters." + name);
//        if (section == null) {
//            throw new RuntimeException("Section 'raidWorldPaster' not found in config!");
//        }
//        String schemName = section.getString("schem");
//        int chunks = section.getInt("chunks", 4);
//        long delay = section.getLong("delay", 80L);
//
//        return PartitionWePaster.newBuilder()
//                .usingPlugin(this)
//                .withName(schemName)
//                .chunks(chunks)
//                .interval(delay)
//                .build();
//    }
//
//    // Вспомогательный метод для чистоты кода
//    private Offset getOffset(ConfigurationSection sec) {
//        if (sec == null) return new Offset(0, 0, 0);
//        return new Offset(
//                sec.getInt("x"),
//                sec.getInt("y"),
//                sec.getInt("z")
//        );
//    }
//
//    private List<Location> getPlayerSafeTeleportationLocations(World world) {
//        List<Map<?, ?>> spawnsConfig = mainConfig.get().getMapList("playerRespawns");
//        return mapsToListOfLocation(spawnsConfig, world);
//    }
//
//    private List<Location> loadChestLocations(World world) {
//        List<Map<?, ?>> chestsConfig = mainConfig.get().getMapList("chests");
//        return mapsToListOfLocation(chestsConfig, world);
//    }
//
//    private List<Location> mapsToListOfLocation(List<Map<?, ?>> maps, World world) {
//        return maps.stream()
//                .map(chestMap -> {
//                    int x = (int) chestMap.get("x");
//                    int y = (int) chestMap.get("y");
//                    int z = (int) chestMap.get("z");
//                    return new Location(world, x, y, z);
//                }).collect(Collectors.toList());
//    }

    public Configs getMainConfig() {
        return mainConfig;
    }

    public EventRunner getEventRunner() {
        return runner;
    }
}
