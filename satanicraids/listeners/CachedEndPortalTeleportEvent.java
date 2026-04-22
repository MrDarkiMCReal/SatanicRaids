package org.mrdarkimc.satanicraids.listeners;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Срабатывает не на каждое касание, а на каждые 3 секунды
 * todo refactor в либу
 */
public class CachedEndPortalTeleportEvent extends Event {
    private PlayerTeleportEvent originalEvent;

    public CachedEndPortalTeleportEvent(PlayerTeleportEvent originalEvent) {
        this.originalEvent = originalEvent;
    }

    public PlayerTeleportEvent getOriginEvent() {
        return this.originalEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return new HandlerList();
    }
}