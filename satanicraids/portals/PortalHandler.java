//package org.mrdarkimc.satanicraids.portals;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.World;
//import org.bukkit.entity.Player;
//import org.bukkit.util.Vector;
//import org.mrdarkimc.satanicraids.SatanicRaids;
//import org.mrdarkimc.satanicraids.raidevent.EventWorld;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Обрабатывает порталы для событий:
// * - Входной портал в обычном мире для входа в эвентовый мир
// * - Выходной портал в эвентовом мире для выхода обратно
// */
//public class PortalHandler {
//    private Location activeEntryPortal; // Порталы в обычном мире для входа
//    private int entryRadius;
//    private Location activeExitPortal; // Портал в эвентовом мире для выхода (центр карты)
//    private int exitRadius;
//    private EventWorld eventWorld;
//    private final Map<UUID, Boolean> allowedTeleports = new HashMap<>(); // Флаг разрешенной телепортации
//    private Location defaultSpawn; //todo телепортировать в радиусе 20 блоков от портала
//    private Offset entryPortalOffset = new Offset(0,8,0);
//    public void closePortalToEventWorld(){
//        //todo
//    }
//    public PortalHandler(EventWorld eventWorld) {
//        this.eventWorld = eventWorld;
//        World spawnWorld = Bukkit.getWorld("spawn");
//        if (spawnWorld != null) {
//            this.defaultSpawn = spawnWorld.getSpawnLocation();
//        } else {
//            World world = Bukkit.getWorld("world");
//            if (world != null) {
//                this.defaultSpawn = world.getSpawnLocation();
//            }
//        }
//    }
//
//    /**
//     * Регистрирует входной портал в обычном мире
//     */
//    public void registerEntryPortal(Location loc, int radius) {
//        this.activeEntryPortal = loc;
//        this.entryRadius = radius;
//    }
//
//    /**
//     * Регистрирует выходной портал в эвентовом мире (центр карты)
//     */
//    public void registerExitPortal(Location loc, int radius) {
//        this.activeExitPortal = loc;
//        this.exitRadius = radius;
//    }
//
//    /**
//     * Проверяет, находится ли игрок в радиусе входного портала
//     */
//    public boolean isTeleportToEvent(Player player) {
//        if (activeEntryPortal == null || eventWorld == null) {
//            return false;
//        }
//        Location playerLocation = player.getLocation();
//        // Проверяем, что игрок в том же мире
//        if (!playerLocation.getWorld().equals(activeEntryPortal.getWorld())) {
//            return false;
//        }
//        Vector offset = entryPortalOffset.toVector();
//        Bukkit.getLogger().info("[SatanicRaids] Vectorized location: " + offset);
//
//        // Клонируем локацию перед добавлением
//        Location add = activeEntryPortal.clone().add(offset);
//        Bukkit.getLogger().info("[SatanicRaids] Total loc: " + add);
//        double distance = playerLocation.distance(add);
//        return distance <= entryRadius;
//    }
//
//    public boolean isExitTeleport(Player player) {
//        if (activeExitPortal == null) {
//            return false;
//        }
//        Location playerLocation = player.getLocation();
//        if (!playerLocation.getWorld().equals(activeExitPortal.getWorld())) {
//            return false;
//        }
//        double distance = playerLocation.distance(activeExitPortal);
//        return distance <= exitRadius;
//    }
//
//    public void allowTeleport(Player player) {
//        allowedTeleports.put(player.getUniqueId(), true);
//    }
//
//    public void denyTeleport(Player player) {
//        allowedTeleports.remove(player.getUniqueId());
//    }
//
//    public void unregisterEntryPortal() {
//        this.activeEntryPortal = null;
//    }
//
//    public void unregisterExitPortal() {
//        this.activeExitPortal = null;
//    }
//
//    public void addPlayerToEventWorld(Player player) {
//        if (eventWorld != null) {
//            eventWorld.teleportIntoEvent(player);
//            // Добавляем игрока в контейнер события через главный класс
//            //SatanicRaids plugin = SatanicRaids.getInstance();
//            //if (plugin != null && plugin.getEventContainer() != null) {
//            //    plugin.getEventContainer().addPlayer(player);
//            //}
//        }else {
//            System.out.println(String.format("[%s] Мир почему то нулл.",getClass().getSimpleName()));
//        }
//    }
//
//    public void removePlayerFromEventWorld(Player player) {
//        if (defaultSpawn != null) {
//
//            player.teleport(defaultSpawn);
//        }
//    }
//
//    /**
//     * Проверяет, является ли телепортация из эвентового мира в обычный разрешенной
//     */
//    public boolean isTeleportFromEventToNormalWorld(World from, World to, Player player) {
//        if (eventWorld == null || eventWorld.getRaidWorld() == null) {
//            return false;
//        }
//
//        // Проверяем, что телепортируемся из эвентового мира
//        boolean fromEventWorld = from.getName().equals(eventWorld.getRaidWorld().getName());
//
//        // Проверяем, что телепортируемся в обычный мир (не эвентовый)
//        boolean toNormalWorld = to == null || !to.getName().equals(eventWorld.getRaidWorld().getName());
//
//        // Если из эвента в обычный мир, но не разрешено - блокируем
//        if (fromEventWorld && toNormalWorld) {
//            return !allowedTeleports.getOrDefault(player.getUniqueId(), false);
//        }
//
//        return false;
//    }
//
//    public void setEventWorld(EventWorld eventWorld) {
//        this.eventWorld = eventWorld;
//    }
//
//    public Location getActiveEntryPortal() {
//        return activeEntryPortal;
//    }
//
//    public Location getActiveExitPortal() {
//        return activeExitPortal;
//    }
//}
