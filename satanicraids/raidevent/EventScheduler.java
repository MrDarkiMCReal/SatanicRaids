package org.mrdarkimc.satanicraids.raidevent;

public interface EventScheduler {
    public boolean isAlreadyLaunched();
    public void announce(int timeToLaunch);
    public void startSchedule();
}
