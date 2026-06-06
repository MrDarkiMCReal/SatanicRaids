package org.mrdarkimc.raidsrecode.portals.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.NotifyAPI.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.raidsrecode.EventListener;
import org.mrdarkimc.raidsrecode.portals.Portal;

import java.util.ArrayList;
import java.util.List;

public class PortalListener extends EventListener {
    private List<Portal> activePortals = new ArrayList<>();
    private World blockedWorld;

    public PortalListener(JavaPlugin plugin, World blockedWorld) {
        super(plugin);
        this.blockedWorld =blockedWorld;
    }

    @EventHandler(priority = EventPriority.HIGH)
    //todo refactor to cachedEnderPortalEvent
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        if (e.getTo().getWorld().getName().equals(blockedWorld.getName()) && !e.getFrom().getWorld().getName().equals(blockedWorld.getName())){
            //если тп совершается не из рейдового мира, но в рейдовый мир -> значит какая-то хуня.
            for (Portal activePortal : activePortals) {
                if (!activePortal.isAllowedToEnterPortal(player)) {
                    KeyedMessage.of("already-met").send(player);
                }
            }
        }
        boolean portalTpEvent = e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL);
        if (!portalTpEvent) {
            return;
        }

        player.getLocation();
        //todo добавить больше fail fast
        for (Portal activePortal : activePortals) {
            if (activePortal.isEnteringPortal(player)) {
                if (activePortal.isAllowedToEnterPortal(player)) {
                    activePortal.enter(player);
                } else {
                    KeyedMessage.of("already-met").send(player);
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
