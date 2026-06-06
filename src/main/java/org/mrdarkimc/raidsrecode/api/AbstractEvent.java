package org.mrdarkimc.raidsrecode.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.ConfigAPI.MessagesConfig;
import org.mrdarkimc.SatanicLib.NotifyAPI.MessageDispatcher;
import org.mrdarkimc.SatanicLib.NotifyAPI.messages.chat.ChatMessage;
import org.mrdarkimc.raidsrecode.EventListener;
import org.mrdarkimc.raidsrecode.EventTimer;
import org.mrdarkimc.raidsrecode.finders.AsyncLocationFinder;
import org.mrdarkimc.raidsrecode.finders.LocationFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractEvent implements RunnableEvent {
    protected final JavaPlugin plugin;
    protected final int eventDuration;
    private boolean isInitialized = false;
    private boolean isEnded = false;
    protected TaskRunner taskRunner;
    protected EventTimer eventTimer;

    protected List<EventListener> listeners;
    protected Location eventLocation;

    protected AbstractEvent(JavaPlugin plugin, int eventDuration) {
        this.plugin = plugin;
        this.eventDuration = eventDuration;
        this.taskRunner = new TaskRunner(plugin);
        this.eventTimer = new EventTimer(plugin, eventDuration);
        this.listeners = new ArrayList<>();
    }

    protected void setRunningStatus() {
        isInitialized = true;
    }

    protected void registerListeners() {
        listeners.forEach(EventListener::register);
    }

    protected void unregisterListeners() {
        listeners.forEach(EventListener::unregister);
    }

    protected boolean isAlreadyRunning() {
        return isInitialized;
    }

    protected void setEndedStatus() {
        isEnded = true;
    }

    protected void checkNotInitialized() {
        if (isAlreadyRunning()) {
            new MessageDispatcher(new ChatMessage("[Ошибка] Эвент уже запущен. Сначала отмените событие. Сообщите админу")).toHelpers();
            throw new RuntimeException();
        }
    }

    @Override
    public int getDuration() {
        return eventDuration;
    }

    @Override
    public boolean isEnded() {
        return isEnded;
    }

    protected void prepareLocation(Consumer<Location> onFound) {
        Location center = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        LocationFinder asyncFinder = AsyncLocationFinder.newBuilder()
                .center(center)
                .maxAttempts(20)
                .radius(3000)
                .onFound(onFound)
                .build();
        asyncFinder.find();
    }

    protected void prepareLocation() {
        prepareLocation(l -> this.eventLocation = l);
    }
}
