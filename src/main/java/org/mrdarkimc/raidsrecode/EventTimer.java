package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.tasks.CountDownTask;

import java.util.ArrayList;
import java.util.List;

public class EventTimer extends CountDownTask {
    private List<Runnable> additionalTasks = new ArrayList<>();

    public EventTimer(JavaPlugin plugin, int totalDuration) {
        super(plugin, totalDuration);
    }

    public int getCurrentTime() {
        return current;
    }

    @Override
    public void work() {
        additionalTasks.forEach(Runnable::run);
    }

    public void addEachSecondUpdateTask(Runnable runnable) {
        additionalTasks.add(runnable);
    }

    public void removeEachSecondUpdateTask(Runnable runnable) {
        boolean removed = additionalTasks.remove(runnable);
        if (!removed) {
            Bukkit.getLogger().warning("Attempting to delete repeatable task failed. No tasks were deleted");
        }
    }
    public String getFormattedTime() {
        return getFormattedTime(current);
    }

    public String getFormattedTime(int current) {
        if (current <= 0) return "сейчас";

        int days = current / 86400;
        int hours = (current % 86400) / 3600;
        int minutes = (current % 3600) / 60;
        int seconds = current % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append(" дн. ");
        if (hours > 0) sb.append(hours).append(" час. ");
        if (minutes > 0) sb.append(minutes).append(" мин. ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append(" сек.");

        return sb.toString().trim();
    }

    public interface TimerTask { //todo на следующей итерации
        public void nextSecound(EventTimer timer);
    }
}
