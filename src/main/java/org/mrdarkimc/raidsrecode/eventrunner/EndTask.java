package org.mrdarkimc.raidsrecode.eventrunner;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.events.RunnableEvent;

public class EndTask extends BukkitRunnable {
    private final RunnableEvent event;

    public EndTask(RunnableEvent event) {
        this.event = event;
    }

    @Override
    public void run() {
        event.stop();
    }
    public BukkitTask startTask(){
        return runTaskLater(SatanicRaids.getInstance(),event.getDuration());
    }
}
