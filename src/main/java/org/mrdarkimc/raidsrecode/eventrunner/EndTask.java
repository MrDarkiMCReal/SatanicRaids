package org.mrdarkimc.raidsrecode.eventrunner;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.raidsrecode.SatanicRaids;
import org.mrdarkimc.raidsrecode.api.RunnableEvent;

public class EndTask extends BukkitRunnable {
    private final RunnableEvent event;
    private Runnable andThen;

    public EndTask(RunnableEvent event) {
        this.event = event;
    }
    public void afterEnd(Runnable rn){
        this.andThen = rn;
    }

    @Override
    public void run() {
        event.stop();
        if (andThen!=null){
            andThen.run();
        }
    }
    public BukkitTask startTask(){
        return runTaskLater(SatanicRaids.getInstance(),event.getDuration() * 20L);
    }
}
