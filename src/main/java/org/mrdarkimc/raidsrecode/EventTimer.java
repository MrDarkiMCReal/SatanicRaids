package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mrdarkimc.SatanicLib.tasks.CountDownTask;
import org.mrdarkimc.raidsrecode.manager.Undoable;

import java.util.ArrayList;
import java.util.List;

//todo обьеденить с taskRunner ?
public class EventTimer extends CountDownTask {
    private List<TimerTask> additionalTasks = new ArrayList<>();

    public EventTimer(JavaPlugin plugin, int totalDuration) {
        super(plugin, totalDuration);
    }

    public int getCurrentTime() {
        return current;
    }

    @Override
    public void work() {
        additionalTasks.forEach((timer) -> timer.nextSecound(this));
//        Bukkit.getLogger().info("For current event running: " + additionalTasks.size() + " repeatable tasks each second: ");
//        Bukkit.getLogger().info(additionalTasks.toString());
    }

    public void addEachSecondUpdateTask(TimerTask runnable) {
        additionalTasks.add(runnable);
    }

    public void removeEachSecondUpdateTask(TimerTask runnable) {
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

    @Override
    public void endTask() {
        Undoable.undoEach(additionalTasks);
        super.endTask();
    }

    public interface TimerTask { //todo на следующей итерации
        public void nextSecound(EventTimer timer);
    }
}
