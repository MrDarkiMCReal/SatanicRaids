package org.mrdarkimc.satanicraids.listeners.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class ChestItemTakeEvent extends Event {
    private InventoryClickEvent original;

    public ChestItemTakeEvent(InventoryClickEvent original) {
        this.original = original;
    }

    public InventoryClickEvent getEvent() {
        return original;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return new HandlerList();
    }
}
