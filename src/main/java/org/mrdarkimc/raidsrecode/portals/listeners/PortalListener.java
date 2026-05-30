package org.mrdarkimc.raidsrecode.portals.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.raidsrecode.EventListener;
import org.mrdarkimc.raidsrecode.portals.Portal;

import java.util.ArrayList;
import java.util.List;

public class PortalListener extends EventListener {
    private List<Portal> activePortals = new ArrayList<>();

    public PortalListener(JavaPlugin plugin) {
        super(plugin);
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
            if (activePortal.isEnteringPortal(player)) {
                if (activePortal.isAllowedToEnterPortal(player)) {
                    activePortal.enter(player);
                } else {
                    new KeyedMessage(player, "messages.already-met", null).send();
                }
                e.setCancelled(true);
                break;

            }
        }
    }

    public void registerPortal(Portal portal) {
        if (activePortals.contains(portal)) {
            throw new RuntimeException("Portal already registred");
        }
        activePortals.add(portal);
    }

    public void unregisterPortal(Portal portal) {
        if (!activePortals.contains(portal)) {
            throw new RuntimeException("Portal is not registred");
        }
        activePortals.remove(portal);
    }


    @Override
    public void unregister() {
        activePortals.clear();
        super.unregister();
    }
}
