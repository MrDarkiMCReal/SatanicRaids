package org.mrdarkimc.satanicraids.raidevent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.satanicraids.SatanicRaids;
import org.mrdarkimc.satanicraids.holograms.HologramHandler;
import org.mrdarkimc.satanicraids.listeners.PortalListener;
import org.mrdarkimc.satanicraids.portals.Portal;
import org.mrdarkimc.satanicraids.portals.PasteOffset;
import org.mrdarkimc.satanicraids.portals.PortalToEventWorldHandler;
import org.mrdarkimc.satanicraids.portals.PortalToOverworldHandler;
import org.mrdarkimc.satanicraids.tasks.HologramUpdateTask;
import org.mrdarkimc.satanicraids.tasks.PrepareToEndEventTask;
import org.mrdarkimc.satanicraids.utils.Disableable;
import org.mrdarkimc.satanicraids.utils.WorthCalculator;
import org.mrdarkimc.satanicraids.weapi.SimpleSchemPaster;
import org.mrdarkimc.satanicraids.weapi.WorldPaster;
import org.mrdarkimc.satanicraids.finders.LocationFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RaidEvent implements EventRunner {
    private final EventWorld eventWorld;
    private final LocationFinder portalSpawnLocationFinder;
    private final WorldPaster portalSchemPaster;
    private final int timeToDeath;
    private WorthCalculator calculator;
    private final int eventLifeTime;
    private final HologramHandler portalHologramHandler;
    private Listener portalListener;
    private Portal leaveEventPortal;

    private BukkitTask portalHoloUpdater;
    private PrepareToEndEventTask prepareToEndEventTask;
    private BukkitTask stopTask;


    public RaidEvent(EventWorld eventWorld, LocationFinder portalSpawnLocationFinder, WorldPaster portalSchemPaster, int timeToDeath, int eventLifeTime, HologramHandler handler) {
        this.eventWorld = eventWorld;
        this.portalSpawnLocationFinder = portalSpawnLocationFinder;
        this.portalSchemPaster = portalSchemPaster;
        this.timeToDeath = timeToDeath;
        this.eventLifeTime = eventLifeTime;
        this.portalHologramHandler = handler;
        //Disableable.register(this);
    }

    @Override
    public void startEvent() {
        this.calculator = new WorthCalculator();
        eventWorld.loadWorld();
        spawnPortalToEventWorld();
        portalHoloUpdater = new HologramUpdateTask(portalHologramHandler).runTaskTimer(SatanicRaids.getInstance(), 0, 20);

        stopTask = new BukkitRunnable() {
            @Override
            public void run() {
                stopEvent();
            }
        }.runTaskLater(SatanicRaids.getInstance(), (eventLifeTime - 30) * 20L); //за минуту до конца убиваем игроков
    }

    @Override
    public void stopEvent() {
        startKillAllTimer();
    }

    public void startKillAllTimer() {
        // Отправляем предупреждение
        //new KeyedMessage(null, "messages.time-to-death", Map.of("{time}", String.valueOf(timeToDeath))).broadcast();
        leaveEventPortal.destroyEndPortalBlocks(); //больше не пускаем игроков
        prepareToEndEventTask = new PrepareToEndEventTask(eventWorld, timeToDeath);
        prepareToEndEventTask.runTaskAndThen(this::endEvent);
    }

    public void endEvent(Void unused) {
        prepareToEndEventTask.cancel();
        unregisterPortalListener();
        broadcastStats();
        portalHoloUpdater.cancel();
        eventWorld.unloadWorld();
        SimpleSchemPaster.forceStopAll(); //todo вынести в метод класса
    }

    private void broadcastStats() {
        Map<Player, Long> top = calculator.getTop(3);
        System.out.println("top players: ");
        System.out.println(top);
        int counter = 0;
        List<String> s = new ArrayList<>(List.of(" \n", " \n", " \n"));
        for (Map.Entry<Player, Long> entry : top.entrySet()) {
            if (counter==2) {
                s.set(counter, formatPlayerTop(entry.getKey(), entry.getValue()));
            }else {
                s.set(counter, formatPlayerTop(entry.getKey(), entry.getValue()) + "\n");
            }
            counter++;
        }
        new KeyedMessage(null, "messages.event-end", Map.of("{players}", String.join(" ",s))).broadcast();
    }

    private String formatPlayerTop(Player player, long sum) {
        String template = "&f             %pFormat_{player}_mini% %design_clr_primary%вынес &f%img_money%%design_clr_money%{money}";
        return template.replace("{player}", player.getName()).replace("{money}", formatBal(sum));
    }
    private String formatBal(long sum){
        String sumStr = String.valueOf(sum);
        char[] charArray = sumStr.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = charArray.length-1; i > 0; i--) {
            builder.append(charArray[i]);
            if (i % 3 == 0){
                builder.append(",");
            }
        }
        return builder.reverse().toString();
    }

    private String formatLocation(Location loc) {
        return String.format("%d, %d, %d", (int) loc.getX(), (int) loc.getY(), (int) loc.getZ());
    }

    //private void pasteWorldAsyncAndLoad() {
        //    //long methodStartTime = System.currentTimeMillis();
        //    eventWorld.loadWorld();
        //    //long tillLoadWorld = System.currentTimeMillis();
        //    spawnPortalToEventWorld();
        //    //long methodEndTime = System.currentTimeMillis();
        //    //System.out.println("pasteWorldAsyncAndLoad costed: " + (methodEndTime-methodStartTime)); //3600
        //    //System.out.println("Til world load costed: " + (tillLoadWorld-methodStartTime)); //3000
        //}

    private void spawnPortalToEventWorld() {
        //long methodStartTime = System.currentTimeMillis();

        if (portalSpawnLocationFinder == null || portalSchemPaster == null) {
            Bukkit.getLogger().warning("[RaidEvent] PortalFinder или TimedWorldPaster не инициализированы!");
            return;
        }

        //long findLocationStartTime = System.currentTimeMillis();
        Location randomLocPortal = portalSpawnLocationFinder.find();
        //Location randomLocPortal = new Location(Bukkit.getWorld("world"),10,10,10);
        //long findLocationEndTime = System.currentTimeMillis();

        if (randomLocPortal == null) {
            Bukkit.getLogger().warning("[RaidEvent] Не удалось найти место для портала!");
            return;
        }

        //System.out.println("Времени затрачено на поиск локации: " + (findLocationEndTime - findLocationStartTime) + " мс");
        // Вставляем схему портала
        //long pasteStartTime = System.currentTimeMillis();
        portalSchemPaster.pasteAsync(randomLocPortal);
        //long pasteEndTime = System.currentTimeMillis();

        //System.out.println("Времени затрачено на вставку: " + (pasteEndTime - pasteStartTime) + " мс");

        // Регистрируем портал
        //long registerStartTime = System.currentTimeMillis();
        createAndRegisterPortals(randomLocPortal);
        //long registerEndTime = System.currentTimeMillis();
        System.out.println("Вызываю метод createPortalHolo");
        createPortalHolo(randomLocPortal);
        System.out.println("Законился метод createPortalHolo");
        //System.out.println("Времени затрачено на регистрацию портала: " + (registerEndTime - registerStartTime) + " мс");

        new KeyedMessage(null, "messages.event-start", Map.of("{location}", formatLocation(randomLocPortal))).broadcast();

        //long methodEndTime = System.currentTimeMillis();
        //System.out.println("Общее время выполнения метода: " + (methodEndTime - methodStartTime) + " мс");
    }

    private void createPortalHolo(Location loc) {
        FileConfiguration configuration = SatanicRaids.getInstance().getHologramConfig().get();
        ConfigurationSection section = configuration.getConfigurationSection("textdisplays.portalIn");
//        Chunk chunk = loc.getChunk();
//        if (!chunk.isLoaded()){
//            chunk.load(); //todo undo
//        }
        MiniTextDisplay miniTextDisplay = MiniTextDisplay.fromConfig(section);
        miniTextDisplay.spawn(loc.clone().add(0, 11, 0));
        //miniTextDisplay.getDisplay().setPersistent(false);
        System.out.println("createPortalHolo: ADD MINI TEXT");

        portalHologramHandler.add(miniTextDisplay);
        System.out.println("createPortalHolo: DONE ADDING");
    }

    public void createAndRegisterPortals(Location loc) {
        this.leaveEventPortal = new Portal(loc, new PasteOffset(0, 8, 0), 3);//todo hardcode
        PortalToEventWorldHandler portalToEventWorldHandler = new PortalToEventWorldHandler(eventWorld, leaveEventPortal, calculator);
        //PortalToOverworldHandler portalToOverworldHandler = new PortalToOverworldHandler(Bukkit.getWorld("world"), leaveEventPortal);
        World world = Bukkit.getWorld("RaidWorld");
        PortalToOverworldHandler portalToOverworldHandler = new PortalToOverworldHandler(world, new Portal(new Location(world, 0, 0, 0), new PasteOffset(0, 0, 0), 3));
        //PortalToOverworldHandler portalToOverworldHandler = new PortalToOverworldHandler(world, leaveEventPortal); //todo фулл дерьмо. Неочевидная работа. 0 ооп. кринжекод, все в одной каше. кто не отрефачит тот пиздюк
        this.portalListener = new PortalListener(portalToOverworldHandler, portalToEventWorldHandler, eventWorld.getChestLocations());

        Bukkit.getServer().getPluginManager().registerEvents(portalListener, SatanicRaids.getInstance());
        System.out.println("Создаю портал. Координаты: " + leaveEventPortal.getPortalLocation().toString());
    }

    public void unregisterPortalListener() {
        HandlerList.unregisterAll(portalListener);
    }


    public void disable() {
        portalHologramHandler.removeAll();
        if (!stopTask.isCancelled()) {
            stopTask.cancel();
        }
    }
}
