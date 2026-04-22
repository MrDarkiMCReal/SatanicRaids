package org.mrdarkimc.satanicraids.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * todo refactor в либу
 */
public class EndPortalListener implements Listener {
    private final Map<UUID, Long> activePortalTouches = new HashMap<>();
    private final int cacheTime = 50 * 20 * 3; //(50 ms in 1 tick) * (20 ticks) * 3 = 3 sec

    //todo сделать List<Event> и таску, которая будет раз в n секунд забирать эвент

    @EventHandler
    public void onEndPortalTouch(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            cachePlayerTeleport(player, event);
        }
    }

    private void cachePlayerTeleport(Player player, PlayerTeleportEvent event) {
        if (activePortalTouches.containsKey(player.getUniqueId())) {
            if (isStillCached(player)) {
                return;
            }
        }
        activePortalTouches.put(player.getUniqueId(), System.currentTimeMillis() + cacheTime);
        Bukkit.getPluginManager().callEvent(new CachedEndPortalTeleportEvent(event));
    }

    private boolean isStillCached(Player player) {
        long resettime = activePortalTouches.get(player.getUniqueId());
        return resettime <= System.currentTimeMillis();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        activePortalTouches.remove(e.getPlayer().getUniqueId()); //todo возможно будет срабатывать сначала удаление игрока,
//потом выкидывание кастом эвента, соответственно игрока телепортирует в энд а потом он ливнет.
//и получится, что сохранится та локация, в которую он случайно попал при баге с телепортом 
    }

}