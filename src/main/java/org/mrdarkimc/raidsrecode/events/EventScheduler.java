package org.mrdarkimc.raidsrecode.events;

public interface EventScheduler {
//    public boolean isAlreadyLaunched();
//    public void announce(int timeToLaunch);
    public void startSchedule();
    public void stopSchedule();
    public void stopCurrentEvent();
}
