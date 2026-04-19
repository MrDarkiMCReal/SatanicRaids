package org.mrdarkimc.raidsrecode.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.SatanicLib.worldedit.pasters.WePaster;
import org.mrdarkimc.enhancedtextdisplays.displays.MiniTextDisplay;
import org.mrdarkimc.raidsrecode.EventDeserializer;
import org.mrdarkimc.raidsrecode.EventListener;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.WorthCalculator;
import org.mrdarkimc.raidsrecode.finders.AsyncLocationFinder;
import org.mrdarkimc.raidsrecode.finders.LocationFinder;
import org.mrdarkimc.raidsrecode.finders.PreparedLocationFinder;
import org.mrdarkimc.raidsrecode.hooks.we.PasterManager;
import org.mrdarkimc.raidsrecode.listeners.RaidWorldListener;
import org.mrdarkimc.raidsrecode.portals.Portal;
import org.mrdarkimc.raidsrecode.portals.RaidPortal;
import org.mrdarkimc.raidsrecode.portals.listeners.PortalListener;
import org.mrdarkimc.raidsrecode.tasks.ChestUpdateTask;
import org.mrdarkimc.raidsrecode.tasks.ChestUpdater;
import org.mrdarkimc.raidsrecode.tasks.RaidWorldKillAllTimer;

import java.util.*;

public class RaidEvent extends AbstractEvent {
    private static Location staticOutPortalLocation = new Location(Bukkit.getWorld("RaidWorld"), 0, 20, 0); //todo в будущем это можно будет заменить на no static
    private final WePaster raidWorldPartitionPaster;
    private Location raidWorldLocation = new Location(Bukkit.getWorld("RaidWorld"), 0, 0, 0);
    private final List<Location> raidWorldSafeTeleportPoints;
    private final Portal portalIn;
    private final Portal portalOut;
    private PortalListener portalListener;
    private EventListener raidWorldListener;
    private final int timeToDeath = 30; //в секундах
    /** Задержки TaskRunner.runNext(..., delay) в секундах — должны совпадать с вызовами ниже. */
    private static final int PORTAL_IN_SPAWN_DELAY_SEC = 10;
    private static final int PORTAL_OUT_SPAWN_DELAY_SEC = 25;
    private WorthCalculator calculator;
    private Location portalLocation;
    private ChestUpdateTask chestUpdateTask;
    private ChestUpdater chestUpdater;
    private Runnable chestEachSecondTick;
    private List<Player> visitedPlayers;



    public RaidEvent(JavaPlugin plugin, EventDeserializer.PasteData data, List<Location> raidWorldSafeTeleportPoints, Portal portalIn, Portal portalOut, int eventDuration) {
        super(plugin, eventDuration);
        this.raidWorldPartitionPaster = data.paster();
        this.raidWorldSafeTeleportPoints = raidWorldSafeTeleportPoints;
        this.portalIn = portalIn;
        this.portalOut = portalOut;
        this.calculator = new WorthCalculator();
        this.visitedPlayers = new ArrayList<>();
        //
        LocationFinder finder = data.finder();
        if (finder instanceof PreparedLocationFinder prepared) {
            this.raidWorldLocation = prepared.find();//
        } else {
            plugin.getLogger().warning("RaidEvent should have static location. Actual: " + finder.getClass().getSimpleName() + " Setting default loc: " + raidWorldLocation);
        }
        this.chestUpdater = createChestUpdater();
    }
    //private PasterManager portalPaster = new SchemPasterManager(); //todo перейти на SpawnManager, добавить интерфейсы Undoable Spawnable и еще что то. next iteration
    //Так же уйти от задач, которые сами себя отменяют


    //private Clipboard portalClipboard = WGSchemLoader.clipboardMap.get("raidportal.schem");


    public void sendAnnounceMessage() {
//за 15 сек до начала
        new Message(null, "Эвент рейдового мира скоро заспавнится", null).broadcast();
    }

    private void markAsVisitedRaidWorld(Player player) {
        visitedPlayers.add(player);
    }

    private boolean hadNotVisitedRaidWorld(Player player) {
        return !visitedPlayers.contains(player);
    }

    @Override
    public void start() {
        checkNotInitialized();
        setRunningStatus();
        eventTimer.startTask();

        portalIn.afterTeleportation(p -> calculator.calculateJoin(p));
        portalIn.afterTeleportation(this::markAsVisitedRaidWorld);
        portalIn.setTeleportRequirements(this::hadNotVisitedRaidWorld);

        portalOut.afterTeleportation(p -> calculator.calculateExit(p));


        //eventTimer.addEachSecondUpdateTask(() -> chestUpdater.nextSecound(eventTimer));



        //prepareLocationAndSpawnRaidWorld();
        //sendAnnounceMessage();
        //registerPortalListener();
        //todo переделать на обьекты типа EventAction и просто собирать их в мапу EventAction / time
        //задачи в одинаковый тик не гарантированы.
        taskRunner.runNext(this::prepareLocationAndSpawnRaidWorld, 0);

        //taskRunner.runNext(this::startHoloUpdateTask, 0); //сразу
        this.chestEachSecondTick = () -> chestUpdater.nextSecound(eventTimer);
        taskRunner.runNext(() -> eventTimer.addEachSecondUpdateTask(chestEachSecondTick), 0); //сразу //todo а может не сразу?
        //taskRunner.runNext(() -> eventTimer.addEachSecondUpdateTask(() -> chestUpdater.nextSecound(eventTimer)), 0); //сразу
        taskRunner.runNext(this::sendAnnounceMessage, 1); //сразу
        taskRunner.runNext(this::registerPortalListener, 2); //сразу
        taskRunner.runNext(this::registerRaidWorldListener, 2); //сразу
        taskRunner.runNext(this::spawnPortalIn, PORTAL_IN_SPAWN_DELAY_SEC);//10 сек
        taskRunner.runNext(this::spawnPortalOut, PORTAL_OUT_SPAWN_DELAY_SEC);//25 сек
        taskRunner.runNext(this::announceEventSpawned, 15);//15 сек
        //todo вот тут просто запланировать таски.
        //таск на аннаунсер, таск на подготовку, еще какие то таски
        //Выделить 1 минуту на подготовку, вставка схем, порталов и т.д

        //Вставляем большой данж в мир RaidWorld
        //Вот этот пастер запускает 4 BukkitRunnable и вставляет за 4 раза постройку

        //Ищем рандомную локацию для вставки портала в overWorld, который ведет в RaidWorld
        //Location ; //LocationFinder - функциональный интерфейс

        //todo может запускать все в асинке и просте в реализации делаеть через Runnable
        Bukkit.getLogger().info("portal in : " + portalIn);
        Bukkit.getLogger().info("portal out : " + portalOut);
        Bukkit.getLogger().info("portal location : " + portalLocation);

        //Спавним входной и выходной порталы
        //Так же они запускают таски которые создают и обновляют голограмму над порталами через TextDisplay


        //todo Регистрация порталов, что бы они могли телепортировать игроков
        //portalIn - портал в мире overWorld который ведет в RaidWorld
        //portalOut - портал в мире RaidWorld который ведет в overWorld


    }

    public void fullEnd(Void unused) {
        broadcastStats();
    }

    private void broadcastStats() {
        Map<Player, Long> top = calculator.getTop(3);
        System.out.println("top players: ");
        System.out.println(top);
        int counter = 0;
        List<String> s = new ArrayList<>(List.of(" \n", " \n", " \n"));
        for (Map.Entry<Player, Long> entry : top.entrySet()) {
            if (counter == 2) {
                s.set(counter, formatPlayerTop(entry.getKey(), entry.getValue()));
            } else {
                s.set(counter, formatPlayerTop(entry.getKey(), entry.getValue()) + "\n");
            }
            counter++;
        }
        new KeyedMessage(null, "messages.event-end", Map.of("{players}", String.join(" ", s))).broadcast();
    }

    private String formatPlayerTop(Player player, long sum) {
        String template = "&f             %pFormat_{player}_mini% %design_clr_primary%вынес &f%img_money%%design_clr_money%{money}";
        return template.replace("{player}", player.getName()).replace("{money}", formatBal(sum));
    }

    private String formatBal(long sum) {
        String sumStr = String.valueOf(sum);
        char[] charArray = sumStr.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = charArray.length - 1; i > 0; i--) {
            builder.append(charArray[i]);
            if (i % 3 == 0) {
                builder.append(",");
            }
        }
        return builder.reverse().toString();
    }


    public void startHoloUpdateTask() {


    }

    public void spawnPortalIn() {
        // Один общий момент окончания эвента: spawnDelay + lifeTime == eventDuration (см. EndTask).
        int portalOpenSeconds = Math.max(1, eventDuration - PORTAL_IN_SPAWN_DELAY_SEC);
        ((RaidPortal) portalIn).setDuration(portalOpenSeconds);
        portalIn.setDestinationPoints(raidWorldSafeTeleportPoints);
        portalIn.spawn(portalLocation);
        portalListener.registerPortal(portalIn);
    }

    public void spawnPortalOut() {
        int portalOpenSeconds = Math.max(1, eventDuration - PORTAL_OUT_SPAWN_DELAY_SEC);
        ((RaidPortal) portalOut).setDuration(portalOpenSeconds);
        portalOut.setDestinationPoints(createOverworldSafePoints(portalLocation));
        portalOut.spawn(staticOutPortalLocation);
        portalListener.registerPortal(portalOut);
    }

    //todo с оффсетом нужно //todo в аргументы портал. (или перенести метод в портал)
    public void destroyEndPortalBlocks(Location overWorldPortalLocation) {
        World world = overWorldPortalLocation.getWorld();
        int centerX = overWorldPortalLocation.getBlockX();
        int centerY = overWorldPortalLocation.getBlockY();
        int centerZ = overWorldPortalLocation.getBlockZ();

        // Уничтожаем все блоки в квадрате 3x3
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                Location blockLocation = new Location(world, x, centerY, z);
                blockLocation.getBlock().setType(Material.AIR);
            }
        }
    }

    public List<Location> createOverworldSafePoints(Location center) {
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

    public void announceEventSpawned() {
        new KeyedMessage(null, "messages.event-start", Map.of("{location}", formatLocation(portalLocation))).broadcast();
    }

    private String formatLocation(Location loc) {
        return String.format("%d, %d, %d", (int) loc.getX(), (int) loc.getY(), (int) loc.getZ());
    }

    //Запускаем метод за 2 минуты до старта события.
    //Подготавливаем локацию, что бы к моменту начала события
    //Локация уже была найдена
    public void prepareLocationAndSpawnRaidWorld() {
        Location center = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        LocationFinder asyncFinder = AsyncLocationFinder.newBuilder()
                .center(center)
                .maxAttempts(20)
                .radius(3000)
                .onFound(l -> this.portalLocation = l)
                .build();
        asyncFinder.find();
        raidWorldPartitionPaster.paste(raidWorldLocation);
        //мне нужно, что бы задача выполнялась в отдельном потоке
        //но когда она была выыполнена что бы в главном потоке было присвоено this.portalLocation = результат выполнения задачи (поиска локации)
    }

    public ChestUpdateTask createChestsTask() {
        FileConfiguration holoConfig = SatanicRaids.getInstance().getHologramConfig().get();
        ConfigurationSection chestHoloConf = holoConfig.getConfigurationSection("textdisplays.templateChest");
        MiniTextDisplay chestHoloTemplate = MiniTextDisplay.fromConfig(chestHoloConf);
        return new ChestUpdateTask(chestHoloTemplate, eventDuration);
    }
    public ChestUpdater createChestUpdater() {
        FileConfiguration holoConfig = SatanicRaids.getInstance().getHologramConfig().get();
        ConfigurationSection chestHoloConf = holoConfig.getConfigurationSection("textdisplays.templateChest");
        MiniTextDisplay chestHoloTemplate = MiniTextDisplay.fromConfig(chestHoloConf);
        return new ChestUpdater(chestHoloTemplate);

    }

    public EventListener registerRaidWorldListener() {
        if (raidWorldListener != null) {
            throw new RuntimeException();
        }
        raidWorldListener = new RaidWorldListener(plugin);
        raidWorldListener.register();
        return raidWorldListener;
    }

    public PortalListener registerPortalListener() {
        if (portalListener != null) {
            throw new RuntimeException();
        }
        portalListener = new PortalListener(plugin);
        portalListener.register();
        return portalListener;
    }

    public void unregisterPortalListener(EventListener listener) {
        if (listener == null) {
            Bukkit.getLogger().warning("Слушатель не может быть разрегистрирован т.к он нулл");
            return;
        }
        listener.unregister();
    }


    @Override
    public void stop() {
        //todo порталы деспавнить тоже в несколько задач?
        destroyEndPortalBlocks(portalLocation); //больше не пускаем игроков
        new BukkitRunnable() {

            @Override
            public void run() {
                portalOut.undo();
                portalIn.undo();
                unregisterPortalListener(raidWorldListener);
                unregisterPortalListener(portalListener);
                portalListener = null;
                raidWorldListener = null;
                if (chestEachSecondTick != null) {
                    eventTimer.removeEachSecondUpdateTask(chestEachSecondTick);
                }
                if (chestUpdateTask != null) {
                    chestUpdateTask.endTask();
                }
                //delete portals
                //stop holograms
                setEndedStatus();
            }
        }.runTaskLater(plugin, timeToDeath);

        RaidWorldKillAllTimer task = new RaidWorldKillAllTimer(staticOutPortalLocation.getWorld(), timeToDeath);
        task.runTaskAndThen(this::fullEnd);
    }

    @Override
    public int getDuration() {
        return eventDuration;
    }


    //todo отрефакторить на EventDescription
    //Он будет содержать информацию о всех подготовках
    //Названии эвента, переодичность, анаунсерах и т.д
    //Все что напрямую не относится к внутриигровому событию данжа
//    @Override
//    public Map<Long, Runnable> getPreparations() {
//        Map<Long, Runnable> preps = new HashMap<>();
//
//        preps.put(1200L, this::prepareLocationAndSpawnRaidWorld);
//
//        preps.put(6000L, () -> {
//            if (portalLocation != null) {
//                Map<String, String> placeholders = Map.of(
//                        "{x}", String.valueOf(portalLocation.getX()),
//                        "{z}", String.valueOf(portalLocation.getZ())
//                );
//                new KeyedMessage(null, "event.starting_soon", placeholders)
//                        .broadcast();
//            }
//        });
//        return preps;
//    }
}
