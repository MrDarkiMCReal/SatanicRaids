package org.mrdarkimc.raidsrecode.eventrunner;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.api.RunnableEvent;

public class RunTask extends BukkitRunnable {
    private final RunnableEvent event;

    public RunTask(RunnableEvent event) {
        this.event = event;
    }

    @Override
    public void run() {
        event.start();
    }
    public BukkitTask startTask(){
        return runTask(SatanicRaids.getInstance());
    }
}
