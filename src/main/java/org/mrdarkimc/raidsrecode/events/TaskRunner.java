package org.mrdarkimc.raidsrecode.events;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskRunner{
    private final JavaPlugin plugin;

    public TaskRunner(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void runNext(Runnable r, int delay){
        new BukkitRunnable() {
            @Override
            public void run() {
                r.run();
            }
        }.runTaskLater(plugin,delay * 20L);
    }
}