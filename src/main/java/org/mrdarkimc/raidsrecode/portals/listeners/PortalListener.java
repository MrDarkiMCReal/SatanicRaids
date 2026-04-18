package org.mrdarkimc.raidsrecode.portals.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.raidsrecode.portals.Portal;

import java.util.ArrayList;
import java.util.List;

public class PortalListener implements Listener {
    private List<Portal> activePortals = new ArrayList<>();
    private final JavaPlugin plugin;

    private boolean isRegistered = false;

    public PortalListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    //todo refactor to cachedEnderPortalEvent
    public void onTeleport(PlayerTeleportEvent e) {
        boolean portalTpEvent = e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL);
        if (!portalTpEvent) {
            return;
        }
        Player player = e.getPlayer();
        player.getLocation();
        //todo добавить больше fail fast
        for (Portal activePortal : activePortals) {
            if (activePortal.isNearToPoral(player)) {
                e.setCancelled(true);
                activePortal.enter(player);
                break;
            }
        }
    }
    public void registerPortal(Portal portal){
        if (activePortals.contains(portal)){
            throw new RuntimeException("Portal already registred");
        }
        activePortals.add(portal);
    }
    public void unregisterPortal(Portal portal){
        if (!activePortals.contains(portal)){
            throw new RuntimeException("Portal is not registred");
        }
        activePortals.remove(portal);
    }
    public PortalListener register() {
        if (!isRegistered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            isRegistered = true;
        }
        return this;
    }

    // Отключение слушателя (все порталы перестанут работать)
    public void unregister() {
        if (isRegistered) {
            HandlerList.unregisterAll(this);
            activePortals.clear();
            isRegistered = false;
        }
    }
}
