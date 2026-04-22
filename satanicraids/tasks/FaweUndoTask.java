package org.mrdarkimc.satanicraids.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.satanicraids.SatanicRaids;

import java.util.function.Consumer;

public class FaweUndoTask extends BukkitRunnable {
    private final Consumer<Void> consumer;

    public FaweUndoTask(Consumer<Void> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void run() {
        consumer.accept(null);
    }
    public BukkitTask runTaskLater(long delay){
        return this.runTaskLater(SatanicRaids.getInstance(),delay);
    }
}
