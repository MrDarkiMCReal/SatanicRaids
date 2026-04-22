package org.mrdarkimc.raidsrecode.events;

import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.raidsrecode.EventListener;
import org.mrdarkimc.raidsrecode.EventTimer;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEvent implements RunnableEvent {
    protected final JavaPlugin plugin;
    protected final int eventDuration;
    private boolean isInitialized = false;
    private boolean isEnded = false;
    protected TaskRunner taskRunner;
    protected EventTimer eventTimer;
    protected List<EventListener> listeners;

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
    protected void registerListeners(){
        listeners.forEach(EventListener::register);
    }
    protected void unregisterListeners(){
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
            new Message(null, "[Ошибка] Эвент уже запущен. Сначала отмените событие. Сообщите админу", null).sendToPlayersWithPermission("satanic.helper");
            throw new RuntimeException();
        }
    }

    @Override
    public boolean isEnded() {
        return isEnded;
    }
}
