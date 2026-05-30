package org.mrdarkimc.raidsrecode.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mrdarkimc.raidsrecode.TaskHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskRunner {
    private final JavaPlugin plugin;
    private List<BukkitTask> taskHistory = new ArrayList<>();

    public TaskRunner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /* делай в секундах */
    public void runNext(Runnable r, int delay) {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                r.run();
            }
        }.runTaskLater(plugin, delay * 20L);
        taskHistory.add(bukkitTask);
    }

    public void killAllRunnedTasks() {
        for (BukkitTask bukkitTask : taskHistory) {
            TaskHelper.cancelTask(bukkitTask);
        }
        taskHistory.clear();
        taskHistory = null;
    }

    public void killAllRunnedTasks(int time) {
        Bukkit.getScheduler().runTaskLater(plugin, f -> killAllRunnedTasks(), time);
    }
}