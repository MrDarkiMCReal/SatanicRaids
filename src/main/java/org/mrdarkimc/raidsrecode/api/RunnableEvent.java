package org.mrdarkimc.raidsrecode.events;

public interface RunnableEvent {
    public abstract boolean isEnded();
    public abstract void start();

    /**
     * Завершает событие
     * @return Значение в секундах, после которого эвент считается завершенным
     */
    public abstract void stop();
    public abstract int getDuration(); //in seconds
}
