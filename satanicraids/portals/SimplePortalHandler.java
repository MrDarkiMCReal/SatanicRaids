package org.mrdarkimc.satanicraids.portals;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class SimplePortalHandler<WORLD> {
    protected Portal portal;
    protected WORLD world;
    protected final Map<UUID, Boolean> allowedTeleports;

    public SimplePortalHandler(WORLD world, Portal portal) {
        this.world = world;
        this.portal = portal;
        this.allowedTeleports = new HashMap<>();
    }

    public Portal getPortal() {
        return portal;
    }

    public boolean isTeleportUsingLocalPortal(Player player) {
        if (portal == null) {
            System.out.println("Portal is null for some reason");
            return false;
        }
        return portal.isNearEnoughForTeleport(player);
    }

    public void allowTeleport(Player player) {
        allowedTeleports.put(player.getUniqueId(), true);
    }

    public void denyTeleport(Player player) {
        allowedTeleports.remove(player.getUniqueId());
    }
    public abstract void enter(Player player);
//    {
//        if (world != null) {
//            world.teleportIntoEvent(player);
//            // Добавляем игрока в контейнер события через главный класс
//            //SatanicRaids plugin = SatanicRaids.getInstance();
//            //if (plugin != null && plugin.getEventContainer() != null) {
//            //    plugin.getEventContainer().addPlayer(player);
//            //}
//        }else {
//            System.out.println(String.format("[%s] Мир почему то нулл.",getClass().getSimpleName()));
//        }
//    }
    public abstract void exit(Player player);
   //{
   //    if (defaultSpawn != null) {

   //        player.teleport(defaultSpawn);
   //    }
   //}
    //public boolean isTeleportFromEventToNormalWorld(World from, World to, Player player) { //todo
    //    if (eventWorld == null || eventWorld.getRaidWorld() == null) {
    //        return false;
    //    }
//
    //    // Проверяем, что телепортируемся из эвентового мира
    //    boolean fromEventWorld = from.getName().equals(eventWorld.getRaidWorld().getName());
//
    //    // Проверяем, что телепортируемся в обычный мир (не эвентовый)
    //    boolean toNormalWorld = to == null || !to.getName().equals(eventWorld.getRaidWorld().getName());
//
    //    // Если из эвента в обычный мир, но не разрешено - блокируем
    //    if (fromEventWorld && toNormalWorld) {
    //        return !allowedTeleports.getOrDefault(player.getUniqueId(), false);
    //    }
//
    //    return false;
    //}
}
