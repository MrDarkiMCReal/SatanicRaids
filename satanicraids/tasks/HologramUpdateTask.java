package org.mrdarkimc.satanicraids.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.satanicraids.holograms.HologramHandler;
@Deprecated
public class HologramUpdateTask extends BukkitRunnable {
    private HologramHandler hologramHandler;

    public HologramUpdateTask(HologramHandler hologramHandler) {
        this.hologramHandler = hologramHandler;
    }

    @Override
    public void run() {
        updateHolos();
        //System.out.println("Updating holos. Taskid: " + this.getTaskId());
    }
    public void updateHolos(){
        hologramHandler.updateAll();
    }
}
