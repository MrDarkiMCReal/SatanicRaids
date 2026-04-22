package org.mrdarkimc.raidsrecode.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mrdarkimc.raidsrecode.BossBarHandler;
//кринжуха но надо сделать быстро
public class BossbarListener implements Listener {
    public boolean isEventRunning = false;
    private static BossbarListener instance;


    public BossbarListener() {
        instance = this;
    }

    public static BossbarListener getInstance() {
        return instance;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!isEventRunning) {
            return;
        }
        BossBarHandler.getInstance().addEventBossbar(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        BossBarHandler.getInstance().removeEventBossbar(e.getPlayer());
    }

}
