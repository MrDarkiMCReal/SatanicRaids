package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
public abstract class EventListener implements Listener {
    protected final JavaPlugin plugin;

    protected boolean isRegistered;

    protected EventListener(JavaPlugin plugin) {
        this.plugin = plugin;
        isRegistered = false;
    }

    public EventListener register() {
        if (!isRegistered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            isRegistered = true;
        }
        return this;
    }

    public void unregister() {
        if (isRegistered) {
            HandlerList.unregisterAll(this);
            isRegistered = false;
        }
    }
}
