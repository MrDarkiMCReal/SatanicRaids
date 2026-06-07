package org.mrdarkimc.raidsrecode.events.raidevent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.NotifyAPI.KeyedMessage;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePaster;
import org.mrdarkimc.enhancedtextdisplays.EnhancedTextDisplays;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.enhancedtextdisplays.displays.interfaces.DisplayHandler;
import org.mrdarkimc.raidsrecode.*;
import org.mrdarkimc.raidsrecode.EventListener;
import org.mrdarkimc.raidsrecode.api.AbstractEvent;
import org.mrdarkimc.raidsrecode.finders.AsyncLocationFinder;
import org.mrdarkimc.raidsrecode.finders.LocationFinder;
import org.mrdarkimc.raidsrecode.finders.PreparedLocationFinder;
import org.mrdarkimc.raidsrecode.listeners.RaidWorldListener;
import org.mrdarkimc.raidsrecode.portals.Portal;
import org.mrdarkimc.raidsrecode.portals.RaidPortal;
import org.mrdarkimc.raidsrecode.portals.listeners.PortalListener;
import org.mrdarkimc.raidsrecode.tasks.ChestUpdater;
import org.mrdarkimc.raidsrecode.tasks.RaidWorldKillAllTimer;

import java.util.*;

public class RaidEvent extends AbstractEvent {
    private static Location staticOutPortalLocation;
    private final WePaster raidWorldPartitionPaster;
    private static Location raidWorldLocation;
    private final List<Location> raidWorldSafeTeleportPoints;

    private final Portal portalIn;
    private final Portal portalOut;
    private PortalListener portalListener;
    private EventListener raidWorldListener;
    private final int timeToDeath = 60; //в секундах
    private WorthCalculator calculator;
    private Location portalLocation;
    //private ChestUpdateTask chestUpdateTask;
    private ChestUpdater chestUpdater;
    private final List<Player> visitedPlayers;
    private final BossBarHandler bossBarHandler;


    public RaidEvent(JavaPlugin plugin, EventDeserializer.PasteData raidWorldPasteData, List<Location> raidWorldSafeTeleportPoints, Portal portalIn, Portal portalOut, int eventDuration) {
        super(plugin, eventDuration);
        this.raidWorldPartitionPaster = raidWorldPasteData.paster();
        this.raidWorldSafeTeleportPoints = raidWorldSafeTeleportPoints;
        this.portalIn = portalIn;
        this.portalOut = portalOut;
        this.calculator = new WorthCalculator();
        this.visitedPlayers = new ArrayList<>();
        staticOutPortalLocation = new Location(Bukkit.getWorld("RaidWorld"), -11, -13, -11); //todo в будущем это можно будет заменить на no static
        raidWorldLocation = new Location(Bukkit.getWorld("RaidWorld"), 0, 0, 0);
        //
        LocationFinder finder = raidWorldPasteData.finder();
        if (finder instanceof PreparedLocationFinder prepared) {
            raidWorldLocation = prepared.find();//
            plugin.getLogger().info("Using static location for RaidEvent");
        } else {
            plugin.getLogger().warning("RaidEvent should have static location. Actual: " + finder.getClass().getSimpleName() + " Setting default loc: " + raidWorldLocation);
        }
        this.chestUpdater = createChestUpdater();
        this.bossBarHandler = new BossBarHandler(eventTimer);

    }

    //private PasterManager portalPaster = new SchemPasterManager(); //todo перейти на SpawnManager, добавить интерфейсы Undoable Spawnable и еще что то. next iteration
    //Так же уйти от задач, которые сами себя отменяют
    //private Clipboard portalClipboard = WGSchemLoader.clipboardMap.get("raidportal.schem");
    public static Location getRaidWorldLocation() {
        if (raidWorldLocation == null) {
            Bukkit.getLogger().warning("RaidWorldLocation is null while getting Raid location!! This is not the deal!");
        }
        return raidWorldLocation;
    }

    @Override
    public void start() {
        checkNotInitialized();
        setRunningStatus();
        eventTimer.startTask();

        portalIn.afterTeleportation(calculator::calculateJoin);
        portalIn.afterTeleportation(this::markAsVisitedRaidWorld);

        portalIn.setTeleportRequirements(this::hadNotVisitedRaidWorld);
        portalOut.afterTeleportation(p -> {
            calculator.calculateExit(p);
            p.sendTitlePart(TitlePart.TITLE, Component.text(""));
            p.sendTitlePart(TitlePart.SUBTITLE, Component.text(""));
            Title.Times times = Title.Times.times(Ticks.duration(10), Ticks.duration(70), Ticks.duration(10));
            p.sendTitlePart(TitlePart.TIMES, times);
        });

        taskRunner.runNext(this::prepareLocation, 0);
        taskRunner.runNext(this::clearAllPreviousHolos, 1);
        taskRunner.runNext(this::registerPortalListener, 2); //сразу
        taskRunner.runNext(this::registerRaidWorldListener, 2); //сразу
        taskRunner.runNext(this::prepareRaidWorld, 3);
        taskRunner.runNext(this::spawnPortalIn, 10);//10 сек
        taskRunner.runNext(this::sendBossbars, 15);//15 сек
        taskRunner.runNext(this::announceEventSpawned, 15);//15 сек
        taskRunner.runNext(this::startChestUpdatingTask, 20); //сразу //todo а может не сразу?
        taskRunner.runNext(this::spawnPortalOut, 25);//25 сек
chestUpdater.undo();
        taskRunner.runNext(this::prepareForEndEvent, eventDuration - timeToDeath); //за 60 сек до окончания
    }

    protected void startChestUpdatingTask() {
        eventTimer.addEachSecondUpdateTask(chestUpdater);
    }

    public void clearAllPreviousHolos() {
        DisplayHandler displayHandler = EnhancedTextDisplays.getInstance().getDisplayHandler();
        displayHandler.removeAll(staticOutPortalLocation.getWorld());
    }

    public void sendBossbars() {
        eventTimer.addEachSecondUpdateTask((s) -> bossBarHandler.updateText(portalLocation));
        bossBarHandler.setEnabled(portalLocation);
        bossBarHandler.addToAllPlayers();
    }

    public void killBossbars() {
        bossBarHandler.removeFromAllPlayers();
        bossBarHandler.setDisabled();
    }

    @Override
    public void stop() {
        setEndedStatus();
        taskRunner.runNext(portalOut::undo, 0);
        taskRunner.runNext(portalIn::undo, 1);
        taskRunner.runNext(() -> unregisterPortalListener(raidWorldListener), 2);
        taskRunner.runNext(() -> unregisterPortalListener(portalListener), 3);
        taskRunner.runNext(this::killBossbars, 4);
        taskRunner.runNext(this::setEndedStatus, 5);
        taskRunner.runNext(calculator::giveAwarsAndBroadcastStats, 6);
        taskRunner.runNext(() -> {
            this.calculator = null;
            this.chestUpdater = null;
            this.portalListener = null;
            this.raidWorldListener = null;
        }, 7);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            eventTimer.endTask();
            taskRunner.killAllRunnedTasks();
        }, 8 * 20L);//8*20 т.к на 8й секунде происходит полная инициализация всего остального

    }

    private void prepareForEndEvent() {
        ((RaidPortal) portalIn).destroyEndPortalBlocks(); //больше не пускаем игроков
        RaidWorldKillAllTimer task = new RaidWorldKillAllTimer(staticOutPortalLocation.getWorld(), timeToDeath);
        task.runTaskAndThen(null);
    }


    private void spawnPortalIn() {
        RaidPortal raidPortal = (RaidPortal) portalIn;
        raidPortal.setTimer(eventTimer);
        raidPortal.setDestinationPoints(raidWorldSafeTeleportPoints);
        raidPortal.spawn(portalLocation);
        portalListener.registerPortal(raidPortal);
    }

    private void spawnPortalOut() {
        ((RaidPortal) portalOut).setTimer(eventTimer);
        portalOut.setDestinationPoints(createOverworldSafePoints(portalLocation));
        portalOut.spawn(staticOutPortalLocation);
        portalListener.registerPortal(portalOut);
    }

    //todo с оффсетом нужно //todo в аргументы портал. (или перенести метод в портал)
    private List<Location> createOverworldSafePoints(Location center) {
        int capacity = 10;
        List<Location> locations = new ArrayList<>(capacity);
        Random random = new Random();
        for (int i = 0; i < capacity; i++) {
            int x = random.nextInt(-20, 20);
            int y = random.nextInt(-20, 20);
            Location closeRandomCoords = center.clone().add(x, 0, y);
            locations.add(closeRandomCoords.getWorld().getHighestBlockAt(closeRandomCoords).getLocation());
        }
        return locations;
    }


    private String formatLocation(Location loc) {
        return String.format("%d, %d, %d", (int) loc.getX(), (int) loc.getY(), (int) loc.getZ());
    }

    private void prepareRaidWorld() {
        raidWorldPartitionPaster.paste(raidWorldLocation);
    }

    @Override
    protected void prepareLocation() {
        Location center = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        LocationFinder asyncFinder = AsyncLocationFinder.newBuilder()
                .center(center)
                .maxAttempts(20)
                .radius(3000)
                .onFound(l -> this.portalLocation = l)
                .build();
        asyncFinder.find();
    }

    private ChestUpdater createChestUpdater() {
        FileConfiguration holoConfig = SatanicRaids.getInstance().getHologramConfig().get();
        ConfigurationSection chestHoloConf = holoConfig.getConfigurationSection("textdisplays.templateChest");
        MiniTextDisplay chestHoloTemplate = MiniTextDisplay.fromConfig(chestHoloConf);
        return new ChestUpdater(chestHoloTemplate);

    }

    private EventListener registerRaidWorldListener() {
        if (raidWorldListener != null) {
            throw new RuntimeException();
        }
        raidWorldListener = new RaidWorldListener(plugin);
        raidWorldListener.register();
        return raidWorldListener;
    }

    private PortalListener registerPortalListener() {
        if (portalListener != null) {
            throw new RuntimeException();
        }
        portalListener = new PortalListener(plugin, raidWorldLocation.getWorld());
        portalListener.register();
        return portalListener;
    }

    private void unregisterPortalListener(EventListener listener) {
        if (listener == null) {
            Bukkit.getLogger().warning("Слушатель не может быть разрегистрирован т.к он нулл");
            return;
        }
        listener.unregister();
    }

    private void announceEventSpawned() {
        KeyedMessage.of("raids-event-start").withPlaceholders(Map.of("{location}", formatLocation(portalLocation))).broadcast();
    }

    private void markAsVisitedRaidWorld(Player player) {
        visitedPlayers.add(player);
    }

    private boolean hadNotVisitedRaidWorld(Player player) {
        return !visitedPlayers.contains(player);
    }

    @Override
    public int getDuration() {
        return eventDuration;
    }
}
