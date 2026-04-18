package org.mrdarkimc.raidsrecode;

import org.bukkit.scheduler.BukkitTask;

public class TaskHelper {
    public static void cancelTask(BukkitTask task) {
        if (task == null) {
            return;
        }
        if (task.isCancelled()) {
            return;
        }
        task.cancel();
    }
}
